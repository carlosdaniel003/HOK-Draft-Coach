package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record RegraSinergiaGrupo(
    String codigo,
    TipoSinergiaGrupo tipo,
    int nota,
    List<String> heroisObrigatorios,
    List<RequisitoSinergiaGrupo> requisitos,
    String descricao,
    List<String> sequencia,
    List<String> beneficios
) {

    public RegraSinergiaGrupo {
        heroisObrigatorios = List.copyOf(heroisObrigatorios);
        requisitos = List.copyOf(requisitos);
        sequencia = List.copyOf(sequencia);
        beneficios = List.copyOf(beneficios);
        if (nota < 1 || nota > 100) {
            throw new IllegalArgumentException(
                "nota deve estar entre 1 e 100."
            );
        }
        int tamanhoMinimo = heroisObrigatorios.size()
            + requisitos.stream().mapToInt(RequisitoSinergiaGrupo::quantidade).sum();
        if (tamanhoMinimo < 3 || tamanhoMinimo > 5) {
            throw new IllegalArgumentException(
                "Uma sinergia de grupo deve exigir de 3 a 5 membros."
            );
        }
    }
}
