package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record AjusteRespostaAmeacaResponse(
    int bonus,
    List<String> alvosRespondidos,
    List<String> motivos
) {

    public AjusteRespostaAmeacaResponse {
        if (bonus < 0 || bonus > 20) {
            throw new IllegalArgumentException(
                "bonus deve estar entre 0 e 20."
            );
        }
        alvosRespondidos = List.copyOf(alvosRespondidos);
        motivos = List.copyOf(motivos);
    }

    public static AjusteRespostaAmeacaResponse vazio() {
        return new AjusteRespostaAmeacaResponse(0, List.of(), List.of());
    }
}
