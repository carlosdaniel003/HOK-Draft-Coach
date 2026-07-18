package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;

public record RecomendacaoEncaixeClashResponse(
    String top,
    int pontuacao,
    List<TipoComposicao> fortalece,
    List<String> motivos
) {
}
