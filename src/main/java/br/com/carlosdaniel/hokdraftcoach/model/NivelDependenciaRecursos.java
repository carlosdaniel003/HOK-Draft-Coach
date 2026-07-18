package br.com.carlosdaniel.hokdraftcoach.model;

public enum NivelDependenciaRecursos {
    BAIXA,
    MEDIA,
    ALTA,
    CRITICA;

    public static NivelDependenciaRecursos classificar(int valor) {
        if (valor < 40) {
            return BAIXA;
        }
        if (valor < 65) {
            return MEDIA;
        }
        if (valor < 82) {
            return ALTA;
        }
        return CRITICA;
    }
}
