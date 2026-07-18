package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.MomentoDraft;

public record ContextoDraftResponse(
    MomentoDraft momento,
    int ordemAliada,
    int inimigosRevelados,
    String prioridade,
    List<String> criterios
) {

    public ContextoDraftResponse {
        criterios = List.copyOf(criterios);
    }
}
