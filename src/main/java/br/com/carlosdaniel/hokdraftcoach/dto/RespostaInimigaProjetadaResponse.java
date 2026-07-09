package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record RespostaInimigaProjetadaResponse(
    String heroi,
    Rota rota,
    int probabilidadeHeuristica,
    int impactoContraNossaComposicao,
    int pontuacaoInimiga,
    List<String> motivos
) {

    public RespostaInimigaProjetadaResponse {
        validar("probabilidadeHeuristica", probabilidadeHeuristica);
        validar("impactoContraNossaComposicao", impactoContraNossaComposicao);
        validar("pontuacaoInimiga", pontuacaoInimiga);
        motivos = List.copyOf(motivos);
    }

    private static void validar(String campo, int valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                campo + " deve estar entre 0 e 100."
            );
        }
    }
}
