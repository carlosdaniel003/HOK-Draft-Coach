package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.TipoRespostaCondicaoVitoria;

public record RespostaCondicaoVitoriaResponse(
    TipoRespostaCondicaoVitoria tipo,
    int prioridade,
    String titulo,
    String descricao,
    List<DimensaoEstrategica> capacidadesNecessarias,
    List<String> alvosPrioritarios
) {

    public RespostaCondicaoVitoriaResponse {
        capacidadesNecessarias = List.copyOf(capacidadesNecessarias);
        alvosPrioritarios = List.copyOf(alvosPrioritarios);
        if (prioridade < 0 || prioridade > 100) {
            throw new IllegalArgumentException(
                "prioridade deve estar entre 0 e 100."
            );
        }
    }
}
