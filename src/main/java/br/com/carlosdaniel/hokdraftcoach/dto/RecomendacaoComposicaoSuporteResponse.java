package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;

public record RecomendacaoComposicaoSuporteResponse(
    String suporte,
    int pontuacao,
    List<TipoComposicao> respondeA,
    List<TipoComposicao> vulneravelA,
    List<String> motivos
) {
}
