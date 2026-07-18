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

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoJungleResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoEncaixeJungleResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoJungle;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaJungleEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.ConfrontoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.PerfilJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaJungleRepository;

class ConhecimentoJungleServiceTest {

    private HeroiService heroiService;
    private ConhecimentoJungleService conhecimentoJungleService;

    @BeforeEach
    void configurar() {
        CatalogoSuporteRepository catalogoSuporte =
            new CatalogoSuporteRepository();
        CatalogoMidRepository catalogoMid = new CatalogoMidRepository();
        CatalogoJungleRepository catalogoJungle =
            new CatalogoJungleRepository();

        heroiService = new HeroiServiceCompleto(
            catalogoSuporte,
            catalogoMid,
            catalogoJungle
        );
        conhecimentoJungleService = new ConhecimentoJungleService(
            heroiService,
            catalogoJungle,
            new PerfilJungleRepository(),
            new ConfrontoJungleRepository(),
            new SinergiaJungleRepository()
        );
    }

    @Test
    void deveCadastrarOsQuarentaETresJunglersDaS15() {
        Set<String> esperados = Set.of(
            "Ata", "Butterfly", "Fatih", "Agu", "Arke", "Arthur",
            "Athena", "Augran", "Chano", "Charlotte", "Chicha",
            "Cirrus", "Dharma", "Dian Wei", "Fang", "Feyd",
            "Gao Changgong", "Han Xin", "Jing", "Kaizer", "Kongming",
            "Lam", "Li Bai", "Liu Bei", "Luna", "Mayene", "Menki",
            "Mi Yue", "Musashi", "Nakoruru", "Pei", "Sima Yi",
            "Sun Ce", "Ukyo Tachibana", "Umbrosa", "Wukong", "Wuyan",
            "Yang Jian", "Yao", "Ying", "Zilong", "Xuance", "Yango"
        );

        Set<String> cadastrados = heroiService.listarPorRota(Rota.JUNGLE)
            .stream()
            .map(Heroi::getNome)
            .collect(Collectors.toSet());

        assertEquals(43, cadastrados.size());
        assertEquals(esperados, cadastrados);
    }

    @Test
    void deveNormalizarNomesInformadosEAliasesGlobais() {
        assertEquals("Agu", buscar("Aguu").getNome());
        assertEquals("Agu", buscar("Agudo").getNome());
        assertEquals("Charlotte", buscar("Charlottr").getNome());
        assertEquals("Yango", buscar("Yuan Ge").getNome());
        assertEquals("Xuance", buscar("Baili Xuance").getNome());
        assertEquals("Yao", buscar("Yao").getNome());
        assertEquals("Gao Changgong", buscar("Prince of Lanling").getNome());
    }

    @Test
    void devePreservarRotasFlexiveisSemDuplicarHerois() {
        assertTrue(buscar("Ata").podeJogarNaRota(Rota.CLASH_LANE));
        assertTrue(buscar("Agu").podeJogarNaRota(Rota.FARM_LANE));
        assertTrue(buscar("Chano").podeJogarNaRota(Rota.FARM_LANE));
        assertTrue(buscar("Chicha").podeJogarNaRota(Rota.CLASH_LANE));
        assertFalse(buscar("Chicha").podeJogarNaRota(Rota.FARM_LANE));
        assertTrue(buscar("Kongming").podeJogarNaRota(Rota.MID_LANE));
        assertTrue(buscar("Sima Yi").podeJogarNaRota(Rota.MID_LANE));
        assertTrue(buscar("Sun Ce").podeJogarNaRota(Rota.CLASH_LANE));

        long idsUnicos = heroiService.listarTodos()
            .stream()
            .map(Heroi::getId)
            .distinct()
            .count();

        assertEquals(heroiService.listarTodos().size(), idsUnicos);
    }

    @Test
    void deveVersionarOTierS15DoJungle() {
        Map<TierMeta, Long> quantidadePorTier = heroiService
            .listarPorRota(Rota.JUNGLE)
            .stream()
            .collect(Collectors.groupingBy(
                heroi -> heroi.getDadosMeta().tier(),
                Collectors.counting()
            ));

        assertEquals(1L, quantidadePorTier.get(TierMeta.S));
        assertEquals(4L, quantidadePorTier.get(TierMeta.A));
        assertEquals(14L, quantidadePorTier.get(TierMeta.B));
        assertEquals(24L, quantidadePorTier.get(TierMeta.C));

        heroiService.listarPorRota(Rota.JUNGLE).forEach(heroi -> {
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
    void deveCadastrarConfrontosDeInvasaoDueloEMecanica() {
        ConfrontoJungle ata = conhecimentoJungleService.analisarConfronto(
            "Ata",
            "Agu"
        );
        ConfrontoJungle musashi = conhecimentoJungleService.analisarConfronto(
            "Musashi",
            "Umbrosa"
        );
        ConfrontoJungle arthur = conhecimentoJungleService.analisarConfronto(
            "Arthur",
            "Luna"
        );
        ConfrontoJungle pei = conhecimentoJungleService.analisarConfronto(
            "Pei",
            "Chano"
        );

        assertEquals(9, ata.vantagem());
        assertEquals(9, musashi.vantagem());
        assertEquals(9, arthur.vantagem());
        assertEquals(9, pei.vantagem());
    }

    @Test
    void deveCadastrarCombosDecisivosDeJungle() {
        SinergiaJungleEquipe augran = conhecimentoJungleService.analisarCombo(
            "Augran",
            "Dolia"
        );
        SinergiaJungleEquipe sunCe = conhecimentoJungleService.analisarCombo(
            "Sun Ce",
            "Gao Jianli"
        );
        SinergiaJungleEquipe luna = conhecimentoJungleService.analisarCombo(
            "Luna",
            "Yaria"
        );
        SinergiaJungleEquipe dharma = conhecimentoJungleService.analisarCombo(
            "Dharma",
            "Wang Zhaojun"
        );

        assertEquals(10, augran.nota());
        assertEquals(10, sunCe.nota());
        assertEquals(10, luna.nota());
        assertEquals(10, dharma.nota());
    }

    @Test
    void deveRecomendarRespostasContraLinhaDeFrenteESustentacao() {
        List<RecomendacaoComposicaoJungleResponse> recomendacoes =
            conhecimentoJungleService.recomendarContraComposicao(
                List.of(
                    TipoComposicao.FRONT_TO_BACK,
                    TipoComposicao.ESCUDOS_E_CURA
                ),
                10
            );

        RecomendacaoComposicaoJungleResponse augran = recomendacoes.stream()
            .filter(resultado -> resultado.jungler().equals("Augran"))
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
    void deveRecomendarJunglersQueFortalecemOPlanoAliado() {
        List<RecomendacaoEncaixeJungleResponse> recomendacoes =
            conhecimentoJungleService.recomendarParaComposicaoAliada(
                List.of(
                    TipoComposicao.FRONT_TO_BACK,
                    TipoComposicao.LUTAS_LONGAS
                ),
                10
            );

        RecomendacaoEncaixeJungleResponse augran = recomendacoes.stream()
            .filter(resultado -> resultado.jungler().equals("Augran"))
            .findFirst()
            .orElseThrow();

        assertTrue(augran.pontuacao() > 60);
        assertTrue(
            augran.fortalece().contains(TipoComposicao.FRONT_TO_BACK)
        );
        assertTrue(
            augran.fortalece().contains(TipoComposicao.LUTAS_LONGAS)
        );
    }

    @Test
    void deveManterUmPerfilEstrategicoParaCadaJungler() {
        assertEquals(43, conhecimentoJungleService.listarJunglers().size());
        conhecimentoJungleService.listarJunglers().forEach(resultado -> {
            assertEquals(resultado.heroi().getNome(), resultado.perfil().heroi());
            assertFalse(resultado.perfil().arquetipos().isEmpty());
            assertFalse(resultado.perfil().observacoes().isEmpty());
        });
    }

    @Test
    void devePontuarConhecimentoParaUsoNoMotor() {
        Heroi ata = buscar("Ata");
        Heroi agudo = buscar("Agu");
        Heroi augran = buscar("Augran");
        Heroi dolia = buscar("Dolia");
        Heroi zhangFei = buscar("Zhang Fei");
        Heroi marco = buscar("Marco Polo");

        assertTrue(
            conhecimentoJungleService.pontuarConfronto(ata, agudo) > 0
        );
        assertTrue(
            conhecimentoJungleService.pontuarConfronto(agudo, ata) < 0
        );
        assertTrue(
            conhecimentoJungleService.pontuarSinergia(
                augran,
                List.of(dolia)
            ) > 0
        );
        assertTrue(
            conhecimentoJungleService.pontuarEncaixeAliado(
                augran,
                List.of(zhangFei, marco)
            ) >= 0
        );
    }

    private Heroi buscar(String nome) {
        return heroiService.buscarPorNome(nome).orElseThrow();
    }
}
