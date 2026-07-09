package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteRespostaAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseAmeacasResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.HipoteseFuncaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaEquipeResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.ProjecaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RespostaInimigaProjetadaResponse;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;

@Service
public class ProjecaoRespostaInimigaService {

    private static final int LIMITE_RESPOSTAS = 3;

    private final HeroiService heroiService;
    private final InferenciaFuncoesService inferenciaFuncoesService;
    private final AnaliseAmeacaComposicaoService composicaoService;
    private final AnaliseAmeacaService analiseAmeacaService;

    public ProjecaoRespostaInimigaService(
        HeroiService heroiService,
        InferenciaFuncoesService inferenciaFuncoesService,
        AnaliseAmeacaComposicaoService composicaoService,
        AnaliseAmeacaService analiseAmeacaService
    ) {
        this.heroiService = heroiService;
        this.inferenciaFuncoesService = inferenciaFuncoesService;
        this.composicaoService = composicaoService;
        this.analiseAmeacaService = analiseAmeacaService;
    }

    public ProjecaoPickResponse projetar(
        RecomendacaoProximoPickRequest request,
        List<String> aliados,
        List<String> inimigos,
        RecomendacaoPickResponse candidato,
        RecomendacaoDnaResponse dnaCandidato
    ) {
        if (picksInimigos(request).size() >= 5) {
            return ProjecaoPickResponse.vazia(
                candidato.heroi(),
                candidato.pontuacaoFinal()
            );
        }

        List<String> aliadosProjetados = new ArrayList<>(aliados);
        aliadosProjetados.add(candidato.heroi());
        Map<Rota, Integer> frequenciaRotas = rotasAbertasInimigas(request);
        if (frequenciaRotas.isEmpty()) {
            for (Rota rota : Rota.values()) {
                frequenciaRotas.put(rota, 1);
            }
        }

        AnaliseAmeacasResponse nossasAmeacas =
            composicaoService.analisarAmeacas(aliadosProjetados);
        Set<Long> indisponiveis = idsIndisponiveis(request);
        indisponiveis.add(candidato.heroiId());
        Map<String, RespostaBruta> respostas = new LinkedHashMap<>();
        int totalFrequencia = frequenciaRotas.values().stream()
            .mapToInt(Integer::intValue)
            .sum();

        for (Map.Entry<Rota, Integer> entrada : frequenciaRotas.entrySet()) {
            Rota rota = entrada.getKey();
            int frequencia = entrada.getValue();
            Map<String, RecomendacaoDnaResponse> estrategicas =
                recomendacoesInimigas(
                    inimigos,
                    aliadosProjetados,
                    rota
                );

            for (Heroi resposta : heroiService.listarPorRota(rota)) {
                if (indisponiveis.contains(resposta.getId())) {
                    continue;
                }
                RecomendacaoDnaResponse recomendacao = estrategicas.get(
                    resposta.getNome()
                );
                AjusteRespostaAmeacaResponse contraAmeacas =
                    analiseAmeacaService.avaliarCandidato(
                        resposta,
                        nossasAmeacas
                    );
                int pontuacao = pontuacaoResposta(
                    resposta,
                    recomendacao,
                    contraAmeacas,
                    frequencia,
                    totalFrequencia
                );
                int impacto = impactoResposta(
                    pontuacao,
                    recomendacao,
                    contraAmeacas
                );
                RespostaBruta bruta = new RespostaBruta(
                    resposta,
                    rota,
                    pontuacao,
                    impacto,
                    frequencia,
                    totalFrequencia,
                    recomendacao,
                    contraAmeacas
                );
                respostas.merge(
                    resposta.getNome(),
                    bruta,
                    (atual, nova) -> nova.pontuacao() > atual.pontuacao()
                        ? nova
                        : atual
                );
            }
        }

        List<RespostaBruta> ordenadas = respostas.values().stream()
            .sorted(
                Comparator.comparingInt(RespostaBruta::pontuacao)
                    .reversed()
                    .thenComparing(
                        Comparator.comparingInt(RespostaBruta::impacto)
                            .reversed()
                    )
                    .thenComparing(resposta -> resposta.heroi().getNome())
            )
            .limit(LIMITE_RESPOSTAS)
            .toList();
        if (ordenadas.isEmpty()) {
            return ProjecaoPickResponse.vazia(
                candidato.heroi(),
                candidato.pontuacaoFinal()
            );
        }

        int maiorPontuacao = ordenadas.getFirst().pontuacao();
        List<RespostaInimigaProjetadaResponse> projetadas = ordenadas.stream()
            .map(resposta -> respostaPublica(resposta, maiorPontuacao))
            .toList();
        int maiorImpacto = projetadas.stream()
            .mapToInt(
                RespostaInimigaProjetadaResponse::impactoContraNossaComposicao
            )
            .max()
            .orElse(0);
        int penalidadePiorCaso = Math.max(0, (maiorImpacto - 42) / 2);
        int piorCenario = limitar(
            candidato.pontuacaoFinal() - penalidadePiorCaso,
            0,
            100
        );
        int segurancaBlind = candidato.perfilBlindPick() == null
            ? candidato.piorCenario()
            : candidato.perfilBlindPick().segurancaBlind();
        int robustez = limitar((int) Math.round(
            piorCenario * 0.55
                + segurancaBlind * 0.30
                + candidato.coberturaHipoteses() * 0.15
        ), 0, 100);
        int ajuste = limitar(
            Math.round((robustez - 58) / 5.0f),
            -10,
            8
        );
        RespostaInimigaProjetadaResponse piorResposta = projetadas.stream()
            .max(Comparator.comparingInt(
                RespostaInimigaProjetadaResponse::impactoContraNossaComposicao
            ))
            .orElse(projetadas.getFirst());
        String resumo = "Após " + candidato.heroi()
            + ", a resposta mais perigosa projetada é "
            + piorResposta.heroi() + " na rota " + piorResposta.rota()
            + ", reduzindo o cenário estimado para " + piorCenario
            + "/100. A probabilidade é heurística e considera funções abertas, encaixe e capacidade de counter.";

        return new ProjecaoPickResponse(
            candidato.heroi(),
            robustez,
            piorCenario,
            ajuste,
            projetadas,
            resumo
        );
    }

    private Map<String, RecomendacaoDnaResponse> recomendacoesInimigas(
        List<String> inimigos,
        List<String> aliadosProjetados,
        Rota rota
    ) {
        if (inimigos.isEmpty()) {
            return Map.of();
        }
        Map<String, RecomendacaoDnaResponse> resultado =
            new LinkedHashMap<>();
        composicaoService.recomendar(
            inimigos,
            aliadosProjetados,
            rota,
            12
        ).forEach(recomendacao -> resultado.put(
            recomendacao.heroi(),
            recomendacao
        ));
        return resultado;
    }

    private int pontuacaoResposta(
        Heroi resposta,
        RecomendacaoDnaResponse recomendacao,
        AjusteRespostaAmeacaResponse contraAmeacas,
        int frequenciaRota,
        int totalFrequencia
    ) {
        int base = recomendacao == null
            ? 45 + valorTier(resposta.getDadosMeta().tier()) / 10
            : recomendacao.pontuacao();
        int bonusAmeaca = Math.max(
            contraAmeacas.bonus(),
            recomendacao == null ? 0 : recomendacao.bonusRespostaAmeaca()
        );
        int frequencia = totalFrequencia == 0
            ? 0
            : (int) Math.round(frequenciaRota * 100.0 / totalFrequencia);
        return limitar(
            base + bonusAmeaca / 2 + Math.min(8, frequencia / 12),
            0,
            100
        );
    }

    private int impactoResposta(
        int pontuacao,
        RecomendacaoDnaResponse recomendacao,
        AjusteRespostaAmeacaResponse contraAmeacas
    ) {
        int bonusAmeaca = Math.max(
            contraAmeacas.bonus(),
            recomendacao == null ? 0 : recomendacao.bonusRespostaAmeaca()
        );
        int sinergia = recomendacao == null
            ? 0
            : recomendacao.bonusSinergiaGrupo();
        int antiSinergia = recomendacao == null
            ? 0
            : recomendacao.penalidadeAntiSinergia();
        return limitar(
            28
                + Math.max(0, pontuacao - 45)
                + bonusAmeaca * 2
                + sinergia / 2
                - antiSinergia / 2,
            0,
            100
        );
    }

    private RespostaInimigaProjetadaResponse respostaPublica(
        RespostaBruta resposta,
        int maiorPontuacao
    ) {
        int frequencia = resposta.totalFrequencia() == 0
            ? 0
            : (int) Math.round(
                resposta.frequenciaRota() * 100.0
                    / resposta.totalFrequencia()
            );
        int distanciaTopo = Math.max(
            0,
            maiorPontuacao - resposta.pontuacao()
        );
        int probabilidade = limitar(
            82 - distanciaTopo * 2 + frequencia / 6,
            25,
            95
        );
        List<String> motivos = new ArrayList<>();
        motivos.add(
            "A rota " + resposta.rota() + " permanece aberta em "
                + frequencia + "% das hipóteses inimigas."
        );
        if (!resposta.contraAmeacas().alvosRespondidos().isEmpty()) {
            motivos.add(
                "A escolha responde diretamente a "
                    + resposta.contraAmeacas().alvosRespondidos() + "."
            );
        }
        if (resposta.recomendacao() != null) {
            motivos.addAll(
                resposta.recomendacao().motivos().stream().limit(2).toList()
            );
            if (!resposta.recomendacao().explora().isEmpty()) {
                motivos.add(
                    "Explora estas dimensões da nossa composição: "
                        + resposta.recomendacao().explora() + "."
                );
            }
        }

        return new RespostaInimigaProjetadaResponse(
            resposta.heroi().getNome(),
            resposta.rota(),
            probabilidade,
            resposta.impacto(),
            resposta.pontuacao(),
            motivos.stream().distinct().limit(5).toList()
        );
    }

    private Map<Rota, Integer> rotasAbertasInimigas(
        RecomendacaoProximoPickRequest request
    ) {
        InferenciaFuncoesResponse inferencia = inferenciaFuncoesService.inferir(
            new InferenciaFuncoesRequest(
                request.picksAzul(),
                request.picksVermelho()
            )
        );
        InferenciaEquipeResponse inimiga = request.meuLado() == LadoDraft.VERMELHO
            ? inferencia.equipeAzul()
            : inferencia.equipeVermelha();
        Map<Rota, Integer> frequencias = new EnumMap<>(Rota.class);
        for (HipoteseFuncaoResponse hipotese : inimiga.hipoteses()) {
            for (Rota rota : hipotese.rotasAbertas()) {
                frequencias.merge(rota, 1, Integer::sum);
            }
        }
        return frequencias;
    }

    private Set<Long> idsIndisponiveis(
        RecomendacaoProximoPickRequest request
    ) {
        Set<Long> ids = new HashSet<>();
        ids.addAll(request.bansAzul());
        ids.addAll(request.bansVermelho());
        request.picksAzul().stream()
            .map(PickSemFuncaoRequest::heroiId)
            .forEach(ids::add);
        request.picksVermelho().stream()
            .map(PickSemFuncaoRequest::heroiId)
            .forEach(ids::add);
        return ids;
    }

    private List<PickSemFuncaoRequest> picksInimigos(
        RecomendacaoProximoPickRequest request
    ) {
        if (request.meuLado() == LadoDraft.VERMELHO) {
            return request.picksAzul();
        }
        return request.picksVermelho();
    }

    private int valorTier(TierMeta tier) {
        return switch (tier) {
            case S -> 100;
            case A -> 85;
            case B -> 70;
            case C -> 55;
            case D -> 40;
            case NAO_CLASSIFICADO -> 60;
        };
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private record RespostaBruta(
        Heroi heroi,
        Rota rota,
        int pontuacao,
        int impacto,
        int frequenciaRota,
        int totalFrequencia,
        RecomendacaoDnaResponse recomendacao,
        AjusteRespostaAmeacaResponse contraAmeacas
    ) {
    }
}
