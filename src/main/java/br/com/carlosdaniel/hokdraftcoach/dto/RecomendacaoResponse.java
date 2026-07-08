package br.com.carlosdaniel.hokdraftcoach.dto;

public record RecomendacaoResponse(
    String inimigo,
    String recomendado,
    int pontuacao,
    String nivel,
    String motivo,
    boolean dadosValidados
) {
}