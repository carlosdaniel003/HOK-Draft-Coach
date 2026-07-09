package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record PerfilSuporte(
    String heroi,
    List<ArquetipoSuporte> arquetipos,
    List<TipoComposicao> fortalece,
    List<TipoComposicao> quebra,
    List<TipoComposicao> sofreContra,
    int protecaoCarry,
    int iniciacao,
    int desengage,
    int sustentacao,
    int pressaoRota,
    int mobilidadeMapa,
    int dependenciaCoordenacao,
    List<String> observacoes
) {

    public PerfilSuporte {
        arquetipos = List.copyOf(arquetipos);
        fortalece = List.copyOf(fortalece);
        quebra = List.copyOf(quebra);
        sofreContra = List.copyOf(sofreContra);
        observacoes = List.copyOf(observacoes);

        validarFaixa("protecaoCarry", protecaoCarry);
        validarFaixa("iniciacao", iniciacao);
        validarFaixa("desengage", desengage);
        validarFaixa("sustentacao", sustentacao);
        validarFaixa("pressaoRota", pressaoRota);
        validarFaixa("mobilidadeMapa", mobilidadeMapa);
        validarFaixa("dependenciaCoordenacao", dependenciaCoordenacao);
    }

    private static void validarFaixa(String atributo, int valor) {
        if (valor < 0 || valor > 10) {
            throw new IllegalArgumentException(
                atributo + " deve estar entre 0 e 10."
            );
        }
    }
}
