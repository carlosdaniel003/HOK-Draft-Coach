package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;

public record RecomendacaoComposicaoClashResponse(
    String top,
    int pontuacao,
    List<TipoComposicao> respondeA,
    List<TipoComposicao> vulneravelA,
    List<String> motivos
) {
}
