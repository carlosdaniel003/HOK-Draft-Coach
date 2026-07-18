package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.PapelAmeaca;

public record AlvoPrioritarioAmeacaResponse(
    String heroi,
    PapelAmeaca papel,
    int prioridade,
    List<DimensaoEstrategica> respostasRecomendadas,
    String justificativa
) {

    public AlvoPrioritarioAmeacaResponse {
        if (prioridade < 0 || prioridade > 100) {
            throw new IllegalArgumentException(
                "prioridade deve estar entre 0 e 100."
            );
        }
        respostasRecomendadas = List.copyOf(respostasRecomendadas);
    }
}
