package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record PerfilJungle(
    String heroi,
    List<ArquetipoJungle> arquetipos,
    List<TipoComposicao> fortalece,
    List<TipoComposicao> quebra,
    List<TipoComposicao> sofreContra,
    int limpeza,
    int gank,
    int invasao,
    int duelo,
    int objetivos,
    int mobilidade,
    int explosao,
    int danoSustentado,
    int linhaDeFrente,
    int seguranca,
    int dependenciaCoordenacao,
    List<String> observacoes
) {

    public PerfilJungle {
        arquetipos = List.copyOf(arquetipos);
        fortalece = List.copyOf(fortalece);
        quebra = List.copyOf(quebra);
        sofreContra = List.copyOf(sofreContra);
        observacoes = List.copyOf(observacoes);

        validar("limpeza", limpeza);
        validar("gank", gank);
        validar("invasao", invasao);
        validar("duelo", duelo);
        validar("objetivos", objetivos);
        validar("mobilidade", mobilidade);
        validar("explosao", explosao);
        validar("danoSustentado", danoSustentado);
        validar("linhaDeFrente", linhaDeFrente);
        validar("seguranca", seguranca);
        validar("dependenciaCoordenacao", dependenciaCoordenacao);
    }

    private static void validar(String atributo, int valor) {
        if (valor < 0 || valor > 10) {
            throw new IllegalArgumentException(
                atributo + " deve estar entre 0 e 10."
            );
        }
    }
}
