package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;

public record DiagnosticoComposicaoResponse(
    DnaComposicao nossaComposicao,
    DnaComposicao composicaoInimiga,
    List<DiagnosticoEstrategico> diagnosticos,
    List<PrioridadeDraftResponse> prioridades,
    boolean diagnosticoConcluido
) {

    public DiagnosticoComposicaoResponse {
        diagnosticos = List.copyOf(diagnosticos);
        prioridades = List.copyOf(prioridades);
    }
}
