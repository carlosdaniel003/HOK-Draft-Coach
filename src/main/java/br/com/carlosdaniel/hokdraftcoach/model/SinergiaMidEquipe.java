package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record SinergiaMidEquipe(
    String mid,
    String aliado,
    int nota,
    ConfiancaDado confianca,
    String motivo,
    List<String> gatilhos
) {

    public SinergiaMidEquipe {
        if (nota < 1 || nota > 10) {
            throw new IllegalArgumentException("nota deve estar entre 1 e 10.");
        }
        gatilhos = List.copyOf(gatilhos);
    }
}
