package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AtribuicaoFuncaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.HipoteseFuncaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaEquipeResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;

@Service
public class RecomendacaoProximoPickService {

    private static final String VERSAO_MOTOR = "PICK-0.1";
    private static final int LIMITE_ALTERNATIVAS = 2;

    private static final List<RodadaPick> RODADAS = List.of(
        new RodadaPick(LadoDraft.AZUL, List.of(1)),
        new RodadaPick(LadoDraft.VERMELHO, List.of(1, 2)),
        new RodadaPick(LadoDraft.AZUL, List.of(2, 3)),
        new RodadaPick(LadoDraft.VERMELHO, List.of(3, 4)),
        new RodadaPick(LadoDraft.AZUL, List.of(4, 5)),
        new RodadaPick(LadoDraft.VERMELHO, List.of(5))
    );

    private final HeroiService heroiService;
    private final InferenciaFuncoesService inferenciaFuncoesService;

    private final Map<String, Integer> confrontos = Map.ofEntries(
        Map.entry(chaveDirecional(6L, 1L), 15),
        Map.entry(chaveDirecional(7L, 1L), 10),
        Map.entry(chaveDirecional(4L, 1L), 5),
        Map.entry(chaveDirecional(2L, 1L), -10),
        Map.entry(chaveDirecional(1L, 2L), 15),
        Map.entry(chaveDirecional(5L, 2L), 10),
        Map.entry(chaveDirecional(3L, 2L), 6),
        Map.entry(chaveDirecional(10L, 9L), 8),
        Map.entry(chaveDirecional(9L, 8L), 5),
        Map.entry(chaveDirecional(11L, 12L), 6),
        Map.entry(chaveDirecional(13L, 11L), 5),
        Map.entry(chaveDirecional(16L, 14L), 8),
        Map.entry(chaveDirecional(15L, 16L), 5),
        Map.entry(chaveDirecional(17L, 19L), 6),
        Map.entry(chaveDirecional(18L, 17L), 4)
    );

    private final Map<String, Integer> sinergias = Map.ofEntries(
        Map.entry(chavePar(2L, 17L), 8),
        Map.entry(chavePar(1L, 19L), 7),
        Map.entry(chavePar(3L, 18L), 8),
        Map.entry(chavePar(4L, 17L), 6),
        Map.entry(chavePar(5L, 17L), 7),
        Map.entry(chavePar(8L, 14L), 5),
        Map.entry(chavePar(10L, 15L), 6),
        Map.entry(chavePar(12L, 19L), 6),
        Map.entry(chavePar(11L, 18L), 5)
    );

    public RecomendacaoProximoPickService(
        HeroiService heroiService,
        InferenciaFuncoesService inferenciaFuncoesService
    ) {
        this.heroiService = heroiService;
        this.inferenciaFuncoesService = inferenciaFuncoesService;
    }

    public RecomendacaoProximoPickResponse recomendar(
        RecomendacaoProximoPickRequest request
    ) {
        validarEstadoInformado(request);

        InferenciaFuncoesResponse inferencia = inferenciaFuncoesService.inferir(
            new InferenciaFuncoesRequest(
                request.picksAzul(),
                request.picksVermelho()
            )
        );

        if (request.meuLado() == null || request.minhaOrdem() == null) {
            return respostaSemRecomendacao(
                "AGUARDANDO_IDENTIFICACAO",
                "Informe seu lado e sua ordem para calcular qual herói você deve pegar.",
                request,
                inferencia,
                null,
                List.of(),
                List.of(
                    "A inferência de funções continua disponível, mas a recomendação principal depende do seu slot."
                )
            );
        }

        int totalBans = request.bansAzul().size()
            + request.bansVermelho().size();

        if (totalBans < 6) {
            return respostaSemRecomendacao(
                "FASE_DE_BANS",
                "Conclua os seis bans antes de calcular o próximo pick.",
                request,
                inferencia,
                null,
                List.of(),
                List.of("Faltam " + (6 - totalBans) + " ban(s).")
            );
        }

        if (slotJaPreenchido(request)) {
            return respostaSemRecomendacao(
                "PICK_JA_REALIZADO",
                "Seu slot já possui um herói registrado.",
                request,
                inferencia,
                null,
                List.of(),
                List.of("Remova o herói do seu slot para recalcular a escolha.")
            );
        }

        EstadoRodada estadoRodada = calcularEstadoRodada(request);

        if (estadoRodada == null) {
            return respostaSemRecomendacao(
                "DRAFT_CONCLUIDO",
                "Todos os picks já foram registrados.",
                request,
                inferencia,
                null,
                List.of(),
                List.of()
            );
        }

        InferenciaEquipeResponse aliados = selecionarEquipe(
            inferencia,
            request.meuLado()
        );
        InferenciaEquipeResponse inimigos = selecionarEquipe(
            inferencia,
            request.meuLado().adversario()
        );

        if (!aliados.composicaoCompativel()) {
            return respostaSemRecomendacao(
                "COMPOSICAO_ALIADA_INCOMPATIVEL",
                "Não existe uma distribuição válida de funções para sua equipe no estado atual.",
                request,
                inferencia,
                estadoRodada.lado(),
                estadoRodada.slots(),
                aliados.avisos()
            );
        }

        Set<Long> indisponiveis = coletarIdsIndisponiveis(request);
        List<Heroi> inimigosSemFuncao = carregarHerois(
            picksDoLado(request, request.meuLado().adversario())
        );

        List<RecomendacaoPickResponse> recomendacoes = heroiService
            .listarTodos()
            .stream()
            .filter(heroi -> !indisponiveis.contains(heroi.getId()))
            .map(heroi -> avaliarCandidato(
                heroi,
                aliados,
                inimigos,
                inimigosSemFuncao,
                request.funcoesPreferidas()
            ))
            .filter(resultado -> resultado != null)
            .sorted(
                Comparator
                    .comparingInt(RecomendacaoPickResponse::pontuacaoFinal)
                    .reversed()
                    .thenComparingInt(RecomendacaoPickResponse::piorCenario)
                    .reversed()
                    .thenComparing(RecomendacaoPickResponse::heroi)
            )
            .toList();

        if (recomendacoes.isEmpty()) {
            return respostaSemRecomendacao(
                "SEM_CANDIDATOS",
                "Nenhum herói disponível consegue preencher as funções abertas nas hipóteses atuais.",
                request,
                inferencia,
                estadoRodada.lado(),
                estadoRodada.slots(),
                List.of(
                    "Revise os picks, bans ou as rotas possíveis cadastradas para os heróis."
                )
            );
        }

        RecomendacaoPickResponse principal = recomendacoes.getFirst();
        List<RecomendacaoPickResponse> alternativas = recomendacoes
            .stream()
            .skip(1)
            .limit(LIMITE_ALTERNATIVAS)
            .toList();

        boolean ehMinhaVez = estadoRodada.lado() == request.meuLado()
            && estadoRodada.ordens().contains(request.minhaOrdem());

        String mensagem = ehMinhaVez
            ? "É sua vez. A melhor escolha calculada agora é "
                + principal.heroi() + "."
            : "Planejamento para seu próximo pick: "
                + principal.heroi()
                + ". A recomendação pode mudar após as escolhas anteriores.";

        List<String> avisos = criarAvisosGerais(
            request,
            aliados,
            inimigos,
            ehMinhaVez
        );

        return new RecomendacaoProximoPickResponse(
            VERSAO_MOTOR,
            ehMinhaVez ? "MINHA_VEZ" : "PLANEJAMENTO",
            mensagem,
            ehMinhaVez,
            meuSlot(request),
            estadoRodada.lado(),
            estadoRodada.slots(),
            aliados.totalHipoteses(),
            inimigos.totalHipoteses(),
            aliados.confiancaMelhorHipotese(),
            inimigos.confiancaMelhorHipotese(),
            principal,
            alternativas,
            avisos
        );
    }

    private RecomendacaoPickResponse avaliarCandidato(
        Heroi candidato,
        InferenciaEquipeResponse aliados,
        InferenciaEquipeResponse inimigos,
        List<Heroi> inimigosSemFuncao,
        List<Rota> funcoesPreferidas
    ) {
        List<HipoteseFuncaoResponse> hipotesesAliadas = aliados.hipoteses();
        List<HipoteseFuncaoResponse> hipotesesInimigas =
            inimigos.hipoteses().isEmpty()
                ? List.of()
                : inimigos.hipoteses();

        List<AvaliacaoCenario> cenarios = new ArrayList<>();
        Map<Rota, Integer> frequenciaRotas = new EnumMap<>(Rota.class);
        int hipotesesAliadasCobertas = 0;

        for (HipoteseFuncaoResponse hipoteseAliada : hipotesesAliadas) {
            List<Rota> rotasValidas = candidato.getRotasPossiveis()
                .stream()
                .filter(hipoteseAliada.rotasAbertas()::contains)
                .toList();

            if (rotasValidas.isEmpty()) {
                continue;
            }

            hipotesesAliadasCobertas++;

            if (hipotesesInimigas.isEmpty()) {
                AvaliacaoCenario melhor = melhorAvaliacaoPorRota(
                    candidato,
                    rotasValidas,
                    hipoteseAliada,
                    null,
                    inimigosSemFuncao,
                    funcoesPreferidas
                );
                cenarios.add(melhor);
                frequenciaRotas.merge(melhor.rota(), 1, Integer::sum);
                continue;
            }

            for (HipoteseFuncaoResponse hipoteseInimiga : hipotesesInimigas) {
                AvaliacaoCenario melhor = melhorAvaliacaoPorRota(
                    candidato,
                    rotasValidas,
                    hipoteseAliada,
                    hipoteseInimiga,
                    inimigosSemFuncao,
                    funcoesPreferidas
                );
                cenarios.add(melhor);
                frequenciaRotas.merge(melhor.rota(), 1, Integer::sum);
            }
        }

        if (cenarios.isEmpty()) {
            return null;
        }

        int totalHipotesesAliadas = Math.max(1, hipotesesAliadas.size());
        int cobertura = (int) Math.round(
            (hipotesesAliadasCobertas * 100.0) / totalHipotesesAliadas
        );
        int media = arredondarMedia(cenarios, AvaliacaoCenario::pontuacao);
        int pior = cenarios.stream()
            .mapToInt(AvaliacaoCenario::pontuacao)
            .min()
            .orElse(0);
        int melhor = cenarios.stream()
            .mapToInt(AvaliacaoCenario::pontuacao)
            .max()
            .orElse(0);
        int pontuacaoFinal = limitar(
            (int) Math.round(
                media * 0.50
                    + pior * 0.35
                    + cobertura * 0.15
            ),
            0,
            100
        );

        Map<String, Integer> componentes = calcularMediaComponentes(cenarios);
        List<Rota> rotasRecomendadas = ordenarRotasRecomendadas(
            candidato,
            frequenciaRotas
        );
        String seguranca = calcularSeguranca(
            cobertura,
            pior,
            melhor - pior
        );
        List<String> motivos = criarMotivos(
            candidato,
            rotasRecomendadas,
            cobertura,
            media,
            pior,
            componentes,
            funcoesPreferidas
        );
        List<String> riscos = criarRiscos(
            candidato,
            rotasRecomendadas,
            cobertura,
            pior,
            melhor - pior
        );

        return new RecomendacaoPickResponse(
            candidato.getId(),
            candidato.getNome(),
            rotasRecomendadas,
            pontuacaoFinal,
            media,
            pior,
            cobertura,
            cenarios.size(),
            seguranca,
            candidato.getDificuldade(),
            componentes,
            motivos,
            riscos,
            false
        );
    }

    private AvaliacaoCenario melhorAvaliacaoPorRota(
        Heroi candidato,
        List<Rota> rotasValidas,
        HipoteseFuncaoResponse hipoteseAliada,
        HipoteseFuncaoResponse hipoteseInimiga,
        List<Heroi> inimigosSemFuncao,
        List<Rota> funcoesPreferidas
    ) {
        return rotasValidas.stream()
            .map(rota -> avaliarCenario(
                candidato,
                rota,
                hipoteseAliada,
                hipoteseInimiga,
                inimigosSemFuncao,
                funcoesPreferidas,
                rotasValidas.size()
            ))
            .max(
                Comparator
                    .comparingInt(AvaliacaoCenario::pontuacao)
                    .thenComparing(avaliacao ->
                        avaliacao.rota() == candidato.getRota()
                    )
            )
            .orElseThrow();
    }

    private AvaliacaoCenario avaliarCenario(
        Heroi candidato,
        Rota rota,
        HipoteseFuncaoResponse hipoteseAliada,
        HipoteseFuncaoResponse hipoteseInimiga,
        List<Heroi> inimigosSemFuncao,
        List<Rota> funcoesPreferidas,
        int totalRotasValidas
    ) {
        List<Heroi> heroisAliados = carregarHerois(
            hipoteseAliada.atribuicoes()
        );
        List<Heroi> heroisInimigos = hipoteseInimiga == null
            ? inimigosSemFuncao
            : carregarHerois(hipoteseInimiga.atribuicoes());
        Heroi adversarioDireto = hipoteseInimiga == null
            ? null
            : buscarHeroiNaRota(hipoteseInimiga, rota);

        int base = 50;
        int afinidadeFuncao = rota == candidato.getRota() ? 6 : 2;
        int confronto = calcularConfronto(
            candidato,
            adversarioDireto
        );
        int sinergia = calcularSinergia(candidato, heroisAliados);
        int composicao = calcularComposicao(candidato, heroisAliados);
        int respostaInimigos = calcularRespostaAosInimigos(
            candidato,
            heroisInimigos
        );
        int preferencia = funcoesPreferidas.isEmpty()
            ? 0
            : funcoesPreferidas.contains(rota) ? 6 : -2;
        int flexibilidade = candidato.isFlex()
            ? totalRotasValidas >= 2 ? 5 : 2
            : 0;
        int acessibilidade = calcularAcessibilidade(candidato);

        Map<String, Integer> componentes = new LinkedHashMap<>();
        componentes.put("base", base);
        componentes.put("afinidadeFuncao", afinidadeFuncao);
        componentes.put("confronto", confronto);
        componentes.put("sinergia", sinergia);
        componentes.put("composicao", composicao);
        componentes.put("respostaAosInimigos", respostaInimigos);
        componentes.put("preferencia", preferencia);
        componentes.put("flexibilidade", flexibilidade);
        componentes.put("acessibilidade", acessibilidade);

        int pontuacao = componentes.values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();

        return new AvaliacaoCenario(
            rota,
            limitar(pontuacao, 0, 100),
            componentes
        );
    }

    private int calcularConfronto(Heroi candidato, Heroi adversario) {
        if (adversario == null) {
            return 0;
        }

        int pontuacao = confrontos.getOrDefault(
            chaveDirecional(candidato.getId(), adversario.getId()),
            0
        );
        AtributosHeroi candidatoAtributos = candidato.getAtributos();
        AtributosHeroi inimigoAtributos = adversario.getAtributos();

        if (
            candidatoAtributos.mobilidade()
                >= inimigoAtributos.mobilidade() + 3
        ) {
            pontuacao += 4;
        }

        if (
            candidatoAtributos.alcance()
                >= inimigoAtributos.alcance() + 3
        ) {
            pontuacao += 4;
        }

        if (
            candidatoAtributos.resistencia() >= 7
                && inimigoAtributos.danoExplosivo() >= 8
        ) {
            pontuacao += 4;
        }

        if (
            candidatoAtributos.controle() >= 7
                && inimigoAtributos.mobilidade() >= 7
        ) {
            pontuacao += 4;
        }

        if (
            candidatoAtributos.danoSustentado() >= 8
                && inimigoAtributos.resistencia() >= 7
        ) {
            pontuacao += 4;
        }

        if (
            inimigoAtributos.controle() >= 8
                && candidatoAtributos.mobilidade() <= 3
        ) {
            pontuacao -= 4;
        }

        return limitar(pontuacao, -15, 15);
    }

    private int calcularSinergia(
        Heroi candidato,
        List<Heroi> aliados
    ) {
        int pontuacao = 0;

        for (Heroi aliado : aliados) {
            int bonus = sinergias.getOrDefault(
                chavePar(candidato.getId(), aliado.getId()),
                0
            );

            if (bonus > 0) {
                pontuacao += bonus;
                continue;
            }

            if (
                candidato.getAtributos().controle() >= 7
                    && aliado.getAtributos().danoSustentado() >= 8
            ) {
                pontuacao += 2;
            }

            if (
                candidato.getAtributos().resistencia() >= 8
                    && aliado.getAtributos().danoExplosivo() >= 8
            ) {
                pontuacao += 2;
            }

            if (
                candidato.getAtributos().danoSustentado() >= 8
                    && aliado.getAtributos().controle() >= 7
            ) {
                pontuacao += 2;
            }
        }

        return limitar(pontuacao, 0, 12);
    }

    private int calcularComposicao(
        Heroi candidato,
        List<Heroi> aliados
    ) {
        if (aliados.isEmpty()) {
            return 0;
        }

        int pontuacao = 0;
        boolean temDanoMagico = aliados.stream()
            .anyMatch(heroi -> heroi.getTipoDano() != TipoDano.FISICO);
        boolean temDanoFisico = aliados.stream()
            .anyMatch(heroi -> heroi.getTipoDano() != TipoDano.MAGICO);

        if (
            !temDanoMagico
                && candidato.getTipoDano() != TipoDano.FISICO
        ) {
            pontuacao += 4;
        }

        if (
            !temDanoFisico
                && candidato.getTipoDano() != TipoDano.MAGICO
        ) {
            pontuacao += 4;
        }

        if (
            mediaAtributo(aliados, heroi ->
                heroi.getAtributos().controle()
            ) < 4
                && candidato.getAtributos().controle() >= 7
        ) {
            pontuacao += 5;
        }

        if (
            mediaAtributo(aliados, heroi ->
                heroi.getAtributos().resistencia()
            ) < 4
                && candidato.getAtributos().resistencia() >= 7
        ) {
            pontuacao += 4;
        }

        if (
            mediaAtributo(aliados, heroi ->
                heroi.getAtributos().danoSustentado()
            ) < 5
                && candidato.getAtributos().danoSustentado() >= 8
        ) {
            pontuacao += 3;
        }

        return limitar(pontuacao, 0, 15);
    }

    private int calcularRespostaAosInimigos(
        Heroi candidato,
        List<Heroi> inimigos
    ) {
        if (inimigos.isEmpty()) {
            return 0;
        }

        int pontuacao = 0;
        double mobilidade = mediaAtributo(inimigos, heroi ->
            heroi.getAtributos().mobilidade()
        );
        double resistencia = mediaAtributo(inimigos, heroi ->
            heroi.getAtributos().resistencia()
        );
        double alcance = mediaAtributo(inimigos, heroi ->
            heroi.getAtributos().alcance()
        );
        long ameacasExplosivas = inimigos.stream()
            .filter(heroi -> heroi.getAtributos().danoExplosivo() >= 8)
            .count();

        if (mobilidade >= 7 && candidato.getAtributos().controle() >= 7) {
            pontuacao += 4;
        }

        if (
            resistencia >= 7
                && candidato.getAtributos().danoSustentado() >= 8
        ) {
            pontuacao += 4;
        }

        if (alcance >= 7 && candidato.getAtributos().mobilidade() >= 7) {
            pontuacao += 3;
        }

        if (
            ameacasExplosivas >= 2
                && candidato.getAtributos().resistencia() >= 7
        ) {
            pontuacao += 3;
        }

        return limitar(pontuacao, 0, 12);
    }

    private int calcularAcessibilidade(Heroi candidato) {
        return switch (candidato.getDificuldade()) {
            case 1 -> 4;
            case 2 -> 2;
            case 3 -> 0;
            case 4 -> -2;
            default -> -4;
        };
    }

    private Map<String, Integer> calcularMediaComponentes(
        List<AvaliacaoCenario> cenarios
    ) {
        Map<String, Integer> medias = new LinkedHashMap<>();
        Set<String> nomes = new LinkedHashSet<>();

        cenarios.forEach(cenario ->
            nomes.addAll(cenario.componentes().keySet())
        );

        for (String nome : nomes) {
            int media = (int) Math.round(
                cenarios.stream()
                    .mapToInt(cenario ->
                        cenario.componentes().getOrDefault(nome, 0)
                    )
                    .average()
                    .orElse(0)
            );
            medias.put(nome, media);
        }

        return medias;
    }

    private List<Rota> ordenarRotasRecomendadas(
        Heroi candidato,
        Map<Rota, Integer> frequenciaRotas
    ) {
        return frequenciaRotas.entrySet()
            .stream()
            .sorted(
                Comparator
                    .<Map.Entry<Rota, Integer>>comparingInt(Map.Entry::getValue)
                    .reversed()
                    .thenComparing(entry ->
                        entry.getKey() == candidato.getRota() ? 0 : 1
                    )
                    .thenComparing(entry -> entry.getKey().name())
            )
            .map(Map.Entry::getKey)
            .toList();
    }

    private List<String> criarMotivos(
        Heroi candidato,
        List<Rota> rotas,
        int cobertura,
        int media,
        int pior,
        Map<String, Integer> componentes,
        List<Rota> funcoesPreferidas
    ) {
        List<String> motivos = new ArrayList<>();

        if (cobertura == 100) {
            motivos.add(
                "Permanece utilizável em todas as hipóteses atuais da sua equipe."
            );
        } else {
            motivos.add(
                "É compatível com " + cobertura
                    + "% das hipóteses atuais da sua equipe."
            );
        }

        motivos.add(
            "Melhor função provável: " + rotas.getFirst() + "."
        );
        motivos.add(
            "Média de " + media + "/100 e pior cenário de "
                + pior + "/100."
        );

        if (componentes.getOrDefault("confronto", 0) >= 4) {
            motivos.add(
                "Apresenta vantagem de confronto nas distribuições inimigas mais prováveis."
            );
        }

        if (componentes.getOrDefault("composicao", 0) >= 4) {
            motivos.add("Supre uma necessidade da composição aliada.");
        }

        if (componentes.getOrDefault("sinergia", 0) >= 4) {
            motivos.add("Possui boa sinergia com os aliados já escolhidos.");
        }

        if (componentes.getOrDefault("respostaAosInimigos", 0) >= 4) {
            motivos.add("Responde bem às características gerais dos inimigos.");
        }

        if (componentes.getOrDefault("flexibilidade", 0) >= 4) {
            motivos.add("Mantém mais de uma possibilidade de função aberta.");
        }

        if (
            !funcoesPreferidas.isEmpty()
                && rotas.stream().anyMatch(funcoesPreferidas::contains)
        ) {
            motivos.add("É compatível com uma das suas funções preferidas.");
        }

        if (candidato.getDificuldade() <= 2) {
            motivos.add("Possui execução relativamente acessível.");
        }

        return motivos.stream().distinct().limit(6).toList();
    }

    private List<String> criarRiscos(
        Heroi candidato,
        List<Rota> rotas,
        int cobertura,
        int pior,
        int variacao
    ) {
        List<String> riscos = new ArrayList<>();

        if (cobertura < 100) {
            riscos.add(
                "Não se encaixa em todas as distribuições de função ainda possíveis."
            );
        }

        if (pior < 55) {
            riscos.add("Possui pelo menos um cenário de confronto desfavorável.");
        }

        if (variacao > 15) {
            riscos.add(
                "O desempenho varia bastante conforme as funções inimigas forem reveladas."
            );
        }

        if (candidato.getDificuldade() >= 4) {
            riscos.add("Exige execução mecânica avançada.");
        }

        if (rotas.size() > 1) {
            riscos.add(
                "A função final ainda depende da evolução do draft."
            );
        }

        if (riscos.isEmpty()) {
            riscos.add("Nenhum risco estrutural relevante foi identificado.");
        }

        return riscos.stream().distinct().limit(4).toList();
    }

    private String calcularSeguranca(
        int cobertura,
        int pior,
        int variacao
    ) {
        if (cobertura == 100 && pior >= 65 && variacao <= 12) {
            return "ALTA";
        }

        if (cobertura == 100 && pior >= 55 && variacao <= 20) {
            return "MEDIA";
        }

        if (cobertura >= 70 && pior >= 45) {
            return "SITUACIONAL";
        }

        return "ARRISCADA";
    }

    private List<String> criarAvisosGerais(
        RecomendacaoProximoPickRequest request,
        InferenciaEquipeResponse aliados,
        InferenciaEquipeResponse inimigos,
        boolean ehMinhaVez
    ) {
        List<String> avisos = new ArrayList<>();

        avisos.add(
            "Os dados estratégicos são provisórios e ainda não representam estatísticas oficiais do jogo."
        );

        if (!ehMinhaVez) {
            avisos.add(
                "Esta é uma recomendação preventiva; picks anteriores podem alterar o resultado."
            );
        }

        if (!"ALTA".equals(aliados.confiancaMelhorHipotese())) {
            avisos.add(
                "As funções da sua equipe ainda possuem incerteza: "
                    + aliados.confiancaMelhorHipotese() + "."
            );
        }

        if (!"ALTA".equals(inimigos.confiancaMelhorHipotese())) {
            avisos.add(
                "As funções inimigas ainda possuem incerteza: "
                    + inimigos.confiancaMelhorHipotese() + "."
            );
        }

        if (request.funcoesPreferidas().isEmpty()) {
            avisos.add(
                "Nenhuma função preferida foi informada; a nota não considera conforto pessoal."
            );
        }

        return avisos;
    }

    private void validarEstadoInformado(
        RecomendacaoProximoPickRequest request
    ) {
        if (
            (request.meuLado() == null) != (request.minhaOrdem() == null)
        ) {
            throw new RegraNegocioException(
                "Informe conjuntamente seu lado e sua ordem, ou deixe ambos vazios."
            );
        }

        Set<Long> idsUsados = new HashSet<>();
        validarListaBans(request.bansAzul(), "bans do lado azul", idsUsados);
        validarListaBans(
            request.bansVermelho(),
            "bans do lado vermelho",
            idsUsados
        );

        for (PickSemFuncaoRequest pick : request.picksAzul()) {
            validarPickContraBans(pick, idsUsados);
        }

        for (PickSemFuncaoRequest pick : request.picksVermelho()) {
            validarPickContraBans(pick, idsUsados);
        }
    }

    private void validarListaBans(
        List<Long> ids,
        String descricao,
        Set<Long> idsUsados
    ) {
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new RegraNegocioException(
                    "Existe um ID inválido em " + descricao + "."
                );
            }

            Heroi heroi = buscarHeroi(id);

            if (!idsUsados.add(id)) {
                throw new RegraNegocioException(
                    "O herói " + heroi.getNome()
                        + " foi informado mais de uma vez entre os bans."
                );
            }
        }
    }

    private void validarPickContraBans(
        PickSemFuncaoRequest pick,
        Set<Long> idsBanidos
    ) {
        Heroi heroi = buscarHeroi(pick.heroiId());

        if (idsBanidos.contains(pick.heroiId())) {
            throw new RegraNegocioException(
                "O herói " + heroi.getNome()
                    + " está banido e não pode ser escolhido."
            );
        }
    }

    private EstadoRodada calcularEstadoRodada(
        RecomendacaoProximoPickRequest request
    ) {
        for (RodadaPick rodada : RODADAS) {
            Map<Integer, Long> picks = mapaPicks(
                picksDoLado(request, rodada.lado())
            );
            List<Integer> ordensFaltantes = rodada.ordens()
                .stream()
                .filter(ordem -> !picks.containsKey(ordem))
                .toList();

            if (!ordensFaltantes.isEmpty()) {
                List<String> slots = ordensFaltantes.stream()
                    .map(ordem -> rodada.lado().prefixoSlot() + ordem)
                    .toList();

                return new EstadoRodada(
                    rodada.lado(),
                    ordensFaltantes,
                    slots
                );
            }
        }

        return null;
    }

    private boolean slotJaPreenchido(
        RecomendacaoProximoPickRequest request
    ) {
        return picksDoLado(request, request.meuLado())
            .stream()
            .anyMatch(pick -> pick.ordem().equals(request.minhaOrdem()));
    }

    private Set<Long> coletarIdsIndisponiveis(
        RecomendacaoProximoPickRequest request
    ) {
        Set<Long> ids = new HashSet<>();
        ids.addAll(request.bansAzul());
        ids.addAll(request.bansVermelho());
        request.picksAzul().forEach(pick -> ids.add(pick.heroiId()));
        request.picksVermelho().forEach(pick -> ids.add(pick.heroiId()));
        return ids;
    }

    private List<PickSemFuncaoRequest> picksDoLado(
        RecomendacaoProximoPickRequest request,
        LadoDraft lado
    ) {
        return lado == LadoDraft.AZUL
            ? request.picksAzul()
            : request.picksVermelho();
    }

    private InferenciaEquipeResponse selecionarEquipe(
        InferenciaFuncoesResponse inferencia,
        LadoDraft lado
    ) {
        return lado == LadoDraft.AZUL
            ? inferencia.equipeAzul()
            : inferencia.equipeVermelha();
    }

    private List<Heroi> carregarHerois(
        List<PickSemFuncaoRequest> picks
    ) {
        return picks.stream()
            .map(pick -> buscarHeroi(pick.heroiId()))
            .toList();
    }

    private List<Heroi> carregarHeroisPorAtribuicao(
        List<AtribuicaoFuncaoResponse> atribuicoes
    ) {
        return atribuicoes.stream()
            .map(atribuicao -> buscarHeroi(atribuicao.heroiId()))
            .toList();
    }

    private List<Heroi> carregarHerois(
        List<AtribuicaoFuncaoResponse> atribuicoes,
        boolean marcador
    ) {
        return carregarHeroisPorAtribuicao(atribuicoes);
    }

    private Heroi buscarHeroiNaRota(
        HipoteseFuncaoResponse hipotese,
        Rota rota
    ) {
        return hipotese.atribuicoes()
            .stream()
            .filter(atribuicao -> atribuicao.rota() == rota)
            .findFirst()
            .map(atribuicao -> buscarHeroi(atribuicao.heroiId()))
            .orElse(null);
    }

    private Heroi buscarHeroi(Long id) {
        return heroiService.buscarPorId(id)
            .orElseThrow(() -> new RegraNegocioException(
                "Herói de ID " + id + " não encontrado."
            ));
    }

    private Map<Integer, Long> mapaPicks(
        List<PickSemFuncaoRequest> picks
    ) {
        Map<Integer, Long> mapa = new HashMap<>();
        picks.forEach(pick -> mapa.put(pick.ordem(), pick.heroiId()));
        return mapa;
    }

    private String meuSlot(RecomendacaoProximoPickRequest request) {
        if (request.meuLado() == null || request.minhaOrdem() == null) {
            return null;
        }

        return request.meuLado().prefixoSlot() + request.minhaOrdem();
    }

    private RecomendacaoProximoPickResponse respostaSemRecomendacao(
        String estado,
        String mensagem,
        RecomendacaoProximoPickRequest request,
        InferenciaFuncoesResponse inferencia,
        LadoDraft proximoLado,
        List<String> proximosSlots,
        List<String> avisos
    ) {
        InferenciaEquipeResponse aliados = request.meuLado() == null
            ? inferencia.equipeAzul()
            : selecionarEquipe(inferencia, request.meuLado());
        InferenciaEquipeResponse inimigos = request.meuLado() == null
            ? inferencia.equipeVermelha()
            : selecionarEquipe(inferencia, request.meuLado().adversario());

        return new RecomendacaoProximoPickResponse(
            VERSAO_MOTOR,
            estado,
            mensagem,
            false,
            meuSlot(request),
            proximoLado,
            proximosSlots,
            aliados.totalHipoteses(),
            inimigos.totalHipoteses(),
            aliados.confiancaMelhorHipotese(),
            inimigos.confiancaMelhorHipotese(),
            null,
            List.of(),
            avisos
        );
    }

    private double mediaAtributo(
        List<Heroi> herois,
        ToIntFunction<Heroi> extrator
    ) {
        return herois.stream()
            .mapToInt(extrator)
            .average()
            .orElse(0);
    }

    private int arredondarMedia(
        List<AvaliacaoCenario> cenarios,
        ToIntFunction<AvaliacaoCenario> extrator
    ) {
        return (int) Math.round(
            cenarios.stream()
                .mapToInt(extrator)
                .average()
                .orElse(0)
        );
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private static String chaveDirecional(Long candidatoId, Long inimigoId) {
        return candidatoId + ":" + inimigoId;
    }

    private static String chavePar(Long primeiroId, Long segundoId) {
        long menor = Math.min(primeiroId, segundoId);
        long maior = Math.max(primeiroId, segundoId);
        return menor + ":" + maior;
    }

    private record RodadaPick(
        LadoDraft lado,
        List<Integer> ordens
    ) {
    }

    private record EstadoRodada(
        LadoDraft lado,
        List<Integer> ordens,
        List<String> slots
    ) {
    }

    private record AvaliacaoCenario(
        Rota rota,
        int pontuacao,
        Map<String, Integer> componentes
    ) {
    }
}
