package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.EconomiaComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.NecessidadeComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PenalidadeComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilEconomicoHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PrioridadeDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;

@Service
public class NecessidadePenalidadeService {

    private static final Map<DimensaoEstrategica, Integer> ALVOS_BASE;
    private static final Map<DimensaoEstrategica, Integer> IMPORTANCIA;

    static {
        EnumMap<DimensaoEstrategica, Integer> alvos =
            new EnumMap<>(DimensaoEstrategica.class);
        alvos.put(DimensaoEstrategica.ENGAGE, 55);
        alvos.put(DimensaoEstrategica.CONTROLE, 50);
        alvos.put(DimensaoEstrategica.LINHA_DE_FRENTE, 55);
        alvos.put(DimensaoEstrategica.DPS, 55);
        alvos.put(DimensaoEstrategica.PEEL, 50);
        alvos.put(DimensaoEstrategica.WAVE_CLEAR, 50);
        alvos.put(DimensaoEstrategica.OBJETIVOS, 50);
        alvos.put(DimensaoEstrategica.SUSTAIN, 45);
        ALVOS_BASE = Map.copyOf(alvos);

        EnumMap<DimensaoEstrategica, Integer> importancia =
            new EnumMap<>(DimensaoEstrategica.class);
        importancia.put(DimensaoEstrategica.ENGAGE, 92);
        importancia.put(DimensaoEstrategica.CONTROLE, 78);
        importancia.put(DimensaoEstrategica.LINHA_DE_FRENTE, 90);
        importancia.put(DimensaoEstrategica.DPS, 88);
        importancia.put(DimensaoEstrategica.PEEL, 82);
        importancia.put(DimensaoEstrategica.WAVE_CLEAR, 65);
        importancia.put(DimensaoEstrategica.OBJETIVOS, 67);
        importancia.put(DimensaoEstrategica.SUSTAIN, 58);
        IMPORTANCIA = Map.copyOf(importancia);
    }

    private final DnaHeroiService dnaHeroiService;
    private final EconomiaRecursosService economiaRecursosService;

    public NecessidadePenalidadeService(
        DnaHeroiService dnaHeroiService,
        EconomiaRecursosService economiaRecursosService
    ) {
        this.dnaHeroiService = dnaHeroiService;
        this.economiaRecursosService = economiaRecursosService;
    }

    public List<NecessidadeComposicaoResponse> necessidades(
        DnaComposicao dna,
        List<PrioridadeDraftResponse> prioridades,
        EconomiaComposicaoResponse economia
    ) {
        Map<String, NecessidadeComposicaoResponse> resultado =
            new LinkedHashMap<>();

        for (Map.Entry<DimensaoEstrategica, Integer> entrada :
            ALVOS_BASE.entrySet()) {
            DimensaoEstrategica dimensao = entrada.getKey();
            int atual = dna.valor(dimensao);
            int alvo = entrada.getValue();
            int deficit = Math.max(0, alvo - atual);
            if (deficit < 8) {
                continue;
            }
            int urgencia = limitar(
                IMPORTANCIA.getOrDefault(dimensao, 60) - 20 + deficit,
                0,
                100
            );
            resultado.put(
                dimensao.name(),
                criarNecessidade(
                    dimensao.name(),
                    dimensao,
                    urgencia,
                    atual,
                    alvo,
                    "A composição ainda não atingiu o mínimo estrutural de "
                        + dimensao + "."
                )
            );
        }

        for (PrioridadeDraftResponse prioridade : prioridades) {
            String chave = prioridade.dimensao().name();
            NecessidadeComposicaoResponse atual = resultado.get(chave);
            if (atual == null || prioridade.urgencia() > atual.urgencia()) {
                resultado.put(
                    chave,
                    criarNecessidade(
                        chave,
                        prioridade.dimensao(),
                        prioridade.urgencia(),
                        prioridade.valorAtual(),
                        prioridade.alvoMinimo(),
                        prioridade.motivo()
                    )
                );
            }
        }

        if (
            economia.carregadoresDependentes() >= 3
                || economia.conflitoDeRecursos() >= 45
        ) {
            int atual = 100 - economia.conflitoDeRecursos();
            resultado.put(
                "BAIXA_DEPENDENCIA_RECURSOS",
                new NecessidadeComposicaoResponse(
                    "BAIXA_DEPENDENCIA_RECURSOS",
                    null,
                    limitar(Math.max(82, economia.conflitoDeRecursos()), 0, 100),
                    atual,
                    70,
                    Math.max(0, 70 - atual),
                    "A equipe precisa de uma escolha que funcione com pouco ouro",
                    "Já existem " + economia.carregadoresDependentes()
                        + " heróis dependentes de recursos. A próxima escolha deve ceder farm e entregar utilidade pelo kit.",
                    List.of(
                        "baixa dependência de recursos",
                        "controle sem itens",
                        "linha de frente",
                        "proteção",
                        "utilidade"
                    )
                )
            );
        }

        return resultado.values().stream()
            .sorted(
                Comparator.comparingInt(
                    NecessidadeComposicaoResponse::urgencia
                ).reversed()
            )
            .toList();
    }

    public List<PenalidadeComposicaoResponse> penalidades(
        List<Heroi> equipe,
        DnaComposicao dna,
        EconomiaComposicaoResponse economia
    ) {
        if (equipe == null || equipe.isEmpty()) {
            return List.of();
        }

        Map<Heroi, DnaHeroi> individuais = new LinkedHashMap<>();
        equipe.forEach(heroi -> individuais.put(
            heroi,
            dnaHeroiService.calcular(heroi)
        ));
        List<PenalidadeComposicaoResponse> penalidades = new ArrayList<>();

        List<String> iniciadores = nomesComValorMinimo(
            individuais,
            DimensaoEstrategica.ENGAGE,
            65
        );
        int danoFollowUp = Math.max(
            dna.valor(DimensaoEstrategica.DPS),
            dna.valor(DimensaoEstrategica.EXPLOSAO)
        );
        if (iniciadores.size() >= 3 && danoFollowUp < 58) {
            penalidades.add(new PenalidadeComposicaoResponse(
                "INICIACAO_REDUNDANTE_SEM_FOLLOW_UP",
                SeveridadeDiagnostico.ALTA,
                limitar(18 + (iniciadores.size() - 3) * 5, 0, 35),
                "Há iniciadores demais e dano de acompanhamento insuficiente",
                "A equipe consegue começar várias lutas, mas não possui DPS ou explosão para converter o controle em eliminações.",
                List.of(DimensaoEstrategica.ENGAGE),
                iniciadores
            ));
        }

        List<String> curadores = equipe.stream()
            .filter(heroi ->
                heroi.getClasse() == ClasseHeroi.SUPORTE
                    && possuiTag(
                        heroi,
                        "cura",
                        "sustentação",
                        "escudo",
                        "proteção"
                    )
            )
            .map(Heroi::getNome)
            .toList();
        if (curadores.size() >= 2 && dna.valor(DimensaoEstrategica.DPS) < 55) {
            penalidades.add(new PenalidadeComposicaoResponse(
                "SUPORTES_DE_SUSTENTACAO_SEM_DPS",
                SeveridadeDiagnostico.ALTA,
                24,
                "A composição possui sustentação redundante, mas não possui DPS",
                "Dois ou mais heróis prolongam a luta, porém falta um carregador capaz de usar esse tempo adicional.",
                List.of(
                    DimensaoEstrategica.SUSTAIN,
                    DimensaoEstrategica.PROTECAO
                ),
                curadores
            ));
        }

        List<String> frontliners = nomesComValorMinimo(
            individuais,
            DimensaoEstrategica.LINHA_DE_FRENTE,
            68
        );
        if (frontliners.size() >= 3 && danoFollowUp < 52) {
            penalidades.add(new PenalidadeComposicaoResponse(
                "FRONTLINE_REDUNDANTE_SEM_DANO",
                SeveridadeDiagnostico.ALTA,
                22,
                "A equipe possui linha de frente em excesso e pouco dano",
                "Múltiplos heróis absorvem dano, mas nenhum núcleo ofensivo converte o espaço criado.",
                List.of(DimensaoEstrategica.LINHA_DE_FRENTE),
                frontliners
            ));
        }

        List<String> artilheiros = nomesComValorMinimo(
            individuais,
            DimensaoEstrategica.POKE,
            70
        );
        if (
            artilheiros.size() >= 3
                && dna.valor(DimensaoEstrategica.LINHA_DE_FRENTE) < 48
                && dna.valor(DimensaoEstrategica.ENGAGE) < 48
        ) {
            penalidades.add(new PenalidadeComposicaoResponse(
                "POKE_REDUNDANTE_SEM_ESPACO",
                SeveridadeDiagnostico.ATENCAO,
                16,
                "Há muito poke, mas ninguém cria espaço para a artilharia",
                "O excesso de alcance não compensa a ausência de frontline, engage ou proteção contra flancos.",
                List.of(
                    DimensaoEstrategica.POKE,
                    DimensaoEstrategica.ALCANCE
                ),
                artilheiros
            ));
        }

        if (economia.carregadoresDependentes() >= 3) {
            List<String> dependentes = economia.perfis().stream()
                .filter(perfil -> perfil.dependenciaRecursos() >= 70)
                .map(PerfilEconomicoHeroiResponse::heroi)
                .toList();
            penalidades.add(new PenalidadeComposicaoResponse(
                "CONFLITO_DE_RECURSOS",
                economia.conflitoDeRecursos() >= 70
                    ? SeveridadeDiagnostico.CRITICA
                    : SeveridadeDiagnostico.ALTA,
                limitar(
                    15 + (economia.carregadoresDependentes() - 3) * 8,
                    0,
                    40
                ),
                "Há carregadores demais disputando o mesmo ouro",
                "Jungle, rotas e eliminações não conseguem sustentar simultaneamente todos os picos de itens da composição.",
                List.of(DimensaoEstrategica.ESCALAMENTO),
                dependentes
            ));
        }

        if (
            equipe.size() >= 3
                && (dna.distribuicaoDano().fisico() == 100
                    || dna.distribuicaoDano().magico() == 100)
        ) {
            penalidades.add(new PenalidadeComposicaoResponse(
                "DANO_TOTALMENTE_MONOTIPO",
                SeveridadeDiagnostico.ATENCAO,
                10,
                "Toda a composição depende do mesmo tipo de dano",
                "Uma única resistência defensiva pode reduzir a eficiência de todos os carregadores ao mesmo tempo.",
                List.of(
                    DimensaoEstrategica.DPS,
                    DimensaoEstrategica.EXPLOSAO
                ),
                equipe.stream().map(Heroi::getNome).toList()
            ));
        }

        return penalidades.stream()
            .sorted(
                Comparator.comparingInt(
                    PenalidadeComposicaoResponse::reducaoPontuacao
                ).reversed()
            )
            .toList();
    }

    public int totalPenalidades(List<PenalidadeComposicaoResponse> penalidades) {
        return limitar(
            penalidades.stream()
                .mapToInt(PenalidadeComposicaoResponse::reducaoPontuacao)
                .sum(),
            0,
            60
        );
    }

    private NecessidadeComposicaoResponse criarNecessidade(
        String codigo,
        DimensaoEstrategica dimensao,
        int urgencia,
        int atual,
        int alvo,
        String motivo
    ) {
        return new NecessidadeComposicaoResponse(
            codigo,
            dimensao,
            urgencia,
            atual,
            alvo,
            Math.max(0, alvo - atual),
            titulo(dimensao),
            motivo,
            capacidades(dimensao)
        );
    }

    private String titulo(DimensaoEstrategica dimensao) {
        return switch (dimensao) {
            case ENGAGE -> "Falta iniciação confiável";
            case CONTROLE -> "Falta controle para fixar alvos";
            case LINHA_DE_FRENTE -> "Falta linha de frente";
            case DPS -> "Falta dano sustentado";
            case PEEL -> "Falta proteção para a retaguarda";
            case WAVE_CLEAR -> "Falta limpeza de ondas";
            case OBJETIVOS -> "Falta controle de objetivos";
            case SUSTAIN -> "Falta sustentação";
            default -> "Falta capacidade de " + dimensao;
        };
    }

    private List<String> capacidades(DimensaoEstrategica dimensao) {
        return switch (dimensao) {
            case ENGAGE -> List.of(
                "iniciação de longo alcance",
                "controle em área",
                "entrada resistente"
            );
            case CONTROLE -> List.of(
                "stun",
                "supressão",
                "deslocamento",
                "controle em área"
            );
            case LINHA_DE_FRENTE -> List.of(
                "tanque",
                "bruiser resistente",
                "mitigação",
                "ocupação de espaço"
            );
            case DPS -> List.of(
                "dano sustentado",
                "velocidade de ataque",
                "dano anti-tanque"
            );
            case PEEL -> List.of(
                "desengage",
                "escudo",
                "controle defensivo",
                "proteção"
            );
            case WAVE_CLEAR -> List.of(
                "dano em área",
                "artilharia",
                "limpeza rápida"
            );
            case OBJETIVOS -> List.of(
                "DPS em objetivo",
                "zoneamento",
                "resistência",
                "execução"
            );
            case SUSTAIN -> List.of(
                "cura",
                "escudo",
                "recuperação",
                "roubo de vida"
            );
            default -> List.of(dimensao.name().toLowerCase(Locale.ROOT));
        };
    }

    private List<String> nomesComValorMinimo(
        Map<Heroi, DnaHeroi> individuais,
        DimensaoEstrategica dimensao,
        int minimo
    ) {
        return individuais.entrySet().stream()
            .filter(entry -> entry.getValue().valor(dimensao) >= minimo)
            .sorted(
                Comparator.<Map.Entry<Heroi, DnaHeroi>>comparingInt(
                    entry -> entry.getValue().valor(dimensao)
                ).reversed()
            )
            .map(entry -> entry.getKey().getNome())
            .toList();
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

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase(Locale.ROOT);
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
