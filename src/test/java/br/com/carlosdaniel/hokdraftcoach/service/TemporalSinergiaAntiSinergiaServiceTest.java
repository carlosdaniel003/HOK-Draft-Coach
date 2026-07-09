package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.model.FaseJogo;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilTemporalHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TipoAntiSinergia;
import br.com.carlosdaniel.hokdraftcoach.model.TipoSinergiaGrupo;
import br.com.carlosdaniel.hokdraftcoach.repository.AntiSinergiaRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaGrupoRepository;

class TemporalSinergiaAntiSinergiaServiceTest {

    private HeroiService heroiService;
    private AnaliseTemporalSinergiaService service;

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
        service = new AnaliseTemporalSinergiaService(
            heroiService,
            dnaHeroi,
            new CondicaoVitoriaService(dnaHeroi, economia),
            economia,
            new NecessidadePenalidadeService(dnaHeroi, economia),
            new PerfilTemporalService(),
            new SinergiaGrupoService(
                new SinergiaGrupoRepository(),
                dnaHeroi
            ),
            new AntiSinergiaService(
                new AntiSinergiaRepository(),
                dnaHeroi
            )
        );
    }

    @Test
    void todoHeroiDevePossuirCurvaTemporalValida() {
        heroiService.listarTodos().forEach(heroi -> {
            PerfilTemporalHeroi perfil = service.perfilTemporal(
                heroi.getNome()
            );

            assertTrue(perfil.earlyGame() >= 0 && perfil.earlyGame() <= 100);
            assertTrue(perfil.midGame() >= 0 && perfil.midGame() <= 100);
            assertTrue(perfil.lateGame() >= 0 && perfil.lateGame() <= 100);
            assertTrue(perfil.pico() != null);
        });
    }

    @Test
    void deveIdentificarEquipeInteiraFracaNoInicio() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Garo", "Hou Yi", "Huang Zhong"),
            List.of()
        );

        assertTrue(resposta.diagnosticosTemporais().stream().anyMatch(
            diagnostico -> diagnostico.codigo()
                .equals("TODOS_FRACOS_NO_INICIO")
        ));
        assertTrue(
            resposta.curvaPoderNossaComposicao().pico()
                == FaseJogo.LATE_GAME
        );
    }

    @Test
    void deveIndicarQueComposicaoPrecisaTerminarCedo() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Fang", "Liu Shan", "Guiguzi"),
            List.of("Garo", "Hou Yi", "Cai Yan")
        );

        assertTrue(resposta.diagnosticosTemporais().stream().anyMatch(
            diagnostico -> diagnostico.codigo()
                .equals("PRECISA_TERMINAR_CEDO")
        ));
        assertTrue(
            resposta.curvaPoderNossaComposicao().earlyGame()
                > resposta.curvaPoderComposicaoInimiga().earlyGame()
        );
        assertTrue(
            resposta.curvaPoderNossaComposicao().lateGame()
                < resposta.curvaPoderComposicaoInimiga().lateGame()
        );
    }

    @Test
    void deveAtivarLianPoMarcoPoloDolia() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Lian Po", "Marco Polo", "Dolia"),
            List.of()
        );

        assertTrue(resposta.sinergiasGrupoNossaComposicao().stream().anyMatch(
            sinergia -> sinergia.ativa()
                && sinergia.codigo().equals("LIAN_PO_MARCO_POLO_DOLIA")
                && sinergia.tipo() == TipoSinergiaGrupo.RESET_DE_HABILIDADES
        ));
    }

    @Test
    void deveReconhecerGuiguziMagoAreaEAdcAreaPorCapacidade() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Guiguzi", "Wang Zhaojun", "Marco Polo"),
            List.of()
        );

        assertTrue(resposta.sinergiasGrupoNossaComposicao().stream().anyMatch(
            sinergia -> sinergia.ativa()
                && sinergia.codigo().equals("GUIGUZI_MAGO_AREA_ADC_AREA")
        ));
    }

    @Test
    void doliaDeveReceberBonusAoCompletarComboDeTres() {
        List<RecomendacaoDnaResponse> recomendacoes = service.recomendar(
            List.of("Lian Po", "Marco Polo"),
            List.of(),
            Rota.ROAMING,
            50
        );

        RecomendacaoDnaResponse dolia = recomendacoes.stream()
            .filter(recomendacao -> recomendacao.heroi().equals("Dolia"))
            .findFirst()
            .orElseThrow();

        assertTrue(dolia.bonusSinergiaGrupo() >= 15);
        assertTrue(dolia.motivos().stream().anyMatch(
            motivo -> motivo.contains("LIAN_PO_MARCO_POLO_DOLIA")
        ));
    }

    @Test
    void deveDetectarSuporteQueNaoAcompanhaAdcMuitoMovel() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Arli", "Cai Yan"),
            List.of()
        );

        assertTrue(resposta.antiSinergiasNossaComposicao().stream().anyMatch(
            anti -> anti.tipo() == TipoAntiSinergia.MOBILIDADE_INCOMPATIVEL
        ));
    }

    @Test
    void devePenalizarCaiYanEManterYariaSemPenalidadeComArli() {
        List<RecomendacaoDnaResponse> recomendacoes = service.recomendar(
            List.of("Arli"),
            List.of(),
            Rota.ROAMING,
            50
        );

        RecomendacaoDnaResponse caiYan = recomendacoes.stream()
            .filter(recomendacao -> recomendacao.heroi().equals("Cai Yan"))
            .findFirst()
            .orElseThrow();
        RecomendacaoDnaResponse yaria = recomendacoes.stream()
            .filter(recomendacao -> recomendacao.heroi().equals("Yaria"))
            .findFirst()
            .orElseThrow();

        assertTrue(caiYan.penalidadeAntiSinergia() > 0);
        assertEquals(0, yaria.penalidadeAntiSinergia());
        assertFalse(caiYan.alertas().isEmpty());
    }

    @Test
    void deveDetectarDeslocamentoQuePodeQuebrarUltimateAliada() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Xiang Yu", "Marco Polo"),
            List.of()
        );

        assertTrue(resposta.antiSinergiasNossaComposicao().stream().anyMatch(
            anti -> anti.tipo()
                == TipoAntiSinergia.DESLOCAMENTO_QUE_QUEBRA_COMBO
        ));
    }
}
