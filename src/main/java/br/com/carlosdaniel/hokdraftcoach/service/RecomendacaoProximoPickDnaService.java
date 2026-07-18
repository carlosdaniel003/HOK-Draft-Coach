package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteBlindPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.ContextoDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.ExplicacaoRecomendacaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.OpcaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.ProjecaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TipoOpcaoPick;

@Service
public class RecomendacaoProximoPickDnaService {

    private static final int LIMITE_ALTERNATIVAS = 2;
    private static final int LIMITE_CACHE_RECOMENDACOES = 128;

    private final RecomendacaoProximoPickService recomendacaoBase;
    private final HeroiService heroiService;
    private final AnaliseAmeacaComposicaoService dnaComposicaoService;
    private final SegurancaBlindPickService segurancaBlindPickService;
    private final ProjecaoRespostaInimigaService projecaoService;
    private final ExplicacaoRecomendacaoService explicacaoService;
    private final ConcurrentMap<
        RecomendacaoProximoPickRequest,
        RecomendacaoProximoPickResponse
    > cacheRecomendacoes = new ConcurrentHashMap<>();

    public RecomendacaoProximoPickDnaService(
        RecomendacaoProximoPickService recomendacaoBase,
        HeroiService heroiService,
        AnaliseAmeacaComposicaoService dnaComposicaoService,
        SegurancaBlindPickService segurancaBlindPickService,
        ProjecaoRespostaInimigaService projecaoService,
        ExplicacaoRecomendacaoService explicacaoService
    ) {
        this.recomendacaoBase = recomendacaoBase;
        this.heroiService = heroiService;
        this.dnaComposicaoService = dnaComposicaoService;
        this.segurancaBlindPickService = segurancaBlindPickService;
        this.projecaoService = projecaoService;
        this.explicacaoService = explicacaoService;
    }

    public RecomendacaoProximoPickResponse recomendar(
        RecomendacaoProximoPickRequest request
    ) {
        RecomendacaoProximoPickRequest chave = copiarRequest(request);
        if (
            cacheRecomendacoes.size() >= LIMITE_CACHE_RECOMENDACOES
                && !cacheRecomendacoes.containsKey(chave)
        ) {
            cacheRecomendacoes.clear();
        }
        return cacheRecomendacoes.computeIfAbsent(
            chave,
            this::calcularRecomendacao
        );
    }

    private RecomendacaoProximoPickResponse calcularRecomendacao(
        RecomendacaoProximoPickRequest request
    ) {
        RecomendacaoProximoPickRequest requestAlvo =
            requestParaProximoAliado(request);
        List<String> aliados = nomes(picksAliados(request));
        List<String> inimigos = nomes(picksInimigos(request));
        ContextoDraftResponse contexto = segurancaBlindPickService.contexto(
            requestAlvo
        );
        DiagnosticoComposicaoResponse diagnostico = aliados.isEmpty()
            ? null
            : dnaComposicaoService.diagnosticar(aliados, inimigos);

        RecomendacaoProximoPickResponse base = recomendacaoBase.recomendar(
            request
        );
        if (base.recomendacaoPrincipal() == null) {
            return anexarDiagnostico(base, diagnostico, contexto);
        }

        List<RecomendacaoPickResponse> candidatas = new ArrayList<>();
        candidatas.add(base.recomendacaoPrincipal());
        candidatas.addAll(base.alternativas());

        Map<Rota, Map<String, RecomendacaoDnaResponse>> dnaPorRota =
            aliados.isEmpty()
                ? Map.of()
                : carregarDnaPorRota(candidatas, aliados, inimigos);

        List<CandidatoAvaliado> avaliadas = candidatas.stream()
            .map(recomendacao -> ajustarBase(
                recomendacao,
                dnaPorRota,
                requestAlvo
            ))
            .map(candidato -> projetar(
                candidato,
                request,
                aliados,
                inimigos
            ))
            .sorted(
                Comparator.comparingInt(
                    (CandidatoAvaliado candidato) ->
                        candidato.pick().pontuacaoFinal()
                )
                .reversed()
                .thenComparing(
                    Comparator.comparingInt(
                        (CandidatoAvaliado candidato) ->
                            candidato.projecao().robustez()
                    ).reversed()
                )
                .thenComparing(candidato -> candidato.pick().heroi())
            )
            .toList();

        List<OpcaoPickResponse> opcoes = selecionarOpcoes(
            avaliadas,
            diagnostico
        );
        RecomendacaoPickResponse principal = opcoes.getFirst().escolha();
        List<RecomendacaoPickResponse> alternativas = opcoes.stream()
            .skip(1)
            .map(OpcaoPickResponse::escolha)
            .distinct()
            .limit(LIMITE_ALTERNATIVAS)
            .toList();

        String mensagem = switch (base.estadoDraft()) {
            case "MINHA_VEZ" ->
                "É sua vez. A melhor escolha geral é "
                    + principal.heroi()
                    + "; a resposta também inclui a opção mais segura e a de maior impacto.";
            case "VEZ_ALIADA" ->
                "É a vez da sua equipe. " + principal.heroi()
                    + " é a melhor escolha geral para o próximo pick aliado.";
            case "AGUARDANDO_INIMIGO" ->
                "O inimigo está escolhendo. " + principal.heroi()
                    + " lidera o planejamento para a próxima resposta aliada.";
            default -> base.mensagem();
        };
        List<String> avisos = new ArrayList<>(base.avisos());
        avisos.add(
            "DNA, curva de poder, sinergias, anti-sinergias, ameaças, ordem do draft e respostas inimigas projetadas foram avaliados."
        );
        avisos.add("Prioridade do momento: " + contexto.prioridade());
        avisos.add(
            "As probabilidades de resposta inimiga são heurísticas, baseadas em funções abertas e encaixe estratégico; não representam frequência estatística real."
        );

        return new RecomendacaoProximoPickResponse(
            base.versaoMotor(),
            base.estadoDraft(),
            mensagem,
            base.ehMinhaVez(),
            base.meuSlot(),
            base.proximoLado(),
            base.proximosSlots(),
            base.hipotesesAliadas(),
            base.hipotesesInimigas(),
            base.confiancaFuncoesAliadas(),
            base.confiancaFuncoesInimigas(),
            contexto,
            diagnostico,
            principal,
            alternativas,
            opcoes,
            avisos.stream().distinct().toList()
        );
    }

    private Map<Rota, Map<String, RecomendacaoDnaResponse>> carregarDnaPorRota(
        List<RecomendacaoPickResponse> candidatas,
        List<String> aliados,
        List<String> inimigos
    ) {
        Map<Rota, Map<String, RecomendacaoDnaResponse>> resultado =
            new EnumMap<>(Rota.class);

        candidatas.stream()
            .flatMap(candidata -> candidata.rotasRecomendadas().stream())
            .distinct()
            .forEach(rota -> {
                Map<String, RecomendacaoDnaResponse> porHeroi =
                    new LinkedHashMap<>();
                dnaComposicaoService.recomendar(
                    aliados,
                    inimigos,
                    rota,
                    50
                ).forEach(resposta -> porHeroi.put(resposta.heroi(), resposta));
                resultado.put(rota, porHeroi);
            });

        return resultado;
    }

    private CandidatoAvaliado ajustarBase(
        RecomendacaoPickResponse base,
        Map<Rota, Map<String, RecomendacaoDnaResponse>> dnaPorRota,
        RecomendacaoProximoPickRequest request
    ) {
        RecomendacaoDnaResponse dna = buscarDna(base, dnaPorRota);
        Heroi candidato = heroiService.buscarPorNome(base.heroi())
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + base.heroi() + "."
            ));
        AjusteBlindPickResponse blind = segurancaBlindPickService.avaliar(
            request,
            candidato,
            base,
            dna
        );

        int componenteDna = dna == null
            ? 0
            : limitar(
                (int) Math.round((dna.pontuacao() - 50) / 3.0),
                -5,
                15
            );
        int ajusteTotal = componenteDna + blind.ajuste();
        Map<String, Integer> componentes = new LinkedHashMap<>(
            base.componentes()
        );
        componentes.put("dnaComposicao", componenteDna);
        componentes.put("ajusteOrdemDraft", blind.ajuste());
        componentes.put(
            "segurancaBlindScore",
            blind.perfil().segurancaBlind()
        );
        componentes.put(
            "especificidadeDraft",
            blind.perfil().especificidade()
        );
        if (dna != null) {
            componentes.put("curvaTemporal", dna.ajusteTemporal());
            componentes.put("sinergiaGrupo", dna.bonusSinergiaGrupo());
            componentes.put("antiSinergia", -dna.penalidadeAntiSinergia());
            componentes.put("respostaAmeaca", dna.bonusRespostaAmeaca());
        }

        List<String> motivos = new ArrayList<>(base.motivos());
        List<String> riscos = new ArrayList<>(base.riscos());
        if (dna != null) {
            if (!dna.corrige().isEmpty()) {
                motivos.add("Corrige déficits do DNA: " + dna.corrige() + ".");
            }
            if (!dna.explora().isEmpty()) {
                motivos.add("Explora o DNA inimigo: " + dna.explora() + ".");
            }
            if (!dna.alvosAmeacaRespondidos().isEmpty()) {
                motivos.add(
                    "Neutraliza alvos estratégicos: "
                        + dna.alvosAmeacaRespondidos() + "."
                );
            }
            motivos.addAll(dna.motivos().stream().limit(5).toList());
            if (dna.pontuacao() < 45) {
                riscos.add("Encaixe fraco com as prioridades atuais do DNA.");
            }
            riscos.addAll(dna.alertas().stream().limit(3).toList());
        }
        motivos.addAll(blind.motivos());
        riscos.addAll(blind.riscos());

        int pontuacaoFinal = limitar(
            base.pontuacaoFinal() + ajusteTotal,
            0,
            100
        );
        int media = limitar(
            base.mediaCenarios() + ajusteTotal,
            0,
            100
        );
        int pior = limitar(
            base.piorCenario() + ajusteTotal,
            0,
            100
        );

        RecomendacaoPickResponse ajustada = new RecomendacaoPickResponse(
            base.heroiId(),
            base.heroi(),
            base.rotasRecomendadas(),
            pontuacaoFinal,
            media,
            pior,
            base.coberturaHipoteses(),
            base.cenariosAvaliados(),
            base.seguranca(),
            base.dificuldade(),
            Map.copyOf(componentes),
            motivos.stream().distinct().limit(12).toList(),
            riscos.stream().distinct().limit(8).toList(),
            blind.perfil(),
            base.dadosValidados()
        );
        return new CandidatoAvaliado(
            ajustada,
            dna,
            ProjecaoPickResponse.vazia(
                ajustada.heroi(),
                ajustada.pontuacaoFinal()
            )
        );
    }

    private CandidatoAvaliado projetar(
        CandidatoAvaliado candidato,
        RecomendacaoProximoPickRequest request,
        List<String> aliados,
        List<String> inimigos
    ) {
        ProjecaoPickResponse projecao = projecaoService.projetar(
            request,
            aliados,
            inimigos,
            candidato.pick(),
            candidato.dna()
        );
        RecomendacaoPickResponse pick = aplicarProjecao(
            candidato.pick(),
            projecao
        );
        return new CandidatoAvaliado(pick, candidato.dna(), projecao);
    }

    private RecomendacaoPickResponse aplicarProjecao(
        RecomendacaoPickResponse base,
        ProjecaoPickResponse projecao
    ) {
        Map<String, Integer> componentes = new LinkedHashMap<>(
            base.componentes()
        );
        componentes.put("ajusteProjecao", projecao.ajusteProjecao());
        componentes.put("robustezProjetada", projecao.robustez());
        componentes.put(
            "piorCenarioProjetado",
            projecao.piorCenarioProjetado()
        );
        List<String> motivos = new ArrayList<>(base.motivos());
        List<String> riscos = new ArrayList<>(base.riscos());
        if (projecao.ajusteProjecao() > 0) {
            motivos.add(
                "A escolha permanece robusta mesmo após as melhores respostas inimigas projetadas."
            );
        }
        if (!projecao.respostasProvaveis().isEmpty()) {
            riscos.add(projecao.resumoPiorCenario());
        }
        int pontuacao = limitar(
            base.pontuacaoFinal() + projecao.ajusteProjecao(),
            0,
            100
        );
        int media = limitar(
            base.mediaCenarios() + projecao.ajusteProjecao(),
            0,
            100
        );
        int pior = Math.min(
            limitar(
                base.piorCenario() + projecao.ajusteProjecao(),
                0,
                100
            ),
            projecao.piorCenarioProjetado()
        );
        return new RecomendacaoPickResponse(
            base.heroiId(),
            base.heroi(),
            base.rotasRecomendadas(),
            pontuacao,
            media,
            pior,
            base.coberturaHipoteses(),
            base.cenariosAvaliados(),
            base.seguranca(),
            base.dificuldade(),
            Map.copyOf(componentes),
            motivos.stream().distinct().limit(13).toList(),
            riscos.stream().distinct().limit(9).toList(),
            base.perfilBlindPick(),
            base.dadosValidados()
        );
    }

    private List<OpcaoPickResponse> selecionarOpcoes(
        List<CandidatoAvaliado> candidatas,
        DiagnosticoComposicaoResponse diagnostico
    ) {
        if (candidatas.isEmpty()) {
            return List.of();
        }
        Set<String> usados = new HashSet<>();
        CandidatoAvaliado geral = selecionar(
            candidatas,
            usados,
            this::pontuacaoGeral
        );
        usados.add(geral.pick().heroi());
        CandidatoAvaliado segura = selecionar(
            candidatas,
            usados,
            this::pontuacaoSegura
        );
        usados.add(segura.pick().heroi());
        CandidatoAvaliado impacto = selecionar(
            candidatas,
            usados,
            this::pontuacaoImpacto
        );

        return List.of(
            criarOpcao(
                TipoOpcaoPick.MELHOR_GERAL,
                "Melhor escolha geral",
                pontuacaoGeral(geral),
                geral,
                diagnostico
            ),
            criarOpcao(
                TipoOpcaoPick.MAIS_SEGURA,
                "Escolha mais segura",
                pontuacaoSegura(segura),
                segura,
                diagnostico
            ),
            criarOpcao(
                TipoOpcaoPick.MAIOR_IMPACTO,
                "Escolha de maior impacto",
                pontuacaoImpacto(impacto),
                impacto,
                diagnostico
            )
        );
    }

    private CandidatoAvaliado selecionar(
        List<CandidatoAvaliado> candidatas,
        Set<String> usados,
        java.util.function.ToIntFunction<CandidatoAvaliado> pontuador
    ) {
        return candidatas.stream()
            .filter(candidato -> !usados.contains(candidato.pick().heroi()))
            .max(Comparator.comparingInt(pontuador))
            .orElseGet(() -> candidatas.stream()
                .max(Comparator.comparingInt(pontuador))
                .orElseThrow());
    }

    private OpcaoPickResponse criarOpcao(
        TipoOpcaoPick tipo,
        String titulo,
        int pontuacao,
        CandidatoAvaliado candidato,
        DiagnosticoComposicaoResponse diagnostico
    ) {
        ExplicacaoRecomendacaoResponse explicacao =
            explicacaoService.explicar(
                tipo,
                candidato.pick(),
                candidato.dna(),
                diagnostico,
                candidato.projecao()
            );
        return new OpcaoPickResponse(
            tipo,
            titulo,
            limitar(pontuacao, 0, 100),
            candidato.pick(),
            candidato.projecao(),
            explicacao
        );
    }

    private int pontuacaoGeral(CandidatoAvaliado candidato) {
        return limitar((int) Math.round(
            candidato.pick().pontuacaoFinal() * 0.55
                + candidato.projecao().robustez() * 0.25
                + candidato.projecao().piorCenarioProjetado() * 0.20
        ), 0, 100);
    }

    private int pontuacaoSegura(CandidatoAvaliado candidato) {
        int blind = candidato.pick().perfilBlindPick() == null
            ? candidato.pick().piorCenario()
            : candidato.pick().perfilBlindPick().segurancaBlind();
        return limitar((int) Math.round(
            blind * 0.35
                + candidato.projecao().robustez() * 0.35
                + candidato.pick().coberturaHipoteses() * 0.15
                + candidato.projecao().piorCenarioProjetado() * 0.15
        ), 0, 100);
    }

    private int pontuacaoImpacto(CandidatoAvaliado candidato) {
        int especificidade = candidato.pick().perfilBlindPick() == null
            ? 50
            : candidato.pick().perfilBlindPick().especificidade();
        int respostaAmeaca = candidato.dna() == null
            ? 0
            : Math.min(100, candidato.dna().bonusRespostaAmeaca() * 5);
        int sinergia = candidato.dna() == null
            ? 0
            : Math.min(100, candidato.dna().bonusSinergiaGrupo() * 6);
        int confronto = Math.min(
            100,
            Math.max(
                0,
                candidato.pick().componentes().getOrDefault("confronto", 0)
            ) * 6
        );
        return limitar((int) Math.round(
            candidato.pick().pontuacaoFinal() * 0.30
                + especificidade * 0.25
                + respostaAmeaca * 0.25
                + Math.max(sinergia, confronto) * 0.20
        ), 0, 100);
    }

    private RecomendacaoDnaResponse buscarDna(
        RecomendacaoPickResponse pick,
        Map<Rota, Map<String, RecomendacaoDnaResponse>> dnaPorRota
    ) {
        return pick.rotasRecomendadas().stream()
            .map(rota -> dnaPorRota
                .getOrDefault(rota, Map.of())
                .get(pick.heroi()))
            .filter(resposta -> resposta != null)
            .max(Comparator.comparingInt(RecomendacaoDnaResponse::pontuacao))
            .orElse(null);
    }

    private RecomendacaoProximoPickResponse anexarDiagnostico(
        RecomendacaoProximoPickResponse base,
        DiagnosticoComposicaoResponse diagnostico,
        ContextoDraftResponse contexto
    ) {
        return new RecomendacaoProximoPickResponse(
            base.versaoMotor(),
            base.estadoDraft(),
            base.mensagem(),
            base.ehMinhaVez(),
            base.meuSlot(),
            base.proximoLado(),
            base.proximosSlots(),
            base.hipotesesAliadas(),
            base.hipotesesInimigas(),
            base.confiancaFuncoesAliadas(),
            base.confiancaFuncoesInimigas(),
            contexto,
            diagnostico,
            base.recomendacaoPrincipal(),
            base.alternativas(),
            List.of(),
            base.avisos()
        );
    }

    private RecomendacaoProximoPickRequest copiarRequest(
        RecomendacaoProximoPickRequest request
    ) {
        return new RecomendacaoProximoPickRequest(
            request.meuLado(),
            request.minhaOrdem(),
            List.copyOf(request.bansAzul()),
            List.copyOf(request.bansVermelho()),
            List.copyOf(request.picksAzul()),
            List.copyOf(request.picksVermelho()),
            List.copyOf(request.funcoesPreferidas()),
            List.copyOf(request.funcoesAliadas())
        );
    }

    private RecomendacaoProximoPickRequest requestParaProximoAliado(
        RecomendacaoProximoPickRequest request
    ) {
        if (request.meuLado() == null) {
            return request;
        }

        List<PickSemFuncaoRequest> aliados = picksAliados(request);
        Integer ordemAlvo = null;
        for (int ordem = 1; ordem <= 5; ordem += 1) {
            int ordemAtual = ordem;
            boolean preenchido = aliados.stream()
                .anyMatch(pick -> pick.ordem().equals(ordemAtual));
            if (!preenchido) {
                ordemAlvo = ordem;
                break;
            }
        }

        if (ordemAlvo == null) {
            return request;
        }

        Integer ordemAlvoFinal = ordemAlvo;
        return new RecomendacaoProximoPickRequest(
            request.meuLado(),
            ordemAlvoFinal,
            request.bansAzul(),
            request.bansVermelho(),
            request.picksAzul(),
            request.picksVermelho(),
            request.funcoesDaOrdem(ordemAlvoFinal),
            request.funcoesAliadas().stream()
                .filter(item -> !ordemAlvoFinal.equals(item.ordem()))
                .toList()
        );
    }

    private List<PickSemFuncaoRequest> picksAliados(
        RecomendacaoProximoPickRequest request
    ) {
        if (request.meuLado() == null || request.meuLado() == LadoDraft.AZUL) {
            return request.picksAzul();
        }
        return request.picksVermelho();
    }

    private List<PickSemFuncaoRequest> picksInimigos(
        RecomendacaoProximoPickRequest request
    ) {
        if (request.meuLado() == null || request.meuLado() == LadoDraft.AZUL) {
            return request.picksVermelho();
        }
        return request.picksAzul();
    }

    private List<String> nomes(List<PickSemFuncaoRequest> picks) {
        return picks.stream()
            .map(pick -> buscarHeroi(pick.heroiId()).getNome())
            .toList();
    }

    private Heroi buscarHeroi(Long id) {
        return heroiService.buscarPorId(id)
            .orElseThrow(() -> new RegraNegocioException(
                "Herói de ID " + id + " não encontrado."
            ));
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private record CandidatoAvaliado(
        RecomendacaoPickResponse pick,
        RecomendacaoDnaResponse dna,
        ProjecaoPickResponse projecao
    ) {
    }
}
