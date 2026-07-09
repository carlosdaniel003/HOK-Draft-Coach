package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteBlindPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.ContextoDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilSegurancaBlindPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.MomentoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;

@Service
public class SegurancaBlindPickService {

    public AjusteBlindPickResponse avaliar(
        RecomendacaoProximoPickRequest request,
        Heroi heroi,
        RecomendacaoPickResponse base,
        RecomendacaoDnaResponse dna
    ) {
        ContextoDraftResponse contexto = contexto(request);
        PerfilSegurancaBlindPickResponse perfil = perfil(
            heroi,
            base,
            dna
        );
        int ajuste = ajuste(contexto.momento(), perfil, base, dna);
        List<String> motivos = new ArrayList<>();
        List<String> riscos = new ArrayList<>();

        switch (contexto.momento()) {
            case BLIND_PICK_INICIAL -> {
                if (perfil.segurancaBlind() >= 70) {
                    motivos.add(
                        heroi.getNome()
                            + " é seguro para pick inicial: mantém consistência mesmo antes de o inimigo revelar a composição."
                    );
                }
                if (perfil.flexibilidade() >= 70) {
                    motivos.add(
                        "A escolha preserva múltiplas rotas e dificulta a leitura do draft."
                    );
                }
                if (perfil.riscoCounters() >= 60) {
                    riscos.add(
                        "Como blind pick, a escolha pode receber counters graves antes das últimas rotações."
                    );
                }
            }
            case PICK_INTERMEDIARIO -> {
                motivos.add(
                    "No meio do draft, o motor equilibra segurança estrutural e resposta às informações já reveladas."
                );
                if (perfil.especificidade() >= 65) {
                    motivos.add(
                        "A escolha já possui resposta concreta ao cenário sem depender de informação completa."
                    );
                }
            }
            case COUNTER_PICK -> {
                if (perfil.especificidade() >= 65) {
                    motivos.add(
                        "A ordem tardia permite priorizar matchup e resposta específica à composição inimiga."
                    );
                }
                if (perfil.segurancaBlind() < 50) {
                    motivos.add(
                        "A baixa segurança genérica é aceitável porque boa parte do draft adversário já foi revelada."
                    );
                }
            }
            case LAST_PICK -> {
                motivos.add(
                    "No último pick, encaixe exato, counter e resposta à maior dependência inimiga superam a segurança genérica."
                );
                if (
                    dna != null
                        && (!dna.alvosAmeacaRespondidos().isEmpty()
                            || dna.bonusSinergiaGrupo() > 0)
                ) {
                    motivos.add(
                        "A escolha fecha uma resposta ou combinação que só se tornou visível no fim do draft."
                    );
                }
                if (perfil.especificidade() < 50) {
                    riscos.add(
                        "A escolha não aproveita suficientemente a informação disponível para um último pick."
                    );
                }
            }
        }

        return new AjusteBlindPickResponse(
            contexto,
            perfil,
            ajuste,
            motivos.stream().distinct().limit(5).toList(),
            riscos.stream().distinct().limit(4).toList()
        );
    }

    public ContextoDraftResponse contexto(
        RecomendacaoProximoPickRequest request
    ) {
        int ordem = request.minhaOrdem() == null
            ? 0
            : request.minhaOrdem();
        int inimigosRevelados = inimigosRevelados(request);
        MomentoDraft momento = momento(ordem);
        String prioridade = switch (momento) {
            case BLIND_PICK_INICIAL ->
                "Flexibilidade, poucos counters graves e consistência em cenários desconhecidos.";
            case PICK_INTERMEDIARIO ->
                "Equilibrar segurança com necessidades e informações parciais do draft.";
            case COUNTER_PICK ->
                "Priorizar matchup favorável e resposta específica sem abandonar a estrutura da equipe.";
            case LAST_PICK ->
                "Escolher a resposta exata à composição, ao elo crítico e à condição de vitória inimiga.";
        };
        List<String> criterios = switch (momento) {
            case BLIND_PICK_INICIAL -> List.of(
                "múltiplas rotas",
                "alta cobertura de hipóteses",
                "pior cenário estável",
                "baixa exposição a counter"
            );
            case PICK_INTERMEDIARIO -> List.of(
                "consistência",
                "necessidade de composição",
                "sinergia já revelada",
                "flexibilidade restante"
            );
            case COUNTER_PICK -> List.of(
                "matchup favorável",
                "resposta à ameaça",
                "counter de composição",
                "encaixe por rota"
            );
            case LAST_PICK -> List.of(
                "counter específico",
                "resposta ao habilitador inimigo",
                "combo completo",
                "máximo valor contextual"
            );
        };

        return new ContextoDraftResponse(
            momento,
            ordem,
            inimigosRevelados,
            prioridade,
            criterios
        );
    }

    private PerfilSegurancaBlindPickResponse perfil(
        Heroi heroi,
        RecomendacaoPickResponse base,
        RecomendacaoDnaResponse dna
    ) {
        int rotas = heroi.getRotasPossiveis().size();
        int flexibilidade = limitar(
            35
                + Math.min(3, Math.max(0, rotas - 1)) * 20
                + (heroi.isFlex() ? 10 : 0)
                + (heroi.getClasse() == ClasseHeroi.HIBRIDO ? 5 : 0)
        );
        int variacao = Math.max(
            0,
            base.mediaCenarios() - base.piorCenario()
        );
        int estabilidadeVariacao = limitar(100 - variacao * 4);
        int consistencia = limitar((int) Math.round(
            base.piorCenario() * 0.55
                + base.coberturaHipoteses() * 0.30
                + estabilidadeVariacao * 0.15
        ));
        int robustez = limitar(
            heroi.getAtributos().resistencia() * 5
                + heroi.getAtributos().mobilidade() * 3
                + heroi.getAtributos().alcance() * 2
        );
        int riscoCounters = limitar((int) Math.round(
            100
                - consistencia * 0.55
                - robustez * 0.25
                - flexibilidade * 0.20
                + Math.max(0, heroi.getDificuldade() - 3) * 5
        ));
        Map<String, Integer> componentes = base.componentes();
        int confronto = Math.max(
            0,
            componentes.getOrDefault("confronto", 0)
        );
        int resposta = Math.max(
            0,
            componentes.getOrDefault("respostaAosInimigos", 0)
        );
        int ameaca = dna == null ? 0 : dna.bonusRespostaAmeaca();
        int sinergiaGrupo = dna == null ? 0 : dna.bonusSinergiaGrupo();
        int especificidade = limitar(
            35
                + confronto * 2
                + resposta * 2
                + ameaca * 2
                + sinergiaGrupo
        );
        int segurancaBlind = limitar((int) Math.round(
            consistencia * 0.45
                + flexibilidade * 0.25
                + (100 - riscoCounters) * 0.20
                + valorTier(heroi.getDadosMeta().tier()) * 0.10
        ));

        List<String> motivos = new ArrayList<>();
        if (rotas > 1) {
            motivos.add(
                "Pode ocupar " + rotas + " rotas e esconder a distribuição da equipe."
            );
        }
        if (base.coberturaHipoteses() >= 90) {
            motivos.add("Permanece válido na maioria das distribuições de função.");
        }
        if (base.piorCenario() >= 60) {
            motivos.add("O pior cenário conhecido ainda é competitivo.");
        }
        if (riscoCounters >= 60) {
            motivos.add("Possui exposição relevante a respostas adversárias.");
        }
        if (especificidade >= 70) {
            motivos.add("Apresenta encaixe altamente específico para o draft atual.");
        }

        return new PerfilSegurancaBlindPickResponse(
            heroi.getNome(),
            segurancaBlind,
            flexibilidade,
            consistencia,
            riscoCounters,
            especificidade,
            motivos
        );
    }

    private int ajuste(
        MomentoDraft momento,
        PerfilSegurancaBlindPickResponse perfil,
        RecomendacaoPickResponse base,
        RecomendacaoDnaResponse dna
    ) {
        int confronto = Math.max(
            0,
            base.componentes().getOrDefault("confronto", 0)
        );
        int resposta = Math.max(
            0,
            base.componentes().getOrDefault("respostaAosInimigos", 0)
        );
        int ameaca = dna == null ? 0 : dna.bonusRespostaAmeaca();
        int grupo = dna == null ? 0 : dna.bonusSinergiaGrupo();
        int anti = dna == null ? 0 : dna.penalidadeAntiSinergia();

        int valor = switch (momento) {
            case BLIND_PICK_INICIAL ->
                Math.round((perfil.segurancaBlind() - 55) / 3.0f)
                    + Math.round((perfil.flexibilidade() - 50) / 10.0f)
                    - (perfil.riscoCounters() >= 65 ? 4 : 0);
            case PICK_INTERMEDIARIO ->
                Math.round((perfil.segurancaBlind() - 50) / 5.0f)
                    + Math.round((perfil.especificidade() - 50) / 8.0f)
                    + Math.min(3, grupo / 5);
            case COUNTER_PICK ->
                Math.round((perfil.especificidade() - 50) / 3.0f)
                    + confronto / 2
                    + resposta / 3
                    + ameaca / 3
                    - anti / 5;
            case LAST_PICK ->
                Math.round((perfil.especificidade() - 45) / 2.5f)
                    + confronto / 2
                    + resposta / 2
                    + ameaca / 2
                    + grupo / 3
                    - anti / 4;
        };

        return Math.max(-20, Math.min(20, valor));
    }

    private MomentoDraft momento(int ordem) {
        if (ordem <= 2) {
            return MomentoDraft.BLIND_PICK_INICIAL;
        }
        if (ordem == 3) {
            return MomentoDraft.PICK_INTERMEDIARIO;
        }
        if (ordem == 4) {
            return MomentoDraft.COUNTER_PICK;
        }
        return MomentoDraft.LAST_PICK;
    }

    private int inimigosRevelados(RecomendacaoProximoPickRequest request) {
        if (request.meuLado() == null) {
            return 0;
        }
        return request.meuLado() == LadoDraft.AZUL
            ? request.picksVermelho().size()
            : request.picksAzul().size();
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

    private int limitar(int valor) {
        return Math.max(0, Math.min(100, valor));
    }
}
