package br.com.carlosdaniel.hokdraftcoach.model;

public enum NivelDna {
    CRITICO,
    BAIXO,
    ADEQUADO,
    FORTE,
    EXCEPCIONAL;

    public static NivelDna classificar(int valor) {
        if (valor < 30) {
            return CRITICO;
        }
        if (valor < 50) {
            return BAIXO;
        }
        if (valor < 70) {
            return ADEQUADO;
        }
        if (valor < 85) {
            return FORTE;
        }
        return EXCEPCIONAL;
    }
}
