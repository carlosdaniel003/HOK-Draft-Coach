package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.PapelAmeaca;

public record PerfilAmeacaHeroiResponse(
    String heroi,
    int potencialVitoria,
    int protecao,
    int iniciacao,
    int habilitacao,
    int vulnerabilidade,
    List<PapelAmeaca> papeis,
    List<String> motivos
) {

    public PerfilAmeacaHeroiResponse {
        validar("potencialVitoria", potencialVitoria);
        validar("protecao", protecao);
        validar("iniciacao", iniciacao);
        validar("habilitacao", habilitacao);
        validar("vulnerabilidade", vulnerabilidade);
        papeis = List.copyOf(papeis);
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
