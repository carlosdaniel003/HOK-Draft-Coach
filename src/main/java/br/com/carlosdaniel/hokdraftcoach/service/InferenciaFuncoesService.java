package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AmbiguidadeFuncaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AtribuicaoFuncaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.HipoteseFuncaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaEquipeResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class InferenciaFuncoesService {

    private static final String VERSAO_MOTOR = "FUNCOES-0.1";
    private static final int LIMITE_HIPOTESES_RETORNADAS = 10;
    private static final int PONTOS_ROTA_PRINCIPAL = 20;
    private static final int PONTOS_ROTA_SECUNDARIA = 12;

    private final HeroiService heroiService;

    public InferenciaFuncoesService(HeroiService heroiService) {
        this.heroiService = heroiService;
    }

    public InferenciaFuncoesResponse inferir(
        InferenciaFuncoesRequest request
    ) {
        List<PickInterno> picksAzul = carregarPicks(
            request.picksAzul(),
            "B"
        );
        List<PickInterno> picksVermelho = carregarPicks(
            request.picksVermelho(),
            "R"
        );

        validarHeroisRepetidos(picksAzul, picksVermelho);

        InferenciaEquipeResponse equipeAzul = inferirEquipe(
            "AZUL",
            picksAzul
        );
        InferenciaEquipeResponse equipeVermelha = inferirEquipe(
            "VERMELHO",
            picksVermelho
        );

        List<String> avisos = new ArrayList<>();
        avisos.add(
            "As funções são hipóteses calculadas a partir das rotas cadastradas de cada herói."
        );

        if (
            equipeAzul.totalPicks() < 5
                || equipeVermelha.totalPicks() < 5
        ) {
            avisos.add(
                "O draft está incompleto; novas escolhas podem confirmar ou invalidar hipóteses atuais."
            );
        }

        return new InferenciaFuncoesResponse(
            VERSAO_MOTOR,
            equipeAzul,
            equipeVermelha,
            avisos
        );
    }

    private InferenciaEquipeResponse inferirEquipe(
        String lado,
        List<PickInterno> picks
    ) {
        List<HipoteseInterna> hipoteses = new ArrayList<>();

        gerarHipoteses(
            picks,
            0,
            EnumSet.noneOf(Rota.class),
            new ArrayList<>(),
            0,
            hipoteses
        );

        hipoteses.sort(
            Comparator
                .comparingInt(HipoteseInterna::pontuacaoBruta)
                .reversed()
                .thenComparing(this::chaveOrdenacao)
        );

        boolean compativel = !hipoteses.isEmpty();
        String confianca = calcularConfianca(picks, hipoteses);
        List<AmbiguidadeFuncaoResponse> ambiguidades =
            calcularAmbiguidades(picks, hipoteses);
        List<HipoteseFuncaoResponse> respostas =
            converterHipoteses(picks, hipoteses);
        List<String> avisos = criarAvisosEquipe(
            picks,
            hipoteses,
            ambiguidades
        );

        return new InferenciaEquipeResponse(
            lado,
            picks.size(),
            hipoteses.size(),
            compativel,
            confianca,
            ambiguidades,
            respostas,
            avisos
        );
    }

    private void gerarHipoteses(
        List<PickInterno> picks,
        int indice,
        Set<Rota> rotasOcupadas,
        List<AtribuicaoInterna> atribuicoes,
        int pontuacao,
        List<HipoteseInterna> resultado
    ) {
        if (indice == picks.size()) {
            resultado.add(
                new HipoteseInterna(
                    List.copyOf(atribuicoes),
                    pontuacao
                )
            );
            return;
        }

        PickInterno pick = picks.get(indice);
        List<Rota> rotasOrdenadas = ordenarRotas(pick.heroi());

        for (Rota rota : rotasOrdenadas) {
            if (rotasOcupadas.contains(rota)) {
                continue;
            }

            rotasOcupadas.add(rota);
            atribuicoes.add(new AtribuicaoInterna(pick, rota));

            int bonus = rota == pick.heroi().getRota()
                ? PONTOS_ROTA_PRINCIPAL
                : PONTOS_ROTA_SECUNDARIA;

            gerarHipoteses(
                picks,
                indice + 1,
                rotasOcupadas,
                atribuicoes,
                pontuacao + bonus,
                resultado
            );

            atribuicoes.remove(atribuicoes.size() - 1);
            rotasOcupadas.remove(rota);
        }
    }

    private List<Rota> ordenarRotas(Heroi heroi) {
        return heroi.getRotasPossiveis()
            .stream()
            .sorted(
                Comparator.comparingInt(
                    rota -> rota == heroi.getRota() ? 0 : 1
                )
            )
            .toList();
    }

    private List<HipoteseFuncaoResponse> converterHipoteses(
        List<PickInterno> picks,
        List<HipoteseInterna> hipoteses
    ) {
        List<HipoteseFuncaoResponse> respostas = new ArrayList<>();
        int limite = Math.min(
            hipoteses.size(),
            LIMITE_HIPOTESES_RETORNADAS
        );

        for (int indice = 0; indice < limite; indice++) {
            HipoteseInterna hipotese = hipoteses.get(indice);
            List<AtribuicaoFuncaoResponse> atribuicoes = hipotese
                .atribuicoes()
                .stream()
                .map(atribuicao -> new AtribuicaoFuncaoResponse(
                    atribuicao.pick().slot(),
                    atribuicao.pick().heroi().getId(),
                    atribuicao.pick().heroi().getNome(),
                    atribuicao.rota(),
                    atribuicao.rota()
                        == atribuicao.pick().heroi().getRota(),
                    atribuicao.pick().heroi().isFlex()
                ))
                .toList();

            respostas.add(
                new HipoteseFuncaoResponse(
                    indice + 1,
                    calcularPontuacaoAfinidade(
                        picks.size(),
                        hipotese.pontuacaoBruta()
                    ),
                    indice == 0,
                    atribuicoes,
                    calcularRotasAbertas(hipotese)
                )
            );
        }

        return respostas;
    }

    private List<AmbiguidadeFuncaoResponse> calcularAmbiguidades(
        List<PickInterno> picks,
        List<HipoteseInterna> hipoteses
    ) {
        Map<String, LinkedHashSet<Rota>> funcoesPorSlot =
            new LinkedHashMap<>();

        for (PickInterno pick : picks) {
            funcoesPorSlot.put(pick.slot(), new LinkedHashSet<>());
        }

        for (HipoteseInterna hipotese : hipoteses) {
            for (AtribuicaoInterna atribuicao : hipotese.atribuicoes()) {
                funcoesPorSlot
                    .get(atribuicao.pick().slot())
                    .add(atribuicao.rota());
            }
        }

        List<AmbiguidadeFuncaoResponse> respostas = new ArrayList<>();

        for (PickInterno pick : picks) {
            List<Rota> funcoes = List.copyOf(
                funcoesPorSlot.get(pick.slot())
            );

            if (funcoes.isEmpty()) {
                funcoes = pick.heroi().getRotasPossiveis();
            }

            respostas.add(
                new AmbiguidadeFuncaoResponse(
                    pick.slot(),
                    pick.heroi().getId(),
                    pick.heroi().getNome(),
                    funcoes.size() == 1 && !hipoteses.isEmpty(),
                    funcoes
                )
            );
        }

        return respostas;
    }

    private String calcularConfianca(
        List<PickInterno> picks,
        List<HipoteseInterna> hipoteses
    ) {
        if (picks.isEmpty()) {
            return "INDEFINIDA";
        }

        if (hipoteses.isEmpty()) {
            return "INCOMPATIVEL";
        }

        if (hipoteses.size() == 1) {
            return "ALTA";
        }

        int melhor = hipoteses.get(0).pontuacaoBruta();
        long empatadas = hipoteses.stream()
            .filter(hipotese -> hipotese.pontuacaoBruta() == melhor)
            .count();

        if (empatadas == 1) {
            return "MEDIA";
        }

        return "BAIXA";
    }

    private List<String> criarAvisosEquipe(
        List<PickInterno> picks,
        List<HipoteseInterna> hipoteses,
        List<AmbiguidadeFuncaoResponse> ambiguidades
    ) {
        List<String> avisos = new ArrayList<>();

        if (picks.isEmpty()) {
            avisos.add("Nenhum pick registrado para esta equipe.");
            return avisos;
        }

        if (hipoteses.isEmpty()) {
            avisos.add(
                "Não existe distribuição válida sem repetir função com os dados atuais."
            );
            return avisos;
        }

        long funcoesIncertas = ambiguidades.stream()
            .filter(item -> !item.funcaoConfirmada())
            .count();

        if (funcoesIncertas > 0) {
            avisos.add(
                funcoesIncertas
                    + " pick(s) ainda possuem mais de uma função possível."
            );
        }

        if (picks.size() < 5) {
            avisos.add(
                "Ainda existem " + (5 - picks.size())
                    + " slot(s) de equipe sem pick."
            );
        }

        if (hipoteses.size() > LIMITE_HIPOTESES_RETORNADAS) {
            avisos.add(
                "Foram retornadas apenas as "
                    + LIMITE_HIPOTESES_RETORNADAS
                    + " hipóteses mais bem classificadas."
            );
        }

        return avisos;
    }

    private List<Rota> calcularRotasAbertas(
        HipoteseInterna hipotese
    ) {
        EnumSet<Rota> abertas = EnumSet.allOf(Rota.class);

        hipotese.atribuicoes()
            .forEach(atribuicao -> abertas.remove(atribuicao.rota()));

        return List.copyOf(abertas);
    }

    private int calcularPontuacaoAfinidade(
        int totalPicks,
        int pontuacaoBruta
    ) {
        if (totalPicks == 0) {
            return 0;
        }

        double maximo = totalPicks * PONTOS_ROTA_PRINCIPAL;
        return (int) Math.round((pontuacaoBruta / maximo) * 100);
    }

    private String chaveOrdenacao(HipoteseInterna hipotese) {
        return hipotese.atribuicoes()
            .stream()
            .map(atribuicao ->
                atribuicao.pick().slot() + ":" + atribuicao.rota().name()
            )
            .sorted()
            .reduce("", (atual, item) -> atual + "|" + item);
    }

    private List<PickInterno> carregarPicks(
        List<PickSemFuncaoRequest> requests,
        String prefixo
    ) {
        List<PickInterno> picks = new ArrayList<>();

        for (int indice = 0; indice < requests.size(); indice++) {
            Long heroiId = requests.get(indice).heroiId();
            Heroi heroi = heroiService.buscarPorId(heroiId)
                .orElseThrow(() -> new RegraNegocioException(
                    "Herói de ID " + heroiId + " não encontrado."
                ));

            picks.add(
                new PickInterno(
                    prefixo + (indice + 1),
                    heroi
                )
            );
        }

        return picks;
    }

    private void validarHeroisRepetidos(
        List<PickInterno> picksAzul,
        List<PickInterno> picksVermelho
    ) {
        Set<Long> ids = new HashSet<>();

        for (PickInterno pick : picksAzul) {
            validarHeroiUnico(ids, pick);
        }

        for (PickInterno pick : picksVermelho) {
            validarHeroiUnico(ids, pick);
        }
    }

    private void validarHeroiUnico(
        Set<Long> ids,
        PickInterno pick
    ) {
        if (!ids.add(pick.heroi().getId())) {
            throw new RegraNegocioException(
                "O herói " + pick.heroi().getNome()
                    + " foi selecionado mais de uma vez."
            );
        }
    }

    private record PickInterno(
        String slot,
        Heroi heroi
    ) {
    }

    private record AtribuicaoInterna(
        PickInterno pick,
        Rota rota
    ) {
    }

    private record HipoteseInterna(
        List<AtribuicaoInterna> atribuicoes,
        int pontuacaoBruta
    ) {
    }
}
