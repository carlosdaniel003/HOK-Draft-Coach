package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record ProjecaoPickResponse(
    String nossoHeroi,
    int robustez,
    int piorCenarioProjetado,
    int ajusteProjecao,
    List<RespostaInimigaProjetadaResponse> respostasProvaveis,
    String resumoPiorCenario
) {

    public ProjecaoPickResponse {
        validar("robustez", robustez);
        validar("piorCenarioProjetado", piorCenarioProjetado);
        if (ajusteProjecao < -15 || ajusteProjecao > 15) {
            throw new IllegalArgumentException(
                "ajusteProjecao deve estar entre -15 e 15."
            );
        }
        respostasProvaveis = List.copyOf(respostasProvaveis);
    }

    public static ProjecaoPickResponse vazia(String heroi, int pontuacao) {
        return new ProjecaoPickResponse(
            heroi,
            pontuacao,
            pontuacao,
            0,
            List.of(),
            "Não há pick inimigo restante ou informação suficiente para projetar uma resposta."
        );
    }

    private static void validar(String campo, int valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                campo + " deve estar entre 0 e 100."
            );
        }
    }
}
