package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;

public record DiagnosticoComposicaoResponse(
    DnaComposicao nossaComposicao,
    DnaComposicao composicaoInimiga,
    List<DiagnosticoEstrategico> diagnosticos,
    List<PrioridadeDraftResponse> prioridades,
    List<CondicaoVitoriaResponse> nossasCondicoesVitoria,
    List<CondicaoVitoriaResponse> condicoesVitoriaInimigas,
    List<NecessidadeComposicaoResponse> necessidades,
    List<PenalidadeComposicaoResponse> penalidades,
    EconomiaComposicaoResponse economiaNossaComposicao,
    EconomiaComposicaoResponse economiaComposicaoInimiga,
    boolean diagnosticoConcluido
) {

    public DiagnosticoComposicaoResponse {
        diagnosticos = List.copyOf(diagnosticos);
        prioridades = List.copyOf(prioridades);
        nossasCondicoesVitoria = List.copyOf(nossasCondicoesVitoria);
        condicoesVitoriaInimigas = List.copyOf(condicoesVitoriaInimigas);
        necessidades = List.copyOf(necessidades);
        penalidades = List.copyOf(penalidades);
        economiaNossaComposicao = economiaNossaComposicao == null
            ? EconomiaComposicaoResponse.vazia()
            : economiaNossaComposicao;
        economiaComposicaoInimiga = economiaComposicaoInimiga == null
            ? EconomiaComposicaoResponse.vazia()
            : economiaComposicaoInimiga;
    }

    public DiagnosticoComposicaoResponse(
        DnaComposicao nossaComposicao,
        DnaComposicao composicaoInimiga,
        List<DiagnosticoEstrategico> diagnosticos,
        List<PrioridadeDraftResponse> prioridades,
        boolean diagnosticoConcluido
    ) {
        this(
            nossaComposicao,
            composicaoInimiga,
            diagnosticos,
            prioridades,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            EconomiaComposicaoResponse.vazia(),
            EconomiaComposicaoResponse.vazia(),
            diagnosticoConcluido
        );
    }
}
