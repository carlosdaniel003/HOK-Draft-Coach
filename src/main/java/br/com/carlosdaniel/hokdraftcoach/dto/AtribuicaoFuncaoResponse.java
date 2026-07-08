package br.com.carlosdaniel.hokdraftcoach.dto;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record AtribuicaoFuncaoResponse(
    String slot,
    Long heroiId,
    String heroi,
    Rota rota,
    boolean rotaPrincipal,
    boolean flex
) {
}
