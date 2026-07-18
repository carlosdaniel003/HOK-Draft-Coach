package br.com.carlosdaniel.hokdraftcoach.model;

public record DistribuicaoDano(
    int fisico,
    int magico,
    int misto
) {

    public DistribuicaoDano {
        validar("fisico", fisico);
        validar("magico", magico);
        validar("misto", misto);
    }

    private static void validar(String campo, int valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                campo + " deve estar entre 0 e 100."
            );
        }
    }
}
