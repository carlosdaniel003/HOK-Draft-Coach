package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record PerfilMidLane(
    String heroi,
    List<ArquetipoMid> arquetipos,
    List<TipoComposicao> fortalece,
    List<TipoComposicao> quebra,
    List<TipoComposicao> sofreContra,
    int pressaoRota,
    int waveClear,
    int rotacao,
    int controle,
    int poke,
    int explosao,
    int danoSustentado,
    int seguranca,
    int dependenciaCoordenacao,
    List<String> observacoes
) {

    public PerfilMidLane {
        arquetipos = List.copyOf(arquetipos);
        fortalece = List.copyOf(fortalece);
        quebra = List.copyOf(quebra);
        sofreContra = List.copyOf(sofreContra);
        observacoes = List.copyOf(observacoes);

        validar("pressaoRota", pressaoRota);
        validar("waveClear", waveClear);
        validar("rotacao", rotacao);
        validar("controle", controle);
        validar("poke", poke);
        validar("explosao", explosao);
        validar("danoSustentado", danoSustentado);
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
