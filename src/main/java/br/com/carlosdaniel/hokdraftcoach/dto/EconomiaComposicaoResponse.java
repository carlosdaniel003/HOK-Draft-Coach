package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record EconomiaComposicaoResponse(
    int cargaEconomica,
    int conflitoDeRecursos,
    int carregadoresDependentes,
    boolean economiaViavel,
    String diagnostico,
    List<PerfilEconomicoHeroiResponse> perfis
) {

    public EconomiaComposicaoResponse {
        perfis = List.copyOf(perfis);
        validar("cargaEconomica", cargaEconomica);
        validar("conflitoDeRecursos", conflitoDeRecursos);
    }

    public static EconomiaComposicaoResponse vazia() {
        return new EconomiaComposicaoResponse(
            0,
            0,
            0,
            true,
            "Nenhum herói informado.",
            List.of()
        );
    }

    private static void validar(String campo, int valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                campo + " deve estar entre 0 e 100."
            );
        }
    }
}
