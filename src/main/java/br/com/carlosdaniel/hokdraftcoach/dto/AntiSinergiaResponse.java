package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;
import br.com.carlosdaniel.hokdraftcoach.model.TipoAntiSinergia;

public record AntiSinergiaResponse(
    String codigo,
    TipoAntiSinergia tipo,
    SeveridadeDiagnostico severidade,
    int penalidade,
    List<String> herois,
    String descricao,
    String mitigacao
) {

    public AntiSinergiaResponse {
        herois = List.copyOf(herois);
        if (penalidade < 0 || penalidade > 100) {
            throw new IllegalArgumentException(
                "penalidade deve estar entre 0 e 100."
            );
        }
    }
}
