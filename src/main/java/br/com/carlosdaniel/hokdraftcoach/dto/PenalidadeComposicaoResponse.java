package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;

public record PenalidadeComposicaoResponse(
    String codigo,
    SeveridadeDiagnostico severidade,
    int reducaoPontuacao,
    String titulo,
    String descricao,
    List<DimensaoEstrategica> redundancias,
    List<String> heroisRelacionados
) {

    public PenalidadeComposicaoResponse {
        redundancias = List.copyOf(redundancias);
        heroisRelacionados = List.copyOf(heroisRelacionados);
        if (reducaoPontuacao < 0 || reducaoPontuacao > 100) {
            throw new IllegalArgumentException(
                "reducaoPontuacao deve estar entre 0 e 100."
            );
        }
    }
}
