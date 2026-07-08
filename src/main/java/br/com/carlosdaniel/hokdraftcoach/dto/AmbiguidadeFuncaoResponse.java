package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record AmbiguidadeFuncaoResponse(
    String slot,
    Long heroiId,
    String heroi,
    boolean funcaoConfirmada,
    List<Rota> funcoesPossiveis
) {
}
