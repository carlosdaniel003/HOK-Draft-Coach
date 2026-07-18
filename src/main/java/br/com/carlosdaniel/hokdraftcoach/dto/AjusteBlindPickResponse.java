package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record AjusteBlindPickResponse(
    ContextoDraftResponse contexto,
    PerfilSegurancaBlindPickResponse perfil,
    int ajuste,
    List<String> motivos,
    List<String> riscos
) {

    public AjusteBlindPickResponse {
        if (ajuste < -20 || ajuste > 20) {
            throw new IllegalArgumentException(
                "ajuste deve estar entre -20 e 20."
            );
        }
        motivos = List.copyOf(motivos);
        riscos = List.copyOf(riscos);
    }
}
