package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteBlindPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AlvoPrioritarioAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseAmeacasResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DadosMetaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.MomentoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;
import br.com.carlosdaniel.hokdraftcoach.repository.AntiSinergiaRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaGrupoRepository;

class AnaliseAmeacaBlindPickServiceTest {

    private AnaliseAmeacaComposicaoService motor;
    private SegurancaBlindPickService blindPickService;

    @BeforeEach
    void configurar() {
        HeroiService heroiService = new HeroiServiceCompleto(
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
        motor = new AnaliseAmeacaComposicaoService(
            heroiService,
            dnaHeroi,
            new CondicaoVitoriaService(dnaHeroi, economia),
            economia,
            new NecessidadePenalidadeService(dnaHeroi, economia),
            temporal,
            sinergias,
            antiSinergias,
            new AnaliseAmeacaService(dnaHeroi, temporal, economia)
        );
        blindPickService = new SegurancaBlindPickService();
    }

    @Test
    void deveSepararAmeacaIniciadorHabilitadorEEloFraco() {
        DiagnosticoComposicaoResponse resposta = motor.diagnosticar(
            List.of("Garo"),
            List.of("Marco Polo", "Lian Po", "Dolia")
        );
        AnaliseAmeacasResponse ameacas = resposta.analiseAmeacasInimigas();

        assertEquals("Marco Polo", ameacas.maiorAmeaca().heroi());
        assertEquals("Lian Po", ameacas.iniciadorPrincipal().heroi());
        assertEquals("Dolia", ameacas.habilitadorCritico().heroi());
        assertNotNull(ameacas.protetorPrincipal());
        assertNotNull(ameacas.eloFraco());
        assertFalse(ameacas.alvosPrioritarios().isEmpty());
    }

    @Test
    void devePriorizarQuebrarAJanelaDeLianPoAntesDoDanoDeMarcoPolo() {
        AnaliseAmeacasResponse ameacas = motor.analisarAmeacas(
            List.of("Marco Polo", "Lian Po", "Dolia")
        );

        AlvoPrioritarioAmeacaResponse lianPo = ameacas.alvosPrioritarios()
            .stream()
            .filter(alvo -> alvo.heroi().equals("Lian Po"))
            .findFirst()
            .orElseThrow();
        AlvoPrioritarioAmeacaResponse marcoPolo = ameacas.alvosPrioritarios()
            .stream()
            .filter(alvo -> alvo.heroi().equals("Marco Polo"))
            .findFirst()
            .orElseThrow();

        assertTrue(lianPo.prioridade() >= marcoPolo.prioridade());
        assertTrue(ameacas.planoResposta().contains("Lian Po"));
    }

    @Test
    void recomendacaoDeveExplicarQualDependenciaInimigaResponde() {
        List<RecomendacaoDnaResponse> recomendacoes = motor.recomendar(
            List.of("Garo"),
            List.of("Marco Polo", "Lian Po", "Dolia"),
            Rota.ROAMING,
            50
        );

        assertTrue(recomendacoes.stream().anyMatch(recomendacao ->
            recomendacao.bonusRespostaAmeaca() > 0
                && !recomendacao.alvosAmeacaRespondidos().isEmpty()
        ));
    }

    @Test
    void blindPickDeveFavorecerFlexibilidadeEConsistencia() {
        Heroi seguro = heroiSeguroFlexivel();
        Heroi counter = heroiCounterEspecifico();
        RecomendacaoProximoPickRequest inicial = request(1);

        AjusteBlindPickResponse ajusteSeguro = blindPickService.avaliar(
            inicial,
            seguro,
            respostaBaseSegura(),
            dnaSeguro()
        );
        AjusteBlindPickResponse ajusteCounter = blindPickService.avaliar(
            inicial,
            counter,
            respostaBaseCounter(),
            dnaCounter()
        );

        assertEquals(
            MomentoDraft.BLIND_PICK_INICIAL,
            ajusteSeguro.contexto().momento()
        );
        assertTrue(
            ajusteSeguro.perfil().segurancaBlind()
                > ajusteCounter.perfil().segurancaBlind()
        );
        assertTrue(ajusteSeguro.ajuste() > ajusteCounter.ajuste());
    }

    @Test
    void ultimoPickDeveFavorecerCounterEspecificoMesmoSendoMenosSeguro() {
        Heroi seguro = heroiSeguroFlexivel();
        Heroi counter = heroiCounterEspecifico();
        RecomendacaoProximoPickRequest ultimo = request(5);

        AjusteBlindPickResponse ajusteSeguro = blindPickService.avaliar(
            ultimo,
            seguro,
            respostaBaseSegura(),
            dnaSeguro()
        );
        AjusteBlindPickResponse ajusteCounter = blindPickService.avaliar(
            ultimo,
            counter,
            respostaBaseCounter(),
            dnaCounter()
        );

        assertEquals(MomentoDraft.LAST_PICK, ajusteCounter.contexto().momento());
        assertTrue(
            ajusteCounter.perfil().especificidade()
                > ajusteSeguro.perfil().especificidade()
        );
        assertTrue(ajusteCounter.ajuste() > ajusteSeguro.ajuste());
    }

    private Heroi heroiSeguroFlexivel() {
        return new Heroi(
            9001L,
            "Escolha Segura",
            List.of(),
            ClasseHeroi.HIBRIDO,
            Rota.MID_LANE,
            List.of(Rota.MID_LANE, Rota.CLASH_LANE, Rota.JUNGLE),
            "Flexível e consistente",
            2,
            TipoDano.MISTO,
            new AtributosHeroi(7, 7, 7, 6, 6, 6),
            List.of("flexibilidade", "utilidade"),
            new DadosMetaHeroi(
                TierMeta.A,
                "TESTE",
                LocalDate.of(2026, 7, 9),
                "TESTE",
                false
            )
        );
    }

    private Heroi heroiCounterEspecifico() {
        return new Heroi(
            9002L,
            "Counter Específico",
            List.of(),
            ClasseHeroi.ASSASSINO,
            Rota.JUNGLE,
            List.of(Rota.JUNGLE),
            "Counter de alta execução",
            5,
            TipoDano.FISICO,
            new AtributosHeroi(6, 3, 8, 3, 5, 10),
            List.of("pickoff", "explosão", "counter específico"),
            new DadosMetaHeroi(
                TierMeta.B,
                "TESTE",
                LocalDate.of(2026, 7, 9),
                "TESTE",
                false
            )
        );
    }

    private RecomendacaoPickResponse respostaBaseSegura() {
        return new RecomendacaoPickResponse(
            9001L,
            "Escolha Segura",
            List.of(Rota.MID_LANE, Rota.CLASH_LANE, Rota.JUNGLE),
            72,
            74,
            69,
            100,
            8,
            "ALTA",
            2,
            Map.of(
                "confronto", 1,
                "respostaAosInimigos", 2,
                "flexibilidade", 5
            ),
            List.of(),
            List.of(),
            false
        );
    }

    private RecomendacaoPickResponse respostaBaseCounter() {
        return new RecomendacaoPickResponse(
            9002L,
            "Counter Específico",
            List.of(Rota.JUNGLE),
            70,
            66,
            43,
            70,
            6,
            "SITUACIONAL",
            5,
            Map.of(
                "confronto", 15,
                "respostaAosInimigos", 12,
                "flexibilidade", 0
            ),
            List.of(),
            List.of(),
            false
        );
    }

    private RecomendacaoDnaResponse dnaSeguro() {
        return new RecomendacaoDnaResponse(
            "Escolha Segura",
            Rota.MID_LANE,
            62,
            List.of(),
            List.of(),
            40,
            4,
            0,
            3,
            0,
            0,
            0,
            List.of(),
            List.of(),
            List.of()
        );
    }

    private RecomendacaoDnaResponse dnaCounter() {
        return new RecomendacaoDnaResponse(
            "Counter Específico",
            Rota.JUNGLE,
            82,
            List.of(),
            List.of(),
            72,
            -4,
            0,
            0,
            10,
            0,
            16,
            List.of("Lian Po", "Marco Polo"),
            List.of(),
            List.of()
        );
    }

    private RecomendacaoProximoPickRequest request(int ordem) {
        return new RecomendacaoProximoPickRequest(
            LadoDraft.AZUL,
            ordem,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(Rota.JUNGLE, Rota.MID_LANE)
        );
    }
}
