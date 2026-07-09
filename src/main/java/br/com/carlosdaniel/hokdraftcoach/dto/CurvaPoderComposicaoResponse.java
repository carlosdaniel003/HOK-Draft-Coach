package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.FaseJogo;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilTemporalHeroi;

public record CurvaPoderComposicaoResponse(
    int earlyGame,
    int midGame,
    int lateGame,
    FaseJogo pico,
    String planoTemporal,
    List<String> alertas,
    List<PerfilTemporalHeroi> perfis
) {

    public CurvaPoderComposicaoResponse {
        alertas = List.copyOf(alertas);
        perfis = List.copyOf(perfis);
        validar("earlyGame", earlyGame);
        validar("midGame", midGame);
        validar("lateGame", lateGame);
    }

    public static CurvaPoderComposicaoResponse vazia() {
        return new CurvaPoderComposicaoResponse(
            0,
            0,
            0,
            FaseJogo.EQUILIBRADA,
            "Nenhum herói informado.",
            List.of(),
            List.of()
        );
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
