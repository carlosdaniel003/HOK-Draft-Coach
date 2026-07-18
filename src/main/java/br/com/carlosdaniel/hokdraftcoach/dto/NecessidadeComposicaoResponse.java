package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;

public record NecessidadeComposicaoResponse(
    String codigo,
    DimensaoEstrategica dimensao,
    int urgencia,
    int valorAtual,
    int alvoMinimo,
    int deficit,
    String titulo,
    String motivo,
    List<String> capacidadesDesejadas
) {

    public NecessidadeComposicaoResponse {
        capacidadesDesejadas = List.copyOf(capacidadesDesejadas);
        if (urgencia < 0 || urgencia > 100) {
            throw new IllegalArgumentException(
                "urgencia deve estar entre 0 e 100."
            );
        }
    }
}
