package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record ConfrontoJungle(
    String vencedor,
    String alvo,
    int vantagem,
    ConfiancaDado confianca,
    List<TipoVantagemJungle> tipos,
    String motivo
) {

    public ConfrontoJungle {
        if (vantagem < 1 || vantagem > 10) {
            throw new IllegalArgumentException(
                "vantagem deve estar entre 1 e 10."
            );
        }
        tipos = List.copyOf(tipos);
    }
}
