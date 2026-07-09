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
    CurvaPoderComposicaoResponse curvaPoderNossaComposicao,
    CurvaPoderComposicaoResponse curvaPoderComposicaoInimiga,
    List<DiagnosticoTemporalResponse> diagnosticosTemporais,
    List<SinergiaGrupoResponse> sinergiasGrupoNossaComposicao,
    List<SinergiaGrupoResponse> sinergiasGrupoComposicaoInimiga,
    List<AntiSinergiaResponse> antiSinergiasNossaComposicao,
    List<AntiSinergiaResponse> antiSinergiasComposicaoInimiga,
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
        curvaPoderNossaComposicao = curvaPoderNossaComposicao == null
            ? CurvaPoderComposicaoResponse.vazia()
            : curvaPoderNossaComposicao;
        curvaPoderComposicaoInimiga = curvaPoderComposicaoInimiga == null
            ? CurvaPoderComposicaoResponse.vazia()
            : curvaPoderComposicaoInimiga;
        diagnosticosTemporais = List.copyOf(diagnosticosTemporais);
        sinergiasGrupoNossaComposicao = List.copyOf(
            sinergiasGrupoNossaComposicao
        );
        sinergiasGrupoComposicaoInimiga = List.copyOf(
            sinergiasGrupoComposicaoInimiga
        );
        antiSinergiasNossaComposicao = List.copyOf(
            antiSinergiasNossaComposicao
        );
        antiSinergiasComposicaoInimiga = List.copyOf(
            antiSinergiasComposicaoInimiga
        );
    }

    public DiagnosticoComposicaoResponse(
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
        this(
            nossaComposicao,
            composicaoInimiga,
            diagnosticos,
            prioridades,
            nossasCondicoesVitoria,
            condicoesVitoriaInimigas,
            necessidades,
            penalidades,
            economiaNossaComposicao,
            economiaComposicaoInimiga,
            CurvaPoderComposicaoResponse.vazia(),
            CurvaPoderComposicaoResponse.vazia(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            diagnosticoConcluido
        );
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
            CurvaPoderComposicaoResponse.vazia(),
            CurvaPoderComposicaoResponse.vazia(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            diagnosticoConcluido
        );
    }
}
