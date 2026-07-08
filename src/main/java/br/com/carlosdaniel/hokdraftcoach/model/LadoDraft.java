package br.com.carlosdaniel.hokdraftcoach.model;

public enum LadoDraft {
    AZUL,
    VERMELHO;

    public LadoDraft adversario() {
        return this == AZUL ? VERMELHO : AZUL;
    }

    public String prefixoSlot() {
        return this == AZUL ? "B" : "R";
    }
}
