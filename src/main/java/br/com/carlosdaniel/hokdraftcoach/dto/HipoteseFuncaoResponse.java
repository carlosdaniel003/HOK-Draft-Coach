package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record HipoteseFuncaoResponse(
    int ordem,
    int pontuacaoAfinidade,
    boolean principal,
    List<AtribuicaoFuncaoResponse> atribuicoes,
    List<Rota> rotasAbertas
) {
}
