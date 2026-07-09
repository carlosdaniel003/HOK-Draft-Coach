package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.List;

public record ConfrontoClashLane(
    String vencedor,
    String alvo,
    int vantagem,
    ConfiancaDado confianca,
    List<TipoVantagemClash> tipos,
    String motivo
) {

    public ConfrontoClashLane {
        if (vantagem < 1 || vantagem > 10) {
            throw new IllegalArgumentException(
                "vantagem deve estar entre 1 e 10."
            );
        }
        tipos = List.copyOf(tipos);
    }
}
