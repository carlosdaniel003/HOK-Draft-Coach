package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoClashResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoEncaixeClashResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoClashLane;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaClashEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.ConfrontoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.PerfilClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaClashRepository;

class ConhecimentoClashServiceTest {

    private HeroiService heroiService;
    private ConhecimentoClashService conhecimentoClashService;

    @BeforeEach
    void configurar() {
        CatalogoSuporteRepository suporte = new CatalogoSuporteRepository();
        CatalogoMidRepository mid = new CatalogoMidRepository();
        CatalogoJungleRepository jungle = new CatalogoJungleRepository();
        CatalogoClashRepository clash = new CatalogoClashRepository();

        heroiService = new HeroiServiceCompleto(
            suporte,
            mid,
            jungle,
            clash
        );
        conhecimentoClashService = new ConhecimentoClashService(
            heroiService,
            clash,
            new PerfilClashRepository(),
            new ConfrontoClashRepository(),
            new SinergiaClashRepository()
        );
    }

    @Test
    void deveCadastrarOsTrintaENoveTopsDaS15() {
        Set<String> esperados = Set.of(
            "Ata", "Devara", "Fatih", "Florentino",
            "Ser do Fluxo (Tanque)", "Lapulapu", "Allain", "Arthur",
            "Augran", "Bai Qi", "Biron", "Charlotte", "Chicha",
            "Diaochan", "Donghuang", "Dun", "Fuzi", "Guan Yu",
            "Heino", "Kaizer", "Li Xin", "Lian Po", "Liu Bang",
            "Lu Bu", "Mayene", "Menki", "Mi Yue", "Mulan", "Musashi",
            "Nezha", "Sun Ce", "Ukyo Tachibana", "Umbrosa", "Wuyan",
            "Xiang Yu", "Yang Jian", "Yao", "Zhuangzi", "Dharma"
        );

        Set<String> cadastrados = heroiService.listarPorRota(Rota.CLASH_LANE)
            .stream()
            .map(Heroi::getNome)
            .collect(Collectors.toSet());

        assertEquals(39, cadastrados.size());
        assertEquals(esperados, cadastrados);
    }

    @Test
    void deveResolverAliasesEManterFlexPicks() {
        assertEquals(
            "Ser do Fluxo (Tanque)",
            buscar("Flowborn Tank").getNome()
        );
        assertEquals("Lapulapu", buscar("Lapu-Lapu").getNome());
        assertEquals("Fuzi", buscar("Old Master").getNome());
        assertEquals("Dun", buscar("Xiahou Dun").getNome());
        assertEquals("Musashi", buscar("Miyamoto Musashi").getNome());

        assertTrue(buscar("Ata").podeJogarNaRota(Rota.JUNGLE));
        assertTrue(buscar("Lapulapu").podeJogarNaRota(Rota.ROAMING));
        assertTrue(buscar("Diaochan").podeJogarNaRota(Rota.MID_LANE));
        assertTrue(buscar("Donghuang").podeJogarNaRota(Rota.ROAMING));
        assertTrue(buscar("Heino").podeJogarNaRota(Rota.MID_LANE));
        assertFalse(buscar("Chicha").podeJogarNaRota(Rota.FARM_LANE));
        assertTrue(buscar("Sun Ce").podeJogarNaRota(Rota.JUNGLE));
    }

    @Test
    void deveVersionarOTierS15DaClashLane() {
        Map<TierMeta, Long> quantidadePorTier = heroiService
            .listarPorRota(Rota.CLASH_LANE)
            .stream()
            .collect(Collectors.groupingBy(
                heroi -> heroi.getDadosMeta().tier(),
                Collectors.counting()
            ));

        assertEquals(4L, quantidadePorTier.get(TierMeta.S));
        assertEquals(5L, quantidadePorTier.get(TierMeta.A));
        assertEquals(11L, quantidadePorTier.get(TierMeta.B));
        assertEquals(19L, quantidadePorTier.get(TierMeta.C));

        heroiService.listarPorRota(Rota.CLASH_LANE).forEach(heroi -> {
            assertEquals("S15-HOK-PLUS-2.0", heroi.getDadosMeta().versao());
            assertEquals(
                LocalDate.of(2026, 7, 2),
                heroi.getDadosMeta().atualizadoEm()
            );
            assertEquals("HOKSTATS_TIER_LIST", heroi.getDadosMeta().fonte());
            assertFalse(heroi.getDadosMeta().oficial());
        });
    }

    @Test
    void deveCadastrarMatchupsDeX1EMecanica() {
        ConfrontoClashLane florentino =
            conhecimentoClashService.analisarConfronto(
                "Florentino",
                "Arthur"
            );
        ConfrontoClashLane donghuang =
            conhecimentoClashService.analisarConfronto(
                "Donghuang",
                "Florentino"
            );
        ConfrontoClashLane musashi =
            conhecimentoClashService.analisarConfronto(
                "Musashi",
                "Fatih"
            );
        ConfrontoClashLane zhuangzi =
            conhecimentoClashService.analisarConfronto(
                "Zhuangzi",
                "Bai Qi"
            );

        assertEquals(9, florentino.vantagem());
        assertEquals(9, donghuang.vantagem());
        assertEquals(9, musashi.vantagem());
        assertEquals(9, zhuangzi.vantagem());
    }

    @Test
    void deveCadastrarCombosDeTeamFightEMacro() {
        SinergiaClashEquipe florentino =
            conhecimentoClashService.analisarCombo(
                "Florentino",
                "Yaria"
            );
        SinergiaClashEquipe baiQi = conhecimentoClashService.analisarCombo(
            "Bai Qi",
            "Wang Zhaojun"
        );
        SinergiaClashEquipe liXin = conhecimentoClashService.analisarCombo(
            "Li Xin",
            "Da Qiao"
        );
        SinergiaClashEquipe sunCe = conhecimentoClashService.analisarCombo(
            "Sun Ce",
            "Gao Jianli"
        );

        assertEquals(10, florentino.nota());
        assertEquals(10, baiQi.nota());
        assertEquals(10, liXin.nota());
        assertEquals(10, sunCe.nota());
    }

    @Test
    void deveRecomendarAntiTanqueContraFrontlinePesada() {
        List<RecomendacaoComposicaoClashResponse> recomendacoes =
            conhecimentoClashService.recomendarContraComposicao(
                List.of(
                    TipoComposicao.FRONT_TO_BACK,
                    TipoComposicao.ESCUDOS_E_CURA
                ),
                10
            );

        RecomendacaoComposicaoClashResponse augran = recomendacoes.stream()
            .filter(resultado -> resultado.top().equals("Augran"))
            .findFirst()
            .orElseThrow();

        assertTrue(augran.pontuacao() > 60);
        assertTrue(
            augran.respondeA().contains(TipoComposicao.FRONT_TO_BACK)
        );
        assertTrue(
            augran.respondeA().contains(TipoComposicao.ESCUDOS_E_CURA)
        );
    }

    @Test
    void deveRecomendarTopQueCompleteAComposicaoAliada() {
        List<RecomendacaoEncaixeClashResponse> recomendacoes =
            conhecimentoClashService.recomendarParaComposicaoAliada(
                List.of(
                    TipoComposicao.ENGAGE_AGRUPADO,
                    TipoComposicao.FRONT_TO_BACK
                ),
                10
            );

        RecomendacaoEncaixeClashResponse lianPo = recomendacoes.stream()
            .filter(resultado -> resultado.top().equals("Lian Po"))
            .findFirst()
            .orElseThrow();

        assertTrue(lianPo.pontuacao() > 60);
        assertTrue(
            lianPo.fortalece().contains(TipoComposicao.ENGAGE_AGRUPADO)
        );
        assertTrue(
            lianPo.fortalece().contains(TipoComposicao.FRONT_TO_BACK)
        );
    }

    @Test
    void deveManterUmPerfilEstrategicoParaCadaTop() {
        assertEquals(39, conhecimentoClashService.listarTops().size());
        conhecimentoClashService.listarTops().forEach(resultado -> {
            assertEquals(resultado.heroi().getNome(), resultado.perfil().heroi());
            assertFalse(resultado.perfil().arquetipos().isEmpty());
            assertFalse(resultado.perfil().observacoes().isEmpty());
        });
    }

    @Test
    void deveManterNomesEIdsUnicosNoCatalogoGlobal() {
        long nomesUnicos = heroiService.listarTodos()
            .stream()
            .map(Heroi::getNome)
            .distinct()
            .count();
        long idsUnicos = heroiService.listarTodos()
            .stream()
            .map(Heroi::getId)
            .distinct()
            .count();

        assertEquals(heroiService.listarTodos().size(), nomesUnicos);
        assertEquals(heroiService.listarTodos().size(), idsUnicos);
    }

    @Test
    void devePontuarConhecimentoParaUsoNoMotor() {
        Heroi florentino = buscar("Florentino");
        Heroi arthur = buscar("Arthur");
        Heroi liXin = buscar("Li Xin");
        Heroi daQiao = buscar("Da Qiao");
        Heroi lianPo = buscar("Lian Po");
        Heroi marco = buscar("Marco Polo");

        assertTrue(
            conhecimentoClashService.pontuarConfronto(
                florentino,
                arthur
            ) > 0
        );
        assertTrue(
            conhecimentoClashService.pontuarConfronto(
                arthur,
                florentino
            ) < 0
        );
        assertTrue(
            conhecimentoClashService.pontuarSinergia(
                liXin,
                List.of(daQiao)
            ) > 0
        );
        assertTrue(
            conhecimentoClashService.pontuarEncaixeAliado(
                lianPo,
                List.of(marco)
            ) > 0
        );
    }

    private Heroi buscar(String nome) {
        return heroiService.buscarPorNome(nome).orElseThrow();
    }
}
