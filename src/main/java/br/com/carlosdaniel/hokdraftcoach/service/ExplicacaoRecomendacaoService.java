package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.ExplicacaoRecomendacaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.NecessidadeComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.ProjecaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RespostaInimigaProjetadaResponse;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.TipoOpcaoPick;

@Service
public class ExplicacaoRecomendacaoService {

    private static final Map<DimensaoEstrategica, String> NOMES = nomes();

    public ExplicacaoRecomendacaoResponse explicar(
        TipoOpcaoPick tipo,
        RecomendacaoPickResponse pick,
        RecomendacaoDnaResponse dna,
        DiagnosticoComposicaoResponse diagnostico,
        ProjecaoPickResponse projecao
    ) {
        List<String> leituraInimiga = leituraInimiga(diagnostico);
        List<String> leituraAliada = leituraAliada(diagnostico);
        List<String> porQueFunciona = porQueFunciona(
            tipo,
            pick,
            dna,
            diagnostico,
            projecao
        );
        List<String> riscos = riscos(pick, dna, projecao);
        String resumo = resumo(
            tipo,
            pick,
            dna,
            diagnostico,
            projecao
        );
        String plano = planoDeJogo(pick, dna, diagnostico, projecao);

        return new ExplicacaoRecomendacaoResponse(
            resumo,
            leituraInimiga,
            leituraAliada,
            porQueFunciona,
            riscos,
            plano
        );
    }

    private List<String> leituraInimiga(
        DiagnosticoComposicaoResponse diagnostico
    ) {
        if (
            diagnostico == null
                || diagnostico.composicaoInimiga().herois().isEmpty()
        ) {
            return List.of(
                "A composição inimiga ainda possui pouca informação; a leitura prioriza respostas genéricas e segurança."
            );
        }

        List<String> resultado = new ArrayList<>();
        List<String> forcas = dimensoesFortes(
            diagnostico.composicaoInimiga(),
            3
        );
        if (!forcas.isEmpty()) {
            resultado.add(
                "A equipe inimiga possui " + juntar(forcas) + "."
            );
        }
        if (
            diagnostico.analiseAmeacasInimigas() != null
                && diagnostico.analiseAmeacasInimigas().maiorAmeaca() != null
        ) {
            var ameacas = diagnostico.analiseAmeacasInimigas();
            String frase = ameacas.maiorAmeaca().heroi()
                + " é a maior ameaça";
            if (ameacas.iniciadorPrincipal() != null) {
                frase += ", enquanto "
                    + ameacas.iniciadorPrincipal().heroi()
                    + " cria a principal janela de luta";
            }
            if (ameacas.habilitadorCritico() != null) {
                frase += " e " + ameacas.habilitadorCritico().heroi()
                    + " amplifica a execução";
            }
            resultado.add(frase + ".");
        }
        diagnostico.condicoesVitoriaInimigas().stream()
            .filter(condicao -> condicao.principal())
            .findFirst()
            .ifPresent(condicao -> resultado.add(
                "A condição de vitória inimiga é "
                    + condicao.titulo().toLowerCase() + "."
            ));
        return resultado.stream().distinct().limit(3).toList();
    }

    private List<String> leituraAliada(
        DiagnosticoComposicaoResponse diagnostico
    ) {
        if (diagnostico == null) {
            return List.of();
        }
        List<String> resultado = new ArrayList<>();
        DnaComposicao nossa = diagnostico.nossaComposicao();
        List<String> forcas = dimensoesFortes(nossa, 2);
        List<NecessidadeComposicaoResponse> necessidades =
            diagnostico.necessidades().stream()
                .sorted(
                    Comparator.comparingInt(
                        NecessidadeComposicaoResponse::urgencia
                    ).reversed()
                )
                .limit(2)
                .toList();

        boolean danoSuficiente = nossa.valor(DimensaoEstrategica.DPS) >= 60
            || nossa.valor(DimensaoEstrategica.EXPLOSAO) >= 65;
        boolean faltaProtecao = nossa.valor(DimensaoEstrategica.PEEL) < 48
            && nossa.valor(DimensaoEstrategica.PROTECAO) < 48;
        if (danoSuficiente && faltaProtecao) {
            resultado.add(
                "Sua equipe possui dano suficiente, mas não tem proteção consistente para o ADC ou para a retaguarda."
            );
        } else if (!forcas.isEmpty() && !necessidades.isEmpty()) {
            resultado.add(
                "Sua equipe já possui " + juntar(forcas)
                    + ", mas ainda precisa de "
                    + necessidades.getFirst().titulo().toLowerCase() + "."
            );
        } else if (!forcas.isEmpty()) {
            resultado.add(
                "Os principais pontos fortes aliados são "
                    + juntar(forcas) + "."
            );
        }
        necessidades.forEach(necessidade -> resultado.add(
            necessidade.titulo() + ": " + necessidade.motivo()
        ));
        if (!diagnostico.penalidades().isEmpty()) {
            resultado.add(
                "A principal fragilidade estrutural atual é: "
                    + diagnostico.penalidades().getFirst().titulo() + "."
            );
        }
        return resultado.stream().distinct().limit(4).toList();
    }

    private List<String> porQueFunciona(
        TipoOpcaoPick tipo,
        RecomendacaoPickResponse pick,
        RecomendacaoDnaResponse dna,
        DiagnosticoComposicaoResponse diagnostico,
        ProjecaoPickResponse projecao
    ) {
        List<String> resultado = new ArrayList<>();
        switch (tipo) {
            case MELHOR_GERAL -> resultado.add(
                "É a escolha com o melhor equilíbrio entre encaixe, segurança, resposta inimiga e pior cenário projetado."
            );
            case MAIS_SEGURA -> resultado.add(
                "Mantém valor mesmo se o inimigo responder corretamente e oferece baixa exposição a counters graves."
            );
            case MAIOR_IMPACTO -> resultado.add(
                "Possui o maior potencial de desmontar a condição inimiga, embora dependa mais da execução e do cenário."
            );
        }
        if (dna != null) {
            if (!dna.corrige().isEmpty()) {
                resultado.add(
                    pick.heroi() + " corrige "
                        + juntarDimensoes(dna.corrige()) + "."
                );
            }
            if (!dna.explora().isEmpty()) {
                resultado.add(
                    "Explora as fraquezas inimigas em "
                        + juntarDimensoes(dna.explora()) + "."
                );
            }
            if (!dna.alvosAmeacaRespondidos().isEmpty()) {
                resultado.add(
                    "A escolha responde diretamente a "
                        + juntar(dna.alvosAmeacaRespondidos()) + "."
                );
            }
            if (dna.bonusSinergiaGrupo() > 0) {
                resultado.add(
                    "Completa ou fortalece uma sinergia de três ou mais heróis."
                );
            }
            if (dna.ajusteTemporal() > 0) {
                resultado.add(
                    "Melhora a fase da partida em que a composição está mais vulnerável."
                );
            }
        }
        if (pick.perfilBlindPick() != null) {
            resultado.add(
                "Segurança de blind pick: "
                    + pick.perfilBlindPick().segurancaBlind()
                    + "/100; especificidade para este draft: "
                    + pick.perfilBlindPick().especificidade() + "/100."
            );
        }
        resultado.add(
            "Robustez após respostas projetadas: "
                + projecao.robustez() + "/100."
        );
        return resultado.stream().distinct().limit(6).toList();
    }

    private List<String> riscos(
        RecomendacaoPickResponse pick,
        RecomendacaoDnaResponse dna,
        ProjecaoPickResponse projecao
    ) {
        List<String> resultado = new ArrayList<>(pick.riscos());
        if (dna != null) {
            resultado.addAll(dna.alertas());
        }
        if (!projecao.respostasProvaveis().isEmpty()) {
            RespostaInimigaProjetadaResponse pior = projecao.respostasProvaveis()
                .stream()
                .max(Comparator.comparingInt(
                    RespostaInimigaProjetadaResponse::impactoContraNossaComposicao
                ))
                .orElse(projecao.respostasProvaveis().getFirst());
            resultado.add(
                "A resposta inimiga mais perigosa é " + pior.heroi()
                    + " em " + pior.rota()
                    + ", com impacto estimado de "
                    + pior.impactoContraNossaComposicao() + "/100."
            );
        }
        if (projecao.piorCenarioProjetado() < 50) {
            resultado.add(
                "O pior cenário projetado é frágil; a escolha exige vantagem de execução ou informação adicional."
            );
        }
        return resultado.stream().distinct().limit(6).toList();
    }

    private String resumo(
        TipoOpcaoPick tipo,
        RecomendacaoPickResponse pick,
        RecomendacaoDnaResponse dna,
        DiagnosticoComposicaoResponse diagnostico,
        ProjecaoPickResponse projecao
    ) {
        String categoria = switch (tipo) {
            case MELHOR_GERAL -> "melhor escolha geral";
            case MAIS_SEGURA -> "escolha mais segura";
            case MAIOR_IMPACTO -> "escolha de maior impacto";
        };
        String foco = "equilibra as necessidades atuais";
        if (dna != null && !dna.alvosAmeacaRespondidos().isEmpty()) {
            foco = "neutraliza " + juntar(dna.alvosAmeacaRespondidos());
        } else if (dna != null && !dna.corrige().isEmpty()) {
            foco = "corrige " + juntarDimensoes(dna.corrige());
        } else if (
            diagnostico != null
                && !diagnostico.necessidades().isEmpty()
        ) {
            foco = diagnostico.necessidades().getFirst().titulo()
                .toLowerCase();
        }
        return pick.heroi() + " é a " + categoria + " porque " + foco
            + " e mantém " + projecao.piorCenarioProjetado()
            + "/100 no pior cenário projetado.";
    }

    private String planoDeJogo(
        RecomendacaoPickResponse pick,
        RecomendacaoDnaResponse dna,
        DiagnosticoComposicaoResponse diagnostico,
        ProjecaoPickResponse projecao
    ) {
        if (dna != null && !dna.alvosAmeacaRespondidos().isEmpty()) {
            return "Jogue para impedir a ativação de "
                + juntar(dna.alvosAmeacaRespondidos())
                + ", preserve a habilidade de resposta para a primeira entrada e só depois converta em objetivo.";
        }
        if (
            diagnostico != null
                && !diagnostico.nossasCondicoesVitoria().isEmpty()
        ) {
            return "Use " + pick.heroi() + " para fortalecer o plano: "
                + diagnostico.nossasCondicoesVitoria().getFirst().descricao();
        }
        if (!projecao.respostasProvaveis().isEmpty()) {
            return "Prepare o draft e a execução para a resposta provável de "
                + projecao.respostasProvaveis().getFirst().heroi()
                + ", evitando entregar a janela de counter projetada.";
        }
        return "Use a escolha para completar a estrutura da equipe e preserve os recursos decisivos para a primeira luta por objetivo.";
    }

    private List<String> dimensoesFortes(
        DnaComposicao composicao,
        int limite
    ) {
        return composicao.vetor().entrySet().stream()
            .filter(entrada -> entrada.getValue() >= 62)
            .filter(entrada -> dimensaoNarrativa(entrada.getKey()))
            .sorted(Map.Entry.<DimensaoEstrategica, Integer>comparingByValue()
                .reversed())
            .limit(limite)
            .map(entrada -> NOMES.get(entrada.getKey()))
            .toList();
    }

    private boolean dimensaoNarrativa(DimensaoEstrategica dimensao) {
        return switch (dimensao) {
            case DIVE, CONTROLE, EXPLOSAO, ENGAGE, POKE, DPS,
                LINHA_DE_FRENTE, SUSTAIN, PEEL, PROTECAO,
                WAVE_CLEAR, OBJETIVOS, PRESSAO_LATERAL -> true;
            default -> false;
        };
    }

    private String juntarDimensoes(List<DimensaoEstrategica> dimensoes) {
        return juntar(dimensoes.stream()
            .map(dimensao -> NOMES.getOrDefault(
                dimensao,
                dimensao.name().toLowerCase()
            ))
            .toList());
    }

    private String juntar(List<String> valores) {
        List<String> unicos = valores.stream().distinct().toList();
        if (unicos.isEmpty()) {
            return "nenhuma dimensão específica";
        }
        if (unicos.size() == 1) {
            return unicos.getFirst();
        }
        if (unicos.size() == 2) {
            return unicos.getFirst() + " e " + unicos.getLast();
        }
        return String.join(", ", unicos.subList(0, unicos.size() - 1))
            + " e " + unicos.getLast();
    }

    private static Map<DimensaoEstrategica, String> nomes() {
        Map<DimensaoEstrategica, String> nomes = new LinkedHashMap<>();
        nomes.put(DimensaoEstrategica.ENGAGE, "iniciação");
        nomes.put(DimensaoEstrategica.DESENGAGE, "desengage");
        nomes.put(DimensaoEstrategica.PEEL, "proteção da retaguarda");
        nomes.put(DimensaoEstrategica.POKE, "poke");
        nomes.put(DimensaoEstrategica.EXPLOSAO, "alto dano explosivo");
        nomes.put(DimensaoEstrategica.DPS, "dano sustentado");
        nomes.put(DimensaoEstrategica.LINHA_DE_FRENTE, "linha de frente");
        nomes.put(DimensaoEstrategica.SUSTAIN, "sustentação");
        nomes.put(DimensaoEstrategica.WAVE_CLEAR, "limpeza de ondas");
        nomes.put(DimensaoEstrategica.OBJETIVOS, "controle de objetivos");
        nomes.put(DimensaoEstrategica.CONTROLE, "controle em área");
        nomes.put(DimensaoEstrategica.MOBILIDADE, "mobilidade");
        nomes.put(DimensaoEstrategica.ALCANCE, "alcance");
        nomes.put(DimensaoEstrategica.ANTI_TANQUE, "dano anti-tanque");
        nomes.put(DimensaoEstrategica.ANTI_CURA, "anti-cura");
        nomes.put(DimensaoEstrategica.PRESSAO_LATERAL, "pressão lateral");
        nomes.put(DimensaoEstrategica.DIVE, "dive");
        nomes.put(DimensaoEstrategica.PROTECAO, "proteção");
        nomes.put(DimensaoEstrategica.ESCALAMENTO, "escalamento");
        return Map.copyOf(nomes);
    }
}
