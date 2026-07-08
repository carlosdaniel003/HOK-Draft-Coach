package br.com.carlosdaniel.hokdraftcoach.model;

public record Confronto(
    String inimigo,
    String recomendado,
    int pontuacao,
    String motivo
) {

    public String calcularNivel() {
        if (pontuacao >= 80) {
            return "FORTE";
        }

        if (pontuacao >= 60) {
            return "MODERADO";
        }

        return "SITUACIONAL";
    }
}