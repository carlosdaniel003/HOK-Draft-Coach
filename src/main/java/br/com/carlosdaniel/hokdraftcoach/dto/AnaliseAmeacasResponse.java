package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record AnaliseAmeacasResponse(
    PerfilAmeacaHeroiResponse maiorAmeaca,
    PerfilAmeacaHeroiResponse protetorPrincipal,
    PerfilAmeacaHeroiResponse iniciadorPrincipal,
    PerfilAmeacaHeroiResponse habilitadorCritico,
    PerfilAmeacaHeroiResponse eloFraco,
    List<PerfilAmeacaHeroiResponse> perfis,
    List<AlvoPrioritarioAmeacaResponse> alvosPrioritarios,
    String planoResposta
) {

    public AnaliseAmeacasResponse {
        perfis = List.copyOf(perfis);
        alvosPrioritarios = List.copyOf(alvosPrioritarios);
    }

    public static AnaliseAmeacasResponse vazia() {
        return new AnaliseAmeacasResponse(
            null,
            null,
            null,
            null,
            null,
            List.of(),
            List.of(),
            "Nenhuma composição inimiga informada."
        );
    }
}
