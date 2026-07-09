package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;

public record DiagnosticoEstrategico(
    String codigo,
    SeveridadeDiagnostico severidade,
    int prioridade,
    String titulo,
    String descricao,
    List<DimensaoEstrategica> dimensoes
) {

    public DiagnosticoEstrategico {
        dimensoes = List.copyOf(dimensoes);
        if (prioridade < 0 || prioridade > 100) {
            throw new IllegalArgumentException(
                "prioridade deve estar entre 0 e 100."
            );
        }
    }
}
