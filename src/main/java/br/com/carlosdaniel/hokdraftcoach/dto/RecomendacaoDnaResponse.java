package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record RecomendacaoDnaResponse(
    String heroi,
    Rota rota,
    int pontuacao,
    List<DimensaoEstrategica> corrige,
    List<DimensaoEstrategica> explora,
    List<String> motivos
) {

    public RecomendacaoDnaResponse {
        corrige = List.copyOf(corrige);
        explora = List.copyOf(explora);
        motivos = List.copyOf(motivos);
    }
}
