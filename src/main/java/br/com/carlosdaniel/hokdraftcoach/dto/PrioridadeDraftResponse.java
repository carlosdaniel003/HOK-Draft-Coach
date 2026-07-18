package br.com.carlosdaniel.hokdraftcoach.dto;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;

public record PrioridadeDraftResponse(
    DimensaoEstrategica dimensao,
    int urgencia,
    int valorAtual,
    int alvoMinimo,
    String motivo
) {

    public PrioridadeDraftResponse {
        if (urgencia < 0 || urgencia > 100) {
            throw new IllegalArgumentException(
                "urgencia deve estar entre 0 e 100."
            );
        }
    }
}
