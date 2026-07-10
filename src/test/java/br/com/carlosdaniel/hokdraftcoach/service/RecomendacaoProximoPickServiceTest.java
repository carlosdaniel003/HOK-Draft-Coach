package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

class RecomendacaoProximoPickServiceTest {

    private RecomendacaoProximoPickService service;

    @BeforeEach
    void configurar() {
        HeroiService heroiService = new HeroiService();
        InferenciaFuncoesService inferenciaService =
            new InferenciaFuncoesService(heroiService);

        service = new RecomendacaoProximoPickService(
            heroiService,
            inferenciaService
        );
    }

    @Test
    void deveRecomendarHeroiQuandoForMinhaVez() {
        RecomendacaoProximoPickResponse resposta = service.recomendar(
            cenarioPrincipal(LadoDraft.AZUL, 3)
        );

        assertEquals("MINHA_VEZ", resposta.estadoDraft());
        assertTrue(resposta.ehMinhaVez());
        assertEquals("B3", resposta.meuSlot());
        assertEquals(LadoDraft.AZUL, resposta.proximoLado());
        assertTrue(resposta.proximosSlots().contains("B3"));
        assertNotNull(resposta.recomendacaoPrincipal());
        assertTrue(resposta.alternativas().size() <= 2);

        Set<Long> indisponiveis = Set.of(
            1L, 2L, 3L, 4L, 5L, 6L,
            8L, 20L, 17L
        );

        assertFalse(
            indisponiveis.contains(
                resposta.recomendacaoPrincipal().heroiId()
            )
        );
        validarRecomendacao(resposta.recomendacaoPrincipal());
    }

    @Test
    void deveGerarPlanejamentoQuandoAindaNaoForMinhaVez() {
        RecomendacaoProximoPickResponse resposta = service.recomendar(
            cenarioPrincipal(LadoDraft.VERMELHO, 5)
        );

        assertEquals("AGUARDANDO_INIMIGO", resposta.estadoDraft());
        assertFalse(resposta.ehMinhaVez());
        assertEquals("R5", resposta.meuSlot());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertTrue(
            resposta.avisos().stream()
                .anyMatch(aviso -> aviso.contains("preventiva"))
        );
    }

    @Test
    void deveContinuarRecomendandoParaAliadoDepoisDoMeuPick() {
        RecomendacaoProximoPickResponse resposta = service.recomendar(
            cenarioPrincipal(LadoDraft.AZUL, 1)
        );

        assertEquals("VEZ_ALIADA", resposta.estadoDraft());
        assertFalse(resposta.ehMinhaVez());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertTrue(resposta.mensagem().contains("B2"));
    }

    @Test
    void deveAguardarConclusaoDosBans() {
        RecomendacaoProximoPickRequest request = new RecomendacaoProximoPickRequest(
            LadoDraft.AZUL,
            1,
            List.of(1L),
            List.of(2L),
            List.of(),
            List.of(),
            List.of(Rota.MID_LANE)
        );

        RecomendacaoProximoPickResponse resposta = service.recomendar(request);

        assertEquals("FASE_DE_BANS", resposta.estadoDraft());
        assertNull(resposta.recomendacaoPrincipal());
        assertTrue(resposta.alternativas().isEmpty());
    }

    @Test
    void devePermitirMesmoHeroiBanidoUmaVezPorEquipe() {
        RecomendacaoProximoPickRequest request = new RecomendacaoProximoPickRequest(
            LadoDraft.AZUL,
            1,
            List.of(1L, 2L, 3L),
            List.of(1L, 4L, 5L),
            List.of(),
            List.of(),
            List.of(Rota.MID_LANE)
        );

        RecomendacaoProximoPickResponse resposta = service.recomendar(request);

        assertEquals("MINHA_VEZ", resposta.estadoDraft());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertFalse(resposta.recomendacaoPrincipal().heroiId().equals(1L));
    }

    @Test
    void deveRejeitarBanRepetidoNaMesmaEquipe() {
        RecomendacaoProximoPickRequest request = new RecomendacaoProximoPickRequest(
            LadoDraft.AZUL,
            1,
            List.of(1L, 1L, 2L),
            List.of(3L, 4L, 5L),
            List.of(),
            List.of(),
            List.of()
        );

        RegraNegocioException erro = assertThrows(
            RegraNegocioException.class,
            () -> service.recomendar(request)
        );

        assertTrue(erro.getMessage().contains("lado azul"));
    }

    @Test
    void deveRejeitarHeroiBanidoNosPicks() {
        RecomendacaoProximoPickRequest request = new RecomendacaoProximoPickRequest(
            LadoDraft.AZUL,
            2,
            List.of(1L, 2L, 3L),
            List.of(4L, 5L, 6L),
            List.of(new PickSemFuncaoRequest(1, 1L)),
            List.of(),
            List.of()
        );

        assertThrows(
            RegraNegocioException.class,
            () -> service.recomendar(request)
        );
    }

    @Test
    void deveBloquearComposicaoAliadaSemDistribuicaoValida() {
        RecomendacaoProximoPickRequest request = new RecomendacaoProximoPickRequest(
            LadoDraft.AZUL,
            3,
            List.of(1L, 2L, 3L),
            List.of(4L, 5L, 6L),
            List.of(
                new PickSemFuncaoRequest(1, 14L),
                new PickSemFuncaoRequest(2, 15L)
            ),
            List.of(
                new PickSemFuncaoRequest(1, 8L),
                new PickSemFuncaoRequest(2, 9L)
            ),
            List.of(Rota.MID_LANE)
        );

        RecomendacaoProximoPickResponse resposta = service.recomendar(request);

        assertEquals(
            "COMPOSICAO_ALIADA_INCOMPATIVEL",
            resposta.estadoDraft()
        );
        assertNull(resposta.recomendacaoPrincipal());
    }

    private RecomendacaoProximoPickRequest cenarioPrincipal(
        LadoDraft meuLado,
        int minhaOrdem
    ) {
        return new RecomendacaoProximoPickRequest(
            meuLado,
            minhaOrdem,
            List.of(1L, 2L, 3L),
            List.of(4L, 5L, 6L),
            List.of(new PickSemFuncaoRequest(1, 8L)),
            List.of(
                new PickSemFuncaoRequest(1, 20L),
                new PickSemFuncaoRequest(2, 17L)
            ),
            List.of(Rota.MID_LANE, Rota.JUNGLE)
        );
    }

    private void validarRecomendacao(RecomendacaoPickResponse recomendacao) {
        assertTrue(recomendacao.pontuacaoFinal() >= 0);
        assertTrue(recomendacao.pontuacaoFinal() <= 100);
        assertTrue(recomendacao.mediaCenarios() >= recomendacao.piorCenario());
        assertTrue(recomendacao.coberturaHipoteses() > 0);
        assertFalse(recomendacao.rotasRecomendadas().isEmpty());
        assertFalse(recomendacao.motivos().isEmpty());
        assertFalse(recomendacao.riscos().isEmpty());
    }
}
