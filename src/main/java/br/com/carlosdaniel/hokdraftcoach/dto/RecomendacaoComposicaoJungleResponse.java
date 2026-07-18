package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;

public record RecomendacaoComposicaoJungleResponse(
    String jungler,
    int pontuacao,
    List<TipoComposicao> respondeA,
    List<TipoComposicao> vulneravelA,
    List<String> motivos
) {
}
