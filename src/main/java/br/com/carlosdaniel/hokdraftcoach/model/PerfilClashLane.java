package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record PerfilClashLane(
    String heroi,
    List<ArquetipoClash> arquetipos,
    List<TipoComposicao> fortalece,
    List<TipoComposicao> quebra,
    List<TipoComposicao> sofreContra,
    int pressaoDeRota,
    int duelo,
    int sustentacao,
    int waveClear,
    int splitPush,
    int rotacao,
    int engage,
    int linhaDeFrente,
    int danoSustentado,
    int explosao,
    int escalamento,
    int seguranca,
    int dependenciaCoordenacao,
    List<String> observacoes
) {

    public PerfilClashLane {
        arquetipos = List.copyOf(arquetipos);
        fortalece = List.copyOf(fortalece);
        quebra = List.copyOf(quebra);
        sofreContra = List.copyOf(sofreContra);
        observacoes = List.copyOf(observacoes);

        validar("pressaoDeRota", pressaoDeRota);
        validar("duelo", duelo);
        validar("sustentacao", sustentacao);
        validar("waveClear", waveClear);
        validar("splitPush", splitPush);
        validar("rotacao", rotacao);
        validar("engage", engage);
        validar("linhaDeFrente", linhaDeFrente);
        validar("danoSustentado", danoSustentado);
        validar("explosao", explosao);
        validar("escalamento", escalamento);
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
