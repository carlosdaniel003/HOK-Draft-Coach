package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record AnaliseDraftResponse(
    String versaoDados,
    Rota rotaAlvo,
    int totalCandidatos,
    DiagnosticoComposicaoResponse diagnosticoComposicao,
    List<RecomendacaoDraftResponse> recomendacoes,
    List<String> avisos
) {

    public AnaliseDraftResponse(
        String versaoDados,
        Rota rotaAlvo,
        int totalCandidatos,
        List<RecomendacaoDraftResponse> recomendacoes,
        List<String> avisos
    ) {
        this(
            versaoDados,
            rotaAlvo,
            totalCandidatos,
            null,
            recomendacoes,
            avisos
        );
    }
}
