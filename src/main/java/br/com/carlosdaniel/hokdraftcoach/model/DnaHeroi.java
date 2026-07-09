package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.EnumMap;
import java.util.Map;

public record DnaHeroi(
    String heroi,
    Map<DimensaoEstrategica, Integer> vetor
) {

    public DnaHeroi {
        EnumMap<DimensaoEstrategica, Integer> copia =
            new EnumMap<>(DimensaoEstrategica.class);
        copia.putAll(vetor);
        for (DimensaoEstrategica dimensao : DimensaoEstrategica.values()) {
            int valor = copia.getOrDefault(dimensao, 0);
            if (valor < 0 || valor > 100) {
                throw new IllegalArgumentException(
                    dimensao + " deve estar entre 0 e 100."
                );
            }
            copia.put(dimensao, valor);
        }
        vetor = Map.copyOf(copia);
    }

    public int valor(DimensaoEstrategica dimensao) {
        return vetor.getOrDefault(dimensao, 0);
    }
}
