package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.NivelDependenciaRecursos;

public record PerfilEconomicoHeroiResponse(
    String heroi,
    int dependenciaRecursos,
    NivelDependenciaRecursos nivel,
    int utilidadeSemOuro,
    boolean consegueCederRecursos,
    List<String> motivos
) {

    public PerfilEconomicoHeroiResponse {
        motivos = List.copyOf(motivos);
        validar("dependenciaRecursos", dependenciaRecursos);
        validar("utilidadeSemOuro", utilidadeSemOuro);
    }

    private static void validar(String campo, int valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                campo + " deve estar entre 0 e 100."
            );
        }
    }
}
