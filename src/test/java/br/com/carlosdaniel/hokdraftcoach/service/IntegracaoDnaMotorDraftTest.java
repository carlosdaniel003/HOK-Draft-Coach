package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import br.com.carlosdaniel.hokdraftcoach.model.TipoOpcaoPick;
import br.com.carlosdaniel.hokdraftcoach.repository.AntiSinergiaRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaGrupoRepository;

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
        DnaHeroiService dnaHeroi = new DnaHeroiService();
        EconomiaRecursosService economia = new EconomiaRecursosService();
        PerfilTemporalService temporal = new PerfilTemporalService();
        SinergiaGrupoService sinergias = new SinergiaGrupoService(
            new SinergiaGrupoRepository(),
            dnaHeroi
        );
        AntiSinergiaService antiSinergias = new AntiSinergiaService(
            new AntiSinergiaRepository(),
            dnaHeroi
        );
        AnaliseAmeacaService ameacas = new AnaliseAmeacaService(
            dnaHeroi,
            temporal,
            economia
        );
        AnaliseAmeacaComposicaoService dnaService =
            new AnaliseAmeacaComposicaoService(
                heroiService,
                dnaHeroi,
                new CondicaoVitoriaService(dnaHeroi, economia),
                economia,
                new NecessidadePenalidadeService(dnaHeroi, economia),
                temporal,
                sinergias,
                antiSinergias,
                ameacas
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
        ProjecaoRespostaInimigaService projecao =
            new ProjecaoRespostaInimigaService(
                heroiService,
                inferencia,
                dnaService,
                ameacas
            );
        proximoPickDnaService = new RecomendacaoProximoPickDnaService(
            base,
            heroiService,
            dnaService,
            new SegurancaBlindPickService(),
            projecao,
            new ExplicacaoRecomendacaoService()
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
        assertNotNull(
            resposta.diagnosticoComposicao().curvaPoderNossaComposicao()
        );
        assertNotNull(
            resposta.diagnosticoComposicao().analiseAmeacasInimigas()
        );
        assertTrue(resposta.recomendacoes().stream().allMatch(
            recomendacao ->
                recomendacao.componentes().containsKey("dnaComposicao")
                    && recomendacao.componentes().containsKey("curvaTemporal")
                    && recomendacao.componentes().containsKey("sinergiaGrupo")
                    && recomendacao.componentes().containsKey("antiSinergia")
                    && recomendacao.componentes().containsKey("respostaAmeaca")
        ));
    }

    @Test
    void deveProjetarRespostasInimigasDesdeOPrimeiroPick() {
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                1,
                List.of(1L, 2L, 3L),
                List.of(1L, 4L, 5L),
                List.of(),
                List.of(),
                List.of(Rota.ROAMING)
            );

        RecomendacaoProximoPickResponse resposta =
            proximoPickDnaService.recomendar(request);

        assertEquals("MINHA_VEZ", resposta.estadoDraft());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertEquals(3, resposta.opcoesEstrategicas().size());
        assertTrue(
            resposta.opcoesEstrategicas().stream().allMatch(
                opcao -> opcao.projecao() != null
                    && !opcao.projecao().respostasProvaveis().isEmpty()
            )
        );
    }

    @Test
    void deveManterDnaEProjecoesDepoisDoPickDoUsuario() {
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                1,
                List.of(1L, 2L, 3L),
                List.of(1L, 4L, 5L),
                List.of(new PickSemFuncaoRequest(1, 7L)),
                List.of(
                    new PickSemFuncaoRequest(1, 31L),
                    new PickSemFuncaoRequest(2, 17L)
                ),
                List.of(Rota.ROAMING)
            );

        RecomendacaoProximoPickResponse resposta =
            proximoPickDnaService.recomendar(request);

        assertEquals("VEZ_ALIADA", resposta.estadoDraft());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertEquals(3, resposta.opcoesEstrategicas().size());
        assertTrue(resposta.opcoesEstrategicas().stream().allMatch(
            opcao -> opcao.projecao() != null
        ));
    }

    @Test
    void deveProjetarRespostasEOferecerTresOpcoesExplicadas() {
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                2,
                List.of(1L, 2L, 3L),
                List.of(1L, 4L, 5L),
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
        assertNotNull(resposta.contextoDraft());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertNotNull(resposta.recomendacaoPrincipal().perfilBlindPick());
        assertEquals(3, resposta.opcoesEstrategicas().size());
        assertEquals(
            TipoOpcaoPick.MELHOR_GERAL,
            resposta.opcoesEstrategicas().get(0).tipo()
        );
        assertEquals(
            TipoOpcaoPick.MAIS_SEGURA,
            resposta.opcoesEstrategicas().get(1).tipo()
        );
        assertEquals(
            TipoOpcaoPick.MAIOR_IMPACTO,
            resposta.opcoesEstrategicas().get(2).tipo()
        );
        assertTrue(resposta.opcoesEstrategicas().stream().allMatch(opcao ->
            opcao.projecao() != null
                && opcao.explicacao() != null
                && !opcao.explicacao().resumo().isBlank()
        ));
        assertTrue(
            resposta.recomendacaoPrincipal()
                .componentes()
                .containsKey("ajusteProjecao")
        );
        assertTrue(resposta.avisos().stream().anyMatch(
            aviso -> aviso.contains("probabilidades")
        ));
    }
}
