package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.EscolhaDraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;

@Service
public class DraftDnaService {

    private static final int LIMITE_RECOMENDACOES = 5;

    private final DraftService draftService;
    private final HeroiService heroiService;
    private final DnaComposicaoService dnaComposicaoService;

    public DraftDnaService(
        DraftService draftService,
        HeroiService heroiService,
        DnaComposicaoService dnaComposicaoService
    ) {
        this.draftService = draftService;
        this.heroiService = heroiService;
        this.dnaComposicaoService = dnaComposicaoService;
    }

    public AnaliseDraftResponse recomendar(DraftRequest request) {
        List<String> aliados = nomes(request.aliados());
        List<String> inimigos = nomes(request.inimigos());
        DiagnosticoComposicaoResponse diagnostico = aliados.isEmpty()
            ? null
            : dnaComposicaoService.diagnosticar(aliados, inimigos);

        AnaliseDraftResponse base = draftService.recomendar(request);
        if (diagnostico == null || base.recomendacoes().isEmpty()) {
            return anexarDiagnostico(base, diagnostico);
        }

        Map<String, RecomendacaoDnaResponse> porHeroi =
            dnaComposicaoService.recomendar(
                aliados,
                inimigos,
                request.rotaAlvo(),
                50
            )
            .stream()
            .collect(
                LinkedHashMap::new,
                (mapa, resposta) -> mapa.put(resposta.heroi(), resposta),
                Map::putAll
            );

        List<RecomendacaoDraftResponse> ajustadas = base.recomendacoes()
            .stream()
            .map(recomendacao -> ajustar(
                recomendacao,
                porHeroi.get(recomendacao.nome())
            ))
            .sorted(
                Comparator.comparingInt(
                    RecomendacaoDraftResponse::pontuacaoFinal
                )
                .reversed()
                .thenComparing(RecomendacaoDraftResponse::nome)
            )
            .limit(LIMITE_RECOMENDACOES)
            .toList();

        List<String> avisos = new ArrayList<>(base.avisos());
        avisos.add(
            "O diagnóstico de composição foi calculado antes da ordenação dos candidatos."
        );

        return new AnaliseDraftResponse(
            base.versaoDados(),
            base.rotaAlvo(),
            base.totalCandidatos(),
            diagnostico,
            ajustadas,
            avisos.stream().distinct().toList()
        );
    }

    private RecomendacaoDraftResponse ajustar(
        RecomendacaoDraftResponse base,
        RecomendacaoDnaResponse dna
    ) {
        if (dna == null) {
            return base;
        }

        int componenteDna = limitar(
            (int) Math.round((dna.pontuacao() - 50) / 3.0),
            -5,
            15
        );
        int pontuacaoFinal = limitar(
            base.pontuacaoFinal() + componenteDna,
            0,
            100
        );
        Map<String, Integer> componentes = new LinkedHashMap<>(
            base.componentes()
        );
        componentes.put("dnaComposicao", componenteDna);

        List<String> motivos = new ArrayList<>(base.motivos());
        if (!dna.corrige().isEmpty()) {
            motivos.add("Corrige déficits do DNA: " + dna.corrige() + ".");
        }
        if (!dna.explora().isEmpty()) {
            motivos.add("Explora o DNA inimigo: " + dna.explora() + ".");
        }
        motivos.addAll(dna.motivos().stream().limit(2).toList());

        return new RecomendacaoDraftResponse(
            base.heroiId(),
            base.nome(),
            base.rota(),
            pontuacaoFinal,
            nivel(pontuacaoFinal),
            Map.copyOf(componentes),
            motivos.stream().distinct().limit(8).toList(),
            base.dadosValidados()
        );
    }

    private AnaliseDraftResponse anexarDiagnostico(
        AnaliseDraftResponse base,
        DiagnosticoComposicaoResponse diagnostico
    ) {
        return new AnaliseDraftResponse(
            base.versaoDados(),
            base.rotaAlvo(),
            base.totalCandidatos(),
            diagnostico,
            base.recomendacoes(),
            base.avisos()
        );
    }

    private List<String> nomes(List<EscolhaDraftRequest> escolhas) {
        return escolhas.stream()
            .map(escolha -> buscarHeroi(escolha.heroiId()).getNome())
            .toList();
    }

    private Heroi buscarHeroi(Long id) {
        return heroiService.buscarPorId(id)
            .orElseThrow(() -> new RegraNegocioException(
                "Herói de ID " + id + " não encontrado."
            ));
    }

    private String nivel(int pontuacao) {
        if (pontuacao >= 80) {
            return "FORTE";
        }
        if (pontuacao >= 65) {
            return "BOA";
        }
        if (pontuacao >= 50) {
            return "SITUACIONAL";
        }
        return "ARRISCADA";
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
