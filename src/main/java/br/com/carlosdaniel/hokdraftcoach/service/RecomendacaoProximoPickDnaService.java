package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteBlindPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.ContextoDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class RecomendacaoProximoPickDnaService {

    private static final int LIMITE_ALTERNATIVAS = 2;

    private final RecomendacaoProximoPickService recomendacaoBase;
    private final HeroiService heroiService;
    private final AnaliseAmeacaComposicaoService dnaComposicaoService;
    private final SegurancaBlindPickService segurancaBlindPickService;

    public RecomendacaoProximoPickDnaService(
        RecomendacaoProximoPickService recomendacaoBase,
        HeroiService heroiService,
        AnaliseAmeacaComposicaoService dnaComposicaoService,
        SegurancaBlindPickService segurancaBlindPickService
    ) {
        this.recomendacaoBase = recomendacaoBase;
        this.heroiService = heroiService;
        this.dnaComposicaoService = dnaComposicaoService;
        this.segurancaBlindPickService = segurancaBlindPickService;
    }

    public RecomendacaoProximoPickResponse recomendar(
        RecomendacaoProximoPickRequest request
    ) {
        List<String> aliados = nomes(picksAliados(request));
        List<String> inimigos = nomes(picksInimigos(request));
        ContextoDraftResponse contexto = segurancaBlindPickService.contexto(
            request
        );
        DiagnosticoComposicaoResponse diagnostico = aliados.isEmpty()
            ? null
            : dnaComposicaoService.diagnosticar(aliados, inimigos);

        RecomendacaoProximoPickResponse base = recomendacaoBase.recomendar(
            request
        );
        if (diagnostico == null || base.recomendacaoPrincipal() == null) {
            return anexarDiagnostico(base, diagnostico, contexto);
        }

        List<RecomendacaoPickResponse> candidatas = new ArrayList<>();
        candidatas.add(base.recomendacaoPrincipal());
        candidatas.addAll(base.alternativas());

        Map<Rota, Map<String, RecomendacaoDnaResponse>> dnaPorRota =
            carregarDnaPorRota(candidatas, aliados, inimigos);

        List<RecomendacaoPickResponse> ajustadas = candidatas.stream()
            .map(recomendacao -> ajustar(
                recomendacao,
                dnaPorRota,
                request
            ))
            .sorted(
                Comparator.comparingInt(
                    RecomendacaoPickResponse::pontuacaoFinal
                )
                .reversed()
                .thenComparing(
                    Comparator.comparingInt(
                        RecomendacaoPickResponse::piorCenario
                    ).reversed()
                )
                .thenComparing(RecomendacaoPickResponse::heroi)
            )
            .toList();

        RecomendacaoPickResponse principal = ajustadas.getFirst();
        List<RecomendacaoPickResponse> alternativas = ajustadas.stream()
            .skip(1)
            .limit(LIMITE_ALTERNATIVAS)
            .toList();
        String mensagem = switch (base.estadoDraft()) {
            case "MINHA_VEZ" ->
                "É sua vez. No contexto " + contexto.momento()
                    + ", a melhor escolha é " + principal.heroi() + ".";
            case "PLANEJAMENTO" ->
                "Planejamento para " + contexto.momento() + ": "
                    + principal.heroi()
                    + ". A recomendação pode mudar após os próximos picks.";
            default -> base.mensagem();
        };
        List<String> avisos = new ArrayList<>(base.avisos());
        avisos.add(
            "DNA, curva de poder, sinergias, anti-sinergias, ameaças e ordem do draft foram avaliados antes da ordenação final."
        );
        avisos.add("Prioridade do momento: " + contexto.prioridade());

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

    private RecomendacaoPickResponse ajustar(
        RecomendacaoPickResponse base,
        Map<Rota, Map<String, RecomendacaoDnaResponse>> dnaPorRota,
        RecomendacaoProximoPickRequest request
    ) {
        RecomendacaoDnaResponse dna = base.rotasRecomendadas()
            .stream()
            .map(rota -> dnaPorRota
                .getOrDefault(rota, Map.of())
                .get(base.heroi()))
            .filter(resposta -> resposta != null)
            .max(Comparator.comparingInt(RecomendacaoDnaResponse::pontuacao))
            .orElse(null);
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

        return new RecomendacaoPickResponse(
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
            base.avisos()
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
}
