package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record RegraAntiSinergia(
    String codigo,
    TipoAntiSinergia tipo,
    int penalidade,
    List<String> heroisObrigatorios,
    String descricao,
    String mitigacao
) {

    public RegraAntiSinergia {
        heroisObrigatorios = List.copyOf(heroisObrigatorios);
        if (heroisObrigatorios.size() < 2 || heroisObrigatorios.size() > 5) {
            throw new IllegalArgumentException(
                "Uma anti-sinergia explícita deve envolver de 2 a 5 heróis."
            );
        }
        if (penalidade < 1 || penalidade > 100) {
            throw new IllegalArgumentException(
                "penalidade deve estar entre 1 e 100."
            );
        }
    }
}
