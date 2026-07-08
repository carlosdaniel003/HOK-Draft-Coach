package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.EscolhaDraftRequest;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

class DraftServiceTest {

    private DraftService draftService;

    @BeforeEach
    void configurar() {
        draftService = new DraftService(new HeroiService());
    }

    @Test
    void deveRecomendarHeroisDisponiveisDaRotaAlvo() {
        DraftRequest request = new DraftRequest(
            Rota.FARM_LANE,
            List.of(
                escolha(Rota.CLASH_LANE, 8L),
                escolha(Rota.JUNGLE, 11L),
                escolha(Rota.MID_LANE, 14L),
                escolha(Rota.ROAMING, 17L)
            ),
            List.of(
                escolha(Rota.CLASH_LANE, 9L),
                escolha(Rota.JUNGLE, 12L),
                escolha(Rota.MID_LANE, 15L),
                escolha(Rota.FARM_LANE, 1L),
                escolha(Rota.ROAMING, 18L)
            )
        );

        AnaliseDraftResponse resposta = draftService.recomendar(request);
        Set<Long> idsSelecionados = Set.of(
            8L, 11L, 14L, 17L,
            9L, 12L, 15L, 1L, 18L
        );

        assertEquals(Rota.FARM_LANE, resposta.rotaAlvo());
        assertEquals(6, resposta.totalCandidatos());
        assertFalse(resposta.recomendacoes().isEmpty());
        assertTrue(
            resposta.recomendacoes().stream()
                .allMatch(item -> item.rota() == Rota.FARM_LANE)
        );
        assertTrue(
            resposta.recomendacoes().stream()
                .noneMatch(item -> idsSelecionados.contains(item.heroiId()))
        );
        assertTrue(estaOrdenadaPorPontuacao(resposta));
    }

    @Test
    void deveRejeitarHeroiRepetidoEntreAsEquipes() {
        DraftRequest request = new DraftRequest(
            Rota.FARM_LANE,
            List.of(escolha(Rota.CLASH_LANE, 8L)),
            List.of(escolha(Rota.CLASH_LANE, 8L))
        );

        assertThrows(
            RegraNegocioException.class,
            () -> draftService.recomendar(request)
        );
    }

    @Test
    void deveRejeitarRotaAlvoJaPreenchida() {
        DraftRequest request = new DraftRequest(
            Rota.FARM_LANE,
            List.of(escolha(Rota.FARM_LANE, 2L)),
            List.of(escolha(Rota.FARM_LANE, 1L))
        );

        assertThrows(
            RegraNegocioException.class,
            () -> draftService.recomendar(request)
        );
    }

    private EscolhaDraftRequest escolha(Rota rota, Long heroiId) {
        return new EscolhaDraftRequest(rota, heroiId);
    }

    private boolean estaOrdenadaPorPontuacao(
        AnaliseDraftResponse resposta
    ) {
        for (int indice = 1; indice < resposta.recomendacoes().size(); indice++) {
            int anterior = resposta.recomendacoes()
                .get(indice - 1)
                .pontuacaoFinal();
            int atual = resposta.recomendacoes()
                .get(indice)
                .pontuacaoFinal();

            if (anterior < atual) {
                return false;
            }
        }

        return true;
    }
}
