package br.com.carlosdaniel.hokdraftcoach.dto;

import br.com.carlosdaniel.hokdraftcoach.model.FaseJogo;
import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;

public record DiagnosticoTemporalResponse(
    String codigo,
    SeveridadeDiagnostico severidade,
    int prioridade,
    String titulo,
    String descricao,
    FaseJogo janelaRecomendada
) {

    public DiagnosticoTemporalResponse {
        if (prioridade < 0 || prioridade > 100) {
            throw new IllegalArgumentException(
                "prioridade deve estar entre 0 e 100."
            );
        }
    }
}
