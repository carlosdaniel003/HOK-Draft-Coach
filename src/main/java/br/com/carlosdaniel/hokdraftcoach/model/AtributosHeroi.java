package br.com.carlosdaniel.hokdraftcoach.model;

public record AtributosHeroi(
    int controle,
    int resistencia,
    int mobilidade,
    int alcance,
    int danoSustentado,
    int danoExplosivo
) {

    public AtributosHeroi {
        validarFaixa("controle", controle);
        validarFaixa("resistencia", resistencia);
        validarFaixa("mobilidade", mobilidade);
        validarFaixa("alcance", alcance);
        validarFaixa("danoSustentado", danoSustentado);
        validarFaixa("danoExplosivo", danoExplosivo);
    }

    private static void validarFaixa(String atributo, int valor) {
        if (valor < 0 || valor > 10) {
            throw new IllegalArgumentException(
                atributo + " deve estar entre 0 e 10."
            );
        }
    }
}
