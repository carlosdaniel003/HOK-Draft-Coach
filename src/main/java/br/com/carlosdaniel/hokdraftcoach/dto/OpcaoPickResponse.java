package br.com.carlosdaniel.hokdraftcoach.dto;

import br.com.carlosdaniel.hokdraftcoach.model.TipoOpcaoPick;

public record OpcaoPickResponse(
    TipoOpcaoPick tipo,
    String titulo,
    int pontuacaoCategoria,
    RecomendacaoPickResponse escolha,
    ProjecaoPickResponse projecao,
    ExplicacaoRecomendacaoResponse explicacao
) {

    public OpcaoPickResponse {
        if (pontuacaoCategoria < 0 || pontuacaoCategoria > 100) {
            throw new IllegalArgumentException(
                "pontuacaoCategoria deve estar entre 0 e 100."
            );
        }
    }
}
