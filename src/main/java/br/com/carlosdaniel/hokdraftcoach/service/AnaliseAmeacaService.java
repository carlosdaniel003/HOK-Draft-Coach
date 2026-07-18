package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteRespostaAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AlvoPrioritarioAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseAmeacasResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AntiSinergiaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.CondicaoVitoriaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilAmeacaHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilEconomicoHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.SinergiaGrupoResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.CondicaoVitoriaTipo;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PapelAmeaca;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilTemporalHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.TipoSinergiaGrupo;

@Service
public class AnaliseAmeacaService {

    private final DnaHeroiService dnaHeroiService;
    private final PerfilTemporalService perfilTemporalService;
    private final EconomiaRecursosService economiaRecursosService;

    public AnaliseAmeacaService(
        DnaHeroiService dnaHeroiService,
        PerfilTemporalService perfilTemporalService,
        EconomiaRecursosService economiaRecursosService
    ) {
        this.dnaHeroiService = dnaHeroiService;
        this.perfilTemporalService = perfilTemporalService;
        this.economiaRecursosService = economiaRecursosService;
    }

    public AnaliseAmeacasResponse analisar(
        List<Heroi> equipe,
        DnaComposicao dna,
        List<CondicaoVitoriaResponse> condicoes,
        List<SinergiaGrupoResponse> sinergias,
        List<AntiSinergiaResponse> antiSinergias
    ) {
        if (equipe == null || equipe.isEmpty()) {
            return AnaliseAmeacasResponse.vazia();
        }

        List<PerfilBruto> brutos = equipe.stream()
            .map(heroi -> calcularPerfil(
                heroi,
                condicoes,
                sinergias,
                antiSinergias
            ))
            .toList();
        PerfilBruto maiorAmeaca = maior(
            brutos,
            PerfilBruto::potencialVitoria,
            null
        );
        PerfilBruto protetor = maior(
            brutos,
            PerfilBruto::protecao,
            maiorAmeaca.heroi()
        );
        PerfilBruto iniciador = maior(
            brutos,
            PerfilBruto::iniciacao,
            null
        );
        PerfilBruto habilitador = maior(
            brutos,
            PerfilBruto::habilitacao,
            maiorAmeaca.heroi()
        );
        PerfilBruto eloFraco = maior(
            brutos,
            PerfilBruto::vulnerabilidade,
            maiorAmeaca.heroi()
        );

        Map<String, Set<PapelAmeaca>> papeis = new LinkedHashMap<>();
        marcar(papeis, maiorAmeaca, PapelAmeaca.AMEACA_PRINCIPAL);
        marcar(papeis, protetor, PapelAmeaca.PROTETOR);
        marcar(papeis, iniciador, PapelAmeaca.INICIADOR);
        marcar(papeis, habilitador, PapelAmeaca.HABILITADOR);
        marcar(papeis, eloFraco, PapelAmeaca.ELO_FRACO);

        List<PerfilAmeacaHeroiResponse> perfis = brutos.stream()
            .map(bruto -> resposta(
                bruto,
                papeis.getOrDefault(
                    normalizar(bruto.heroi().getNome()),
                    Set.of()
                )
            ))
            .sorted(
                Comparator.comparingInt(
                    PerfilAmeacaHeroiResponse::potencialVitoria
                ).reversed()
            )
            .toList();

        PerfilAmeacaHeroiResponse maiorAmeacaResposta = encontrar(
            perfis,
            maiorAmeaca
        );
        PerfilAmeacaHeroiResponse protetorResposta = encontrar(
            perfis,
            protetor
        );
        PerfilAmeacaHeroiResponse iniciadorResposta = encontrar(
            perfis,
            iniciador
        );
        PerfilAmeacaHeroiResponse habilitadorResposta = encontrar(
            perfis,
            habilitador
        );
        PerfilAmeacaHeroiResponse eloFracoResposta = encontrar(
            perfis,
            eloFraco
        );
        List<AlvoPrioritarioAmeacaResponse> alvos = montarAlvos(
            maiorAmeaca,
            protetor,
            iniciador,
            habilitador,
            eloFraco,
            sinergias
        );

        return new AnaliseAmeacasResponse(
            maiorAmeacaResposta,
            protetorResposta,
            iniciadorResposta,
            habilitadorResposta,
            eloFracoResposta,
            perfis,
            alvos,
            PlanoRespostaAmeaca.criar(
                maiorAmeacaResposta,
                alvos
            )
        );
    }

    public AjusteRespostaAmeacaResponse avaliarCandidato(
        Heroi candidato,
        AnaliseAmeacasResponse analise
    ) {
        if (
            analise == null
                || analise.alvosPrioritarios().isEmpty()
        ) {
            return AjusteRespostaAmeacaResponse.vazio();
        }

        DnaHeroi dna = dnaHeroiService.calcular(candidato);
        int bonus = 0;
        List<String> alvos = new ArrayList<>();
        List<String> motivos = new ArrayList<>();

        for (
            AlvoPrioritarioAmeacaResponse alvo
                : analise.alvosPrioritarios().stream().limit(3).toList()
        ) {
            if (alvo.respostasRecomendadas().isEmpty()) {
                continue;
            }
            double media = alvo.respostasRecomendadas().stream()
                .mapToInt(dna::valor)
                .average()
                .orElse(0);
            int maiorValor = alvo.respostasRecomendadas().stream()
                .mapToInt(dna::valor)
                .max()
                .orElse(0);
            if (media < 55 && maiorValor < 72) {
                continue;
            }

            int qualidade = media >= 70
                ? 6
                : media >= 62 ? 4 : 2;
            if (maiorValor >= 82) {
                qualidade += 2;
            }
            int pesoAlvo = alvo.prioridade() >= 92
                ? 4
                : alvo.prioridade() >= 80 ? 3 : 2;
            bonus += qualidade + pesoAlvo;
            alvos.add(alvo.heroi());
            motivos.add(
                candidato.getNome() + " possui ferramentas para neutralizar "
                    + alvo.heroi() + " como " + nomePapel(alvo.papel()) + "."
            );
        }

        return new AjusteRespostaAmeacaResponse(
            Math.min(20, bonus),
            alvos.stream().distinct().toList(),
            motivos.stream().distinct().limit(4).toList()
        );
    }

    private PerfilBruto calcularPerfil(
        Heroi heroi,
        List<CondicaoVitoriaResponse> condicoes,
        List<SinergiaGrupoResponse> sinergias,
        List<AntiSinergiaResponse> antiSinergias
    ) {
        DnaHeroi dna = dnaHeroiService.calcular(heroi);
        PerfilTemporalHeroi temporal = perfilTemporalService.perfil(heroi);
        PerfilEconomicoHeroiResponse economia = economiaRecursosService.perfil(
            heroi
        );
        int condicaoPrincipal = executaCondicaoPrincipal(heroi, condicoes)
            ? 12
            : 0;
        int condicoesExecutadas = (int) condicoes.stream()
            .filter(condicao -> contemNome(condicao.executores(), heroi))
            .count();
        List<SinergiaGrupoResponse> sinergiasAtivas = sinergias.stream()
            .filter(SinergiaGrupoResponse::ativa)
            .filter(sinergia -> contemNome(sinergia.membros(), heroi))
            .toList();
        int centralidade = Math.min(
            20,
            sinergiasAtivas.size() * 5
                + sinergiasAtivas.stream()
                    .mapToInt(SinergiaGrupoResponse::nota)
                    .max()
                    .orElse(0) / 20
        );
        int anti = (int) antiSinergias.stream()
            .filter(resposta -> contemNome(resposta.herois(), heroi))
            .count();

        int potencial = limitar((int) Math.round(
            dna.valor(DimensaoEstrategica.DPS) * 0.23
                + dna.valor(DimensaoEstrategica.EXPLOSAO) * 0.17
                + dna.valor(DimensaoEstrategica.ESCALAMENTO) * 0.17
                + dna.valor(DimensaoEstrategica.OBJETIVOS) * 0.10
                + dna.valor(DimensaoEstrategica.PRESSAO_LATERAL) * 0.08
                + dna.valor(DimensaoEstrategica.ALCANCE) * 0.08
                + dna.valor(DimensaoEstrategica.MOBILIDADE) * 0.07
                + Math.max(temporal.midGame(), temporal.lateGame()) * 0.10
        ) + bonusClasseCarregadora(heroi.getClasse())
            + condicaoPrincipal
            + Math.min(8, centralidade / 2));

        int protecao = limitar((int) Math.round(
            dna.valor(DimensaoEstrategica.PROTECAO) * 0.27
                + dna.valor(DimensaoEstrategica.PEEL) * 0.22
                + dna.valor(DimensaoEstrategica.SUSTAIN) * 0.15
                + dna.valor(DimensaoEstrategica.DESENGAGE) * 0.14
                + dna.valor(DimensaoEstrategica.CONTROLE) * 0.11
                + dna.valor(DimensaoEstrategica.LINHA_DE_FRENTE) * 0.11
        ) + bonusClasseProtetora(heroi.getClasse())
            + bonusProtecaoCondicao(heroi, condicoes));

        int iniciacao = limitar((int) Math.round(
            dna.valor(DimensaoEstrategica.ENGAGE) * 0.36
                + dna.valor(DimensaoEstrategica.CONTROLE) * 0.24
                + dna.valor(DimensaoEstrategica.LINHA_DE_FRENTE) * 0.18
                + dna.valor(DimensaoEstrategica.MOBILIDADE) * 0.12
                + dna.valor(DimensaoEstrategica.EXPLOSAO) * 0.10
        ) + bonusClasseIniciadora(heroi.getClasse())
            + (condicaoPrincipal > 0 ? 5 : 0));

        int utilidade = media(
            dna.valor(DimensaoEstrategica.CONTROLE),
            dna.valor(DimensaoEstrategica.OBJETIVOS),
            dna.valor(DimensaoEstrategica.WAVE_CLEAR),
            dna.valor(DimensaoEstrategica.PEEL),
            dna.valor(DimensaoEstrategica.DESENGAGE)
        );
        int habilitacao = limitar((int) Math.round(
            protecao * 0.26
                + iniciacao * 0.28
                + utilidade * 0.24
                + centralidade * 0.75
        ) + condicoesExecutadas * 4
            + bonusTagsHabilitadoras(heroi)
            + bonusTipoSinergia(sinergiasAtivas));

        int contribuicao = Math.max(
            potencial,
            Math.max(protecao, Math.max(iniciacao, habilitacao))
        );
        int vulnerabilidade = limitar((int) Math.round(
            (100 - dna.valor(DimensaoEstrategica.LINHA_DE_FRENTE)) * 0.22
                + (100 - dna.valor(DimensaoEstrategica.MOBILIDADE)) * 0.17
                + (100 - dna.valor(DimensaoEstrategica.PROTECAO)) * 0.11
                + economia.dependenciaRecursos() * 0.18
                + (100 - contribuicao) * 0.25
        ) + anti * 5);

        List<String> motivos = new ArrayList<>();
        if (potencial >= 75) {
            motivos.add("Possui dano, escalamento ou pressão suficientes para decidir a partida.");
        }
        if (protecao >= 70) {
            motivos.add("Mantém carregadores vivos com proteção, peel ou sustain.");
        }
        if (iniciacao >= 70) {
            motivos.add("Cria a janela inicial das lutas com engage e controle.");
        }
        if (habilitacao >= 70) {
            motivos.add("Conecta aliados e amplifica a execução da condição de vitória.");
        }
        if (centralidade >= 10) {
            motivos.add("Participa diretamente de sinergias de grupo ativas.");
        }
        if (vulnerabilidade >= 65) {
            motivos.add("Apresenta dependência ou exposição que pode ser explorada.");
        }

        return new PerfilBruto(
            heroi,
            potencial,
            protecao,
            iniciacao,
            habilitacao,
            vulnerabilidade,
            motivos
        );
    }

    private List<AlvoPrioritarioAmeacaResponse> montarAlvos(
        PerfilBruto ameaca,
        PerfilBruto protetor,
        PerfilBruto iniciador,
        PerfilBruto habilitador,
        PerfilBruto eloFraco,
        List<SinergiaGrupoResponse> sinergias
    ) {
        boolean conectaIniciador = conecta(sinergias, iniciador, ameaca);
        boolean conectaHabilitador = conecta(sinergias, habilitador, ameaca);
        boolean conectaProtetor = conecta(sinergias, protetor, ameaca);
        boolean ameacaProtegida = conectaIniciador
            || conectaHabilitador
            || conectaProtetor;

        List<AlvoPrioritarioAmeacaResponse> candidatos = List.of(
            alvo(
                ameaca,
                PapelAmeaca.AMEACA_PRINCIPAL,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.AMEACA_PRINCIPAL,
                    ameaca.potencialVitoria(),
                    ameaca.vulnerabilidade(),
                    false,
                    ameacaProtegida
                ),
                ameaca,
                sinergias
            ),
            alvo(
                iniciador,
                PapelAmeaca.INICIADOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.INICIADOR,
                    iniciador.iniciacao(),
                    iniciador.vulnerabilidade(),
                    conectaIniciador,
                    false
                ),
                ameaca,
                sinergias
            ),
            alvo(
                habilitador,
                PapelAmeaca.HABILITADOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.HABILITADOR,
                    habilitador.habilitacao(),
                    habilitador.vulnerabilidade(),
                    conectaHabilitador,
                    false
                ),
                ameaca,
                sinergias
            ),
            alvo(
                protetor,
                PapelAmeaca.PROTETOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.PROTETOR,
                    protetor.protecao(),
                    protetor.vulnerabilidade(),
                    conectaProtetor,
                    false
                ),
                ameaca,
                sinergias
            ),
            alvo(
                eloFraco,
                PapelAmeaca.ELO_FRACO,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.ELO_FRACO,
                    eloFraco.vulnerabilidade(),
                    eloFraco.vulnerabilidade(),
                    false,
                    false
                ),
                ameaca,
                sinergias
            )
        );

        Map<String, AlvoPrioritarioAmeacaResponse> unicos =
            new LinkedHashMap<>();
        candidatos.forEach(candidato -> unicos.merge(
            normalizar(candidato.heroi()),
            candidato,
            (atual, novo) -> atual.prioridade() >= novo.prioridade()
                ? atual
                : novo
        ));

        return unicos.values().stream()
            .sorted(
                Comparator.comparingInt(
                    AlvoPrioritarioAmeacaResponse::prioridade
                ).reversed()
            )
            .limit(4)
            .toList();
    }

    private AlvoPrioritarioAmeacaResponse alvo(
        PerfilBruto perfil,
        PapelAmeaca papel,
        int prioridade,
        PerfilBruto ameaca,
        List<SinergiaGrupoResponse> sinergias
    ) {
        boolean conectado = conecta(sinergias, perfil, ameaca);
        String justificativa = switch (papel) {
            case AMEACA_PRINCIPAL ->
                perfil.heroi().getNome()
                    + " possui o maior potencial individual de converter recursos em vitória.";
            case INICIADOR ->
                perfil.heroi().getNome()
                    + " cria a janela de entrada para o dano principal"
                    + (conectado ? " e participa do mesmo combo de grupo." : ".");
            case HABILITADOR ->
                perfil.heroi().getNome()
                    + " amplifica ou permite repetir a principal condição inimiga"
                    + (conectado ? " dentro de uma sinergia ativa." : ".");
            case PROTETOR ->
                perfil.heroi().getNome()
                    + " mantém o carregador vivo e preserva sua janela de dano.";
            case ELO_FRACO ->
                perfil.heroi().getNome()
                    + " apresenta a maior combinação de exposição, dependência e baixa autonomia.";
        };

        return new AlvoPrioritarioAmeacaResponse(
            perfil.heroi().getNome(),
            papel,
            limitar(prioridade),
            respostas(papel, perfil),
            justificativa
        );
    }

    private List<DimensaoEstrategica> respostas(
        PapelAmeaca papel,
        PerfilBruto perfil
    ) {
        LinkedHashSet<DimensaoEstrategica> dimensoes =
            new LinkedHashSet<>();
        switch (papel) {
            case AMEACA_PRINCIPAL -> {
                dimensoes.add(DimensaoEstrategica.CONTROLE);
                dimensoes.add(DimensaoEstrategica.DIVE);
                dimensoes.add(DimensaoEstrategica.EXPLOSAO);
                if (perfil.protecao() >= 60) {
                    dimensoes.add(DimensaoEstrategica.ANTI_CURA);
                }
                dimensoes.add(DimensaoEstrategica.MOBILIDADE);
            }
            case INICIADOR -> {
                dimensoes.add(DimensaoEstrategica.DESENGAGE);
                dimensoes.add(DimensaoEstrategica.PEEL);
                dimensoes.add(DimensaoEstrategica.CONTROLE);
                dimensoes.add(DimensaoEstrategica.MOBILIDADE);
            }
            case HABILITADOR -> {
                dimensoes.add(DimensaoEstrategica.CONTROLE);
                dimensoes.add(DimensaoEstrategica.EXPLOSAO);
                dimensoes.add(DimensaoEstrategica.MOBILIDADE);
                dimensoes.add(DimensaoEstrategica.ALCANCE);
            }
            case PROTETOR -> {
                dimensoes.add(DimensaoEstrategica.ANTI_CURA);
                dimensoes.add(DimensaoEstrategica.DIVE);
                dimensoes.add(DimensaoEstrategica.CONTROLE);
                dimensoes.add(DimensaoEstrategica.EXPLOSAO);
            }
            case ELO_FRACO -> {
                dimensoes.add(DimensaoEstrategica.EXPLOSAO);
                dimensoes.add(DimensaoEstrategica.DIVE);
                dimensoes.add(DimensaoEstrategica.ALCANCE);
            }
        }
        return List.copyOf(dimensoes);
    }

    private PerfilAmeacaHeroiResponse resposta(
        PerfilBruto bruto,
        Set<PapelAmeaca> papeis
    ) {
        return new PerfilAmeacaHeroiResponse(
            bruto.heroi().getNome(),
            bruto.potencialVitoria(),
            bruto.protecao(),
            bruto.iniciacao(),
            bruto.habilitacao(),
            bruto.vulnerabilidade(),
            List.copyOf(papeis),
            bruto.motivos()
        );
    }

    private PerfilAmeacaHeroiResponse encontrar(
        List<PerfilAmeacaHeroiResponse> perfis,
        PerfilBruto bruto
    ) {
        if (bruto == null) {
            return null;
        }
        return perfis.stream()
            .filter(perfil -> bruto.heroi().correspondeAoNome(perfil.heroi()))
            .findFirst()
            .orElse(null);
    }

    private PerfilBruto maior(
        List<PerfilBruto> perfis,
        ToIntFunction<PerfilBruto> funcao,
        Heroi evitar
    ) {
        List<PerfilBruto> candidatos = perfis;
        if (evitar != null && perfis.size() > 1) {
            candidatos = perfis.stream()
                .filter(perfil -> !perfil.heroi().correspondeAoNome(
                    evitar.getNome()
                ))
                .toList();
        }
        return candidatos.stream()
            .max(Comparator.comparingInt(funcao))
            .orElse(perfis.getFirst());
    }

    private void marcar(
        Map<String, Set<PapelAmeaca>> papeis,
        PerfilBruto perfil,
        PapelAmeaca papel
    ) {
        if (perfil == null) {
            return;
        }
        papeis.computeIfAbsent(
            normalizar(perfil.heroi().getNome()),
            ignorada -> new LinkedHashSet<>()
        ).add(papel);
    }

    private boolean executaCondicaoPrincipal(
        Heroi heroi,
        List<CondicaoVitoriaResponse> condicoes
    ) {
        return condicoes.stream()
            .filter(CondicaoVitoriaResponse::principal)
            .anyMatch(condicao -> contemNome(condicao.executores(), heroi));
    }

    private int bonusProtecaoCondicao(
        Heroi heroi,
        List<CondicaoVitoriaResponse> condicoes
    ) {
        return condicoes.stream()
            .filter(CondicaoVitoriaResponse::principal)
            .filter(condicao ->
                condicao.tipo() == CondicaoVitoriaTipo.PROTEGER_HIPERCARREGADOR
            )
            .filter(condicao -> contemNome(condicao.executores(), heroi))
            .findFirst()
            .map(ignorada -> 8)
            .orElse(0);
    }

    private int bonusTipoSinergia(
        List<SinergiaGrupoResponse> sinergias
    ) {
        int bonus = 0;
        for (SinergiaGrupoResponse sinergia : sinergias) {
            bonus += switch (sinergia.tipo()) {
                case RESET_DE_HABILIDADES -> 14;
                case AGRUPAMENTO_SEGUIDO_DE_DANO_EM_AREA,
                    COMBO_DE_ULTIMATES,
                    SEQUENCIA_DE_ENGAGE -> 8;
                case PROTECAO_EM_CAMADAS -> 7;
                case CONTROLE_EM_CADEIA, WOMBO_COMBO -> 6;
            };
        }
        return Math.min(18, bonus);
    }

    private int bonusTagsHabilitadoras(Heroi heroi) {
        int bonus = 0;
        if (possuiTag(
            heroi,
            "reset de ultimate",
            "reset",
            "reinício",
            "reinicio"
        )) {
            bonus += 16;
        }
        if (possuiTag(
            heroi,
            "amplificação",
            "amplificacao",
            "agrupamento",
            "macro",
            "visão",
            "visao",
            "controle em área",
            "controle em area"
        )) {
            bonus += 8;
        }
        return bonus;
    }

    private boolean conecta(
        List<SinergiaGrupoResponse> sinergias,
        PerfilBruto primeiro,
        PerfilBruto segundo
    ) {
        if (primeiro == null || segundo == null) {
            return false;
        }
        return sinergias.stream()
            .filter(SinergiaGrupoResponse::ativa)
            .anyMatch(sinergia ->
                contemNome(sinergia.membros(), primeiro.heroi())
                    && contemNome(sinergia.membros(), segundo.heroi())
            );
    }

    private boolean contemNome(List<String> nomes, Heroi heroi) {
        return nomes.stream().anyMatch(heroi::correspondeAoNome);
    }

    private boolean possuiTag(Heroi heroi, String... tags) {
        return heroi.getCaracteristicas().stream()
            .map(this::normalizar)
            .anyMatch(caracteristica -> {
                for (String tag : tags) {
                    if (caracteristica.contains(normalizar(tag))) {
                        return true;
                    }
                }
                return false;
            });
    }

    private int bonusClasseCarregadora(ClasseHeroi classe) {
        return switch (classe) {
            case ATIRADOR -> 8;
            case ASSASSINO -> 6;
            case MAGO -> 4;
            case LUTADOR, HIBRIDO -> 2;
            case TANQUE, SUPORTE -> 0;
        };
    }

    private int bonusClasseProtetora(ClasseHeroi classe) {
        return switch (classe) {
            case SUPORTE -> 9;
            case TANQUE -> 6;
            case HIBRIDO -> 3;
            default -> 0;
        };
    }

    private int bonusClasseIniciadora(ClasseHeroi classe) {
        return switch (classe) {
            case TANQUE -> 8;
            case LUTADOR -> 5;
            case ASSASSINO, HIBRIDO -> 3;
            default -> 0;
        };
    }

    private int media(int... valores) {
        int soma = 0;
        for (int valor : valores) {
            soma += valor;
        }
        return valores.length == 0
            ? 0
            : Math.round(soma / (float) valores.length);
    }

    private String nomePapel(PapelAmeaca papel) {
        return switch (papel) {
            case AMEACA_PRINCIPAL -> "ameaça principal";
            case PROTETOR -> "protetor";
            case INICIADOR -> "iniciador";
            case HABILITADOR -> "habilitador";
            case ELO_FRACO -> "elo fraco";
        };
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase(Locale.ROOT);
    }

    private int limitar(int valor) {
        return Math.max(0, Math.min(100, valor));
    }

    private record PerfilBruto(
        Heroi heroi,
        int potencialVitoria,
        int protecao,
        int iniciacao,
        int habilitacao,
        int vulnerabilidade,
        List<String> motivos
    ) {
    }
}
