package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record PerfilTemporalHeroi(
    String heroi,
    int earlyGame,
    int midGame,
    int lateGame,
    FaseJogo pico,
    List<String> motivos
) {

    public PerfilTemporalHeroi {
        validar("earlyGame", earlyGame);
        validar("midGame", midGame);
        validar("lateGame", lateGame);
        motivos = List.copyOf(motivos);
    }

    public int valor(FaseJogo fase) {
        return switch (fase) {
            case EARLY_GAME -> earlyGame;
            case MID_GAME -> midGame;
            case LATE_GAME -> lateGame;
            case EQUILIBRADA -> Math.round((earlyGame + midGame + lateGame) / 3.0f);
        };
    }

    private static void validar(String campo, int valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                campo + " deve estar entre 0 e 100."
            );
        }
    }
}
