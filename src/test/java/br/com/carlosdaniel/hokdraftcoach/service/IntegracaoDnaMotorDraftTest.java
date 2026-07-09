package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.EscolhaDraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;

class IntegracaoDnaMotorDraftTest {

    private HeroiService heroiService;
    private DraftDnaService draftDnaService;
    private RecomendacaoProximoPickDnaService proximoPickDnaService;

    @BeforeEach
    void configurar() {
        heroiService = new HeroiServiceCompleto(
            new CatalogoSuporteRepository(),
            new CatalogoMidRepository(),
            new CatalogoJungleRepository(),
            new CatalogoClashRepository()
        );
        DnaComposicaoService dnaService = new DnaComposicaoService(
            heroiService,
            new DnaHeroiService()
        );
        DraftService draftService = new DraftService(heroiService);
        draftDnaService = new DraftDnaService(
            draftService,
            heroiService,
            dnaService
        );

        InferenciaFuncoesService inferencia = new InferenciaFuncoesService(
            heroiService
        );
        RecomendacaoProximoPickService base =
            new RecomendacaoProximoPickService(heroiService, inferencia);
        proximoPickDnaService = new RecomendacaoProximoPickDnaService(
            base,
            heroiService,
            dnaService
        );
    }

    @Test
    void deveAnexarDiagnosticoEAjustarRecomendacaoPorRota() {
        DraftRequest request = new DraftRequest(
            Rota.ROAMING,
            List.of(
                new EscolhaDraftRequest(Rota.FARM_LANE, 7L),
                new EscolhaDraftRequest(Rota.MID_LANE, 14L)
            ),
            List.of(
                new EscolhaDraftRequest(Rota.CLASH_LANE, 200L)
            )
        );

        AnaliseDraftResponse resposta = draftDnaService.recomendar(request);

        assertNotNull(resposta.diagnosticoComposicao());
        assertTrue(resposta.diagnosticoComposicao().diagnosticoConcluido());
        assertTrue(resposta.recomendacoes().stream().allMatch(
            recomendacao ->
                recomendacao.componentes().containsKey("dnaComposicao")
        ));
    }

    @Test
    void deveDiagnosticarAntesDeOrdenarOProximoPick() {
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                2,
                List.of(1L, 2L, 3L),
                List.of(4L, 5L, 6L),
                List.of(new PickSemFuncaoRequest(1, 7L)),
                List.of(
                    new PickSemFuncaoRequest(1, 31L),
                    new PickSemFuncaoRequest(2, 17L)
                ),
                List.of(Rota.ROAMING, Rota.JUNGLE)
            );

        RecomendacaoProximoPickResponse resposta =
            proximoPickDnaService.recomendar(request);

        assertNotNull(resposta.diagnosticoComposicao());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertTrue(
            resposta.recomendacaoPrincipal()
                .componentes()
                .containsKey("dnaComposicao")
        );
        assertTrue(resposta.avisos().stream().anyMatch(
            aviso -> aviso.contains("DNA aliado e inimigo")
        ));
    }
}
