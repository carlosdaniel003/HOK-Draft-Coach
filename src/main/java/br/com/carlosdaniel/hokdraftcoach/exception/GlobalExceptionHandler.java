package br.com.carlosdaniel.hokdraftcoach.exception;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiErrorResponse> tratarRegraNegocio(
        RegraNegocioException exception
    ) {
        return resposta(
            HttpStatus.BAD_REQUEST,
            "Regra de negócio inválida",
            List.of(exception.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacao(
        MethodArgumentNotValidException exception
    ) {
        List<String> detalhes = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
            .toList();

        return resposta(
            HttpStatus.BAD_REQUEST,
            "Dados de entrada inválidos",
            detalhes
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> tratarJsonInvalido(
        HttpMessageNotReadableException exception
    ) {
        return resposta(
            HttpStatus.BAD_REQUEST,
            "JSON inválido",
            List.of(
                "Verifique os IDs, nomes das rotas e o formato da requisição."
            )
        );
    }

    private ResponseEntity<ApiErrorResponse> resposta(
        HttpStatus status,
        String erro,
        List<String> detalhes
    ) {
        ApiErrorResponse corpo = new ApiErrorResponse(
            OffsetDateTime.now(),
            status.value(),
            erro,
            detalhes
        );

        return ResponseEntity.status(status).body(corpo);
    }
}
