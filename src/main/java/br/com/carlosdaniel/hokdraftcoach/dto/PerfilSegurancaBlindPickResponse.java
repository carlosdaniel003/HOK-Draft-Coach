package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record PerfilSegurancaBlindPickResponse(
    String heroi,
    int segurancaBlind,
    int flexibilidade,
    int consistencia,
    int riscoCounters,
    int especificidade,
    List<String> motivos
) {

    public PerfilSegurancaBlindPickResponse {
        validar("segurancaBlind", segurancaBlind);
        validar("flexibilidade", flexibilidade);
        validar("consistencia", consistencia);
        validar("riscoCounters", riscoCounters);
        validar("especificidade", especificidade);
        motivos = List.copyOf(motivos);
    }

    private static void validar(String campo, int valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                campo + " deve estar entre 0 e 100."
            );
        }
    }
}
