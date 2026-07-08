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

import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.EscolhaDraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;

@Service
public class DraftService {

    private static final String VERSAO_DADOS = "ESTUDO-0.1";
    private static final int LIMITE_RECOMENDACOES = 5;

    private final HeroiService heroiService;

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

    public DraftService(HeroiService heroiService) {
        this.heroiService = heroiService;
    }

    public AnaliseDraftResponse recomendar(DraftRequest request) {
        Map<Rota, Heroi> aliados = montarEquipe(
            request.aliados(),
            "equipe aliada"
        );
        Map<Rota, Heroi> inimigos = montarEquipe(
            request.inimigos(),
            "equipe inimiga"
        );

        validarHeroisRepetidos(aliados, inimigos);

        if (aliados.containsKey(request.rotaAlvo())) {
            throw new RegraNegocioException(
                "A rota alvo já possui um herói na equipe aliada."
            );
        }

        Set<Long> idsSelecionados = new HashSet<>();
        aliados.values().forEach(heroi -> idsSelecionados.add(heroi.getId()));
        inimigos.values().forEach(heroi -> idsSelecionados.add(heroi.getId()));

        List<Heroi> candidatos = heroiService
            .listarPorRota(request.rotaAlvo())
            .stream()
            .filter(heroi -> !idsSelecionados.contains(heroi.getId()))
            .toList();

        List<RecomendacaoDraftResponse> recomendacoes = candidatos.stream()
            .map(heroi -> pontuar(
                heroi,
                request.rotaAlvo(),
                aliados,
                inimigos
            ))
            .sorted(
                Comparator
                    .comparingInt(
                        RecomendacaoDraftResponse::pontuacaoFinal
                    )
                    .reversed()
                    .thenComparing(RecomendacaoDraftResponse::nome)
            )
            .limit(LIMITE_RECOMENDACOES)
            .toList();

        List<String> avisos = criarAvisos(
            request.rotaAlvo(),
            inimigos,
            candidatos.size()
        );

        return new AnaliseDraftResponse(
            VERSAO_DADOS,
            request.rotaAlvo(),
            candidatos.size(),
            recomendacoes,
            avisos
        );
    }

    private Map<Rota, Heroi> montarEquipe(
        List<EscolhaDraftRequest> escolhas,
        String nomeEquipe
    ) {
        Map<Rota, Heroi> equipe = new EnumMap<>(Rota.class);

        for (EscolhaDraftRequest escolha : escolhas) {
            Heroi heroi = heroiService.buscarPorId(escolha.heroiId())
                .orElseThrow(() -> new RegraNegocioException(
                    "Herói de ID " + escolha.heroiId() + " não encontrado."
                ));

            if (!heroi.podeJogarNaRota(escolha.rota())) {
                throw new RegraNegocioException(
                    heroi.getNome()
                        + " não está cadastrado para "
                        + escolha.rota()
                        + ". Rotas possíveis: "
                        + heroi.getRotasPossiveis()
                        + "."
                );
            }

            if (equipe.putIfAbsent(escolha.rota(), heroi) != null) {
                throw new RegraNegocioException(
                    "A " + nomeEquipe + " possui mais de um herói em "
                        + escolha.rota() + "."
                );
            }
        }

        return equipe;
    }

    private void validarHeroisRepetidos(
        Map<Rota, Heroi> aliados,
        Map<Rota, Heroi> inimigos
    ) {
        Set<Long> ids = new HashSet<>();

        for (Heroi heroi : aliados.values()) {
            if (!ids.add(heroi.getId())) {
                throw heroiRepetido(heroi);
            }
        }

        for (Heroi heroi : inimigos.values()) {
            if (!ids.add(heroi.getId())) {
                throw heroiRepetido(heroi);
            }
        }
    }

    private RegraNegocioException heroiRepetido(Heroi heroi) {
        return new RegraNegocioException(
            "O herói " + heroi.getNome() + " foi selecionado mais de uma vez."
        );
    }

    private RecomendacaoDraftResponse pontuar(
        Heroi candidato,
        Rota rotaAlvo,
        Map<Rota, Heroi> aliados,
        Map<Rota, Heroi> inimigos
    ) {
        List<String> motivos = new ArrayList<>();
        Heroi adversarioDireto = inimigos.get(rotaAlvo);

        int base = 50;
        int confronto = calcularConfronto(
            candidato,
            adversarioDireto,
            motivos
        );
        int sinergia = calcularSinergia(candidato, aliados, motivos);
        int composicao = calcularComposicao(candidato, aliados, motivos);
        int resposta = calcularRespostaAosInimigos(
            candidato,
            inimigos,
            motivos
        );
        int acessibilidade = calcularAcessibilidade(candidato, motivos);

        int pontuacaoFinal = limitar(
            base + confronto + sinergia + composicao
                + resposta + acessibilidade,
            0,
            100
        );

        Map<String, Integer> componentes = new LinkedHashMap<>();
        componentes.put("base", base);
        componentes.put("confronto", confronto);
        componentes.put("sinergia", sinergia);
        componentes.put("composicao", composicao);
        componentes.put("respostaAosInimigos", resposta);
        componentes.put("acessibilidade", acessibilidade);

        if (motivos.isEmpty()) {
            motivos.add(
                "Escolha compatível com a rota, sem bônus estratégico específico."
            );
        }

        return new RecomendacaoDraftResponse(
            candidato.getId(),
            candidato.getNome(),
            rotaAlvo,
            pontuacaoFinal,
            calcularNivel(pontuacaoFinal),
            componentes,
            motivos,
            false
        );
    }

    private int calcularConfronto(
        Heroi candidato,
        Heroi adversario,
        List<String> motivos
    ) {
        if (adversario == null) {
            return 0;
        }

        int pontuacao = confrontos.getOrDefault(
            chaveDirecional(candidato.getId(), adversario.getId()),
            0
        );

        if (pontuacao > 0) {
            motivos.add(
                "Vantagem provisória cadastrada contra "
                    + adversario.getNome() + "."
            );
        } else if (pontuacao < 0) {
            motivos.add(
                "Confronto direto provisoriamente desfavorável contra "
                    + adversario.getNome() + "."
            );
        }

        AtributosHeroi atributos = candidato.getAtributos();
        AtributosHeroi atributosInimigo = adversario.getAtributos();

        if (atributos.mobilidade() >= atributosInimigo.mobilidade() + 3) {
            pontuacao += 4;
            motivos.add("Possui vantagem relevante de mobilidade na rota.");
        }

        if (atributos.alcance() >= atributosInimigo.alcance() + 3) {
            pontuacao += 4;
            motivos.add("Pode pressionar o adversário por diferença de alcance.");
        }

        if (
            atributos.resistencia() >= 7
                && atributosInimigo.danoExplosivo() >= 8
        ) {
            pontuacao += 4;
            motivos.add("Tem resistência para absorver a explosão inimiga.");
        }

        if (
            atributosInimigo.controle() >= 8
                && atributos.mobilidade() <= 3
        ) {
            pontuacao -= 4;
            motivos.add("Pode sofrer contra o alto controle do adversário.");
        }

        return limitar(pontuacao, -15, 15);
    }

    private int calcularSinergia(
        Heroi candidato,
        Map<Rota, Heroi> aliados,
        List<String> motivos
    ) {
        int pontuacao = 0;

        for (Heroi aliado : aliados.values()) {
            int bonusCadastrado = sinergias.getOrDefault(
                chavePar(candidato.getId(), aliado.getId()),
                0
            );

            if (bonusCadastrado > 0) {
                pontuacao += bonusCadastrado;
                motivos.add(
                    "Sinergia provisória cadastrada com "
                        + aliado.getNome() + "."
                );
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

        if (pontuacao > 0 && !aliados.isEmpty()) {
            motivos.add("Complementa características já presentes nos aliados.");
        }

        return limitar(pontuacao, 0, 15);
    }

    private int calcularComposicao(
        Heroi candidato,
        Map<Rota, Heroi> aliados,
        List<String> motivos
    ) {
        if (aliados.isEmpty()) {
            return 0;
        }

        int pontuacao = 0;
        double controleMedio = media(aliados, Atributo.CONTROLE);
        double resistenciaMedia = media(aliados, Atributo.RESISTENCIA);
        double danoSustentadoMedio = media(
            aliados,
            Atributo.DANO_SUSTENTADO
        );

        boolean temDanoMagico = aliados.values().stream()
            .anyMatch(heroi -> heroi.getTipoDano() != TipoDano.FISICO);
        boolean temDanoFisico = aliados.values().stream()
            .anyMatch(heroi -> heroi.getTipoDano() != TipoDano.MAGICO);

        if (
            !temDanoMagico
                && candidato.getTipoDano() != TipoDano.FISICO
        ) {
            pontuacao += 6;
            motivos.add("Adiciona dano mágico a uma composição física.");
        }

        if (
            !temDanoFisico
                && candidato.getTipoDano() != TipoDano.MAGICO
        ) {
            pontuacao += 6;
            motivos.add("Adiciona dano físico a uma composição mágica.");
        }

        if (
            controleMedio < 4
                && candidato.getAtributos().controle() >= 7
        ) {
            pontuacao += 6;
            motivos.add("Supre a falta de controle da equipe aliada.");
        }

        if (
            resistenciaMedia < 4
                && candidato.getAtributos().resistencia() >= 7
        ) {
            pontuacao += 5;
            motivos.add("Adiciona linha de frente à composição aliada.");
        }

        if (
            danoSustentadoMedio < 5
                && candidato.getAtributos().danoSustentado() >= 8
        ) {
            pontuacao += 4;
            motivos.add("Melhora o dano sustentado em lutas prolongadas.");
        }

        return limitar(pontuacao, 0, 20);
    }

    private int calcularRespostaAosInimigos(
        Heroi candidato,
        Map<Rota, Heroi> inimigos,
        List<String> motivos
    ) {
        if (inimigos.isEmpty()) {
            return 0;
        }

        int pontuacao = 0;
        double mobilidadeMedia = media(inimigos, Atributo.MOBILIDADE);
        double resistenciaMedia = media(inimigos, Atributo.RESISTENCIA);
        double alcanceMedio = media(inimigos, Atributo.ALCANCE);
        long ameacasExplosivas = inimigos.values().stream()
            .filter(heroi -> heroi.getAtributos().danoExplosivo() >= 8)
            .count();

        if (
            mobilidadeMedia >= 7
                && candidato.getAtributos().controle() >= 7
        ) {
            pontuacao += 5;
            motivos.add("Oferece controle contra uma equipe inimiga móvel.");
        }

        if (
            resistenciaMedia >= 7
                && candidato.getAtributos().danoSustentado() >= 8
        ) {
            pontuacao += 5;
            motivos.add("Possui dano sustentado contra alvos resistentes.");
        }

        if (
            alcanceMedio >= 7
                && candidato.getAtributos().mobilidade() >= 7
        ) {
            pontuacao += 4;
            motivos.add("Tem mobilidade para alcançar uma composição de alcance.");
        }

        if (
            ameacasExplosivas >= 2
                && candidato.getAtributos().resistencia() >= 7
        ) {
            pontuacao += 4;
            motivos.add("Resiste melhor a múltiplas ameaças de explosão.");
        }

        return limitar(pontuacao, 0, 15);
    }

    private int calcularAcessibilidade(
        Heroi candidato,
        List<String> motivos
    ) {
        int pontuacao = switch (candidato.getDificuldade()) {
            case 1 -> 4;
            case 2 -> 2;
            case 3 -> 0;
            case 4 -> -2;
            default -> -4;
        };

        if (pontuacao > 0) {
            motivos.add("Execução relativamente acessível para uso imediato.");
        } else if (pontuacao < 0) {
            motivos.add("Exige execução mecânica mais avançada.");
        }

        return pontuacao;
    }

    private double media(
        Map<Rota, Heroi> equipe,
        Atributo atributo
    ) {
        return equipe.values().stream()
            .mapToInt(heroi -> valorAtributo(heroi, atributo))
            .average()
            .orElse(0);
    }

    private int valorAtributo(Heroi heroi, Atributo atributo) {
        return switch (atributo) {
            case CONTROLE -> heroi.getAtributos().controle();
            case RESISTENCIA -> heroi.getAtributos().resistencia();
            case MOBILIDADE -> heroi.getAtributos().mobilidade();
            case ALCANCE -> heroi.getAtributos().alcance();
            case DANO_SUSTENTADO ->
                heroi.getAtributos().danoSustentado();
        };
    }

    private List<String> criarAvisos(
        Rota rotaAlvo,
        Map<Rota, Heroi> inimigos,
        int totalCandidatos
    ) {
        List<String> avisos = new ArrayList<>();

        avisos.add(
            "Os dados estratégicos são provisórios e ainda não representam estatísticas oficiais do jogo."
        );

        if (!inimigos.containsKey(rotaAlvo)) {
            avisos.add(
                "A rota adversária alvo está vazia; a nota não inclui confronto direto."
            );
        }

        if (totalCandidatos < 3) {
            avisos.add(
                "Há poucos candidatos disponíveis porque alguns heróis já foram selecionados."
            );
        }

        return avisos;
    }

    private String calcularNivel(int pontuacao) {
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

    private static String chaveDirecional(Long candidatoId, Long inimigoId) {
        return candidatoId + ":" + inimigoId;
    }

    private static String chavePar(Long primeiroId, Long segundoId) {
        long menor = Math.min(primeiroId, segundoId);
        long maior = Math.max(primeiroId, segundoId);
        return menor + ":" + maior;
    }

    private enum Atributo {
        CONTROLE,
        RESISTENCIA,
        MOBILIDADE,
        ALCANCE,
        DANO_SUSTENTADO
    }
}
