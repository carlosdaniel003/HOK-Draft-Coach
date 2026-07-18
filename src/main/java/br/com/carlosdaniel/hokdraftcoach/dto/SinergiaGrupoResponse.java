package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.TipoSinergiaGrupo;

public record SinergiaGrupoResponse(
    String codigo,
    TipoSinergiaGrupo tipo,
    int nota,
    boolean ativa,
    List<String> membros,
    List<String> faltando,
    String descricao,
    List<String> sequencia,
    List<String> beneficios
) {

    public SinergiaGrupoResponse {
        membros = List.copyOf(membros);
        faltando = List.copyOf(faltando);
        sequencia = List.copyOf(sequencia);
        beneficios = List.copyOf(beneficios);
        if (nota < 0 || nota > 100) {
            throw new IllegalArgumentException(
                "nota deve estar entre 0 e 100."
            );
        }
    }
}
