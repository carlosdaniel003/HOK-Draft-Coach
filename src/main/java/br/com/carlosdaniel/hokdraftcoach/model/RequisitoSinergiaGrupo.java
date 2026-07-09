package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record RequisitoSinergiaGrupo(
    String codigo,
    String descricao,
    int quantidade,
    List<ClasseHeroi> classesAceitas,
    List<String> tagsAceitas,
    DimensaoEstrategica dimensao,
    int valorMinimo
) {

    public RequisitoSinergiaGrupo {
        classesAceitas = List.copyOf(classesAceitas);
        tagsAceitas = List.copyOf(tagsAceitas);
        if (quantidade < 1 || quantidade > 5) {
            throw new IllegalArgumentException(
                "quantidade deve estar entre 1 e 5."
            );
        }
        if (valorMinimo < 0 || valorMinimo > 100) {
            throw new IllegalArgumentException(
                "valorMinimo deve estar entre 0 e 100."
            );
        }
    }
}
