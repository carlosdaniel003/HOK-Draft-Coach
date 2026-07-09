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

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoMidResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoMidLane;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaMidEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.ConfrontoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.PerfilMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaMidRepository;

class ConhecimentoMidServiceTest {

    private HeroiService heroiService;
    private ConhecimentoMidService conhecimentoMidService;

    @BeforeEach
    void configurar() {
        CatalogoSuporteRepository catalogoSuporte =
            new CatalogoSuporteRepository();
        CatalogoMidRepository catalogoMid = new CatalogoMidRepository();

        heroiService = new HeroiServiceCompleto(
            catalogoSuporte,
            catalogoMid
        );
        conhecimentoMidService = new ConhecimentoMidService(
            heroiService,
            catalogoMid,
            new PerfilMidRepository(),
            new ConfrontoMidRepository(),
            new SinergiaMidRepository()
        );
    }

    @Test
    void deveCadastrarOsVinteEOitoMidLanersDaS15() {
        Set<String> esperados = Set.of(
            "Ser do Fluxo (Mago)", "Garuda", "Lorion", "Angela",
            "Da Qiao", "Daji", "Diaochan", "Dr Bian Qe", "Gan & Mo",
            "Gao Jianli", "Haya", "Heino", "Kongming", "Lady Zhen",
            "Liang", "Mai Shiranui", "Milady", "Mozi", "Nuwa",
            "Shangguan", "Shi", "Sima Yi", "Wang Zhaojun", "Xiao Qiao",
            "Yixing", "Zhou Yu", "Ziya", "Yuhuan"
        );

        Set<String> cadastrados = heroiService.listarPorRota(Rota.MID_LANE)
            .stream()
            .map(Heroi::getNome)
            .collect(Collectors.toSet());

        assertEquals(28, cadastrados.size());
        assertEquals(esperados, cadastrados);
    }

    @Test
    void deveResolverAliasesERotasFlexiveisDoMid() {
        assertEquals("Ser do Fluxo (Mago)", buscar("Flowborn Mage").getNome());
        assertEquals("Dr Bian Qe", buscar("Bian Que").getNome());
        assertEquals("Kongming", buscar("Zhuge Liang").getNome());
        assertEquals("Wang Zhaojun", buscar("Princesa Gélida").getNome());
        assertEquals("Zhou Yu", buscar("Zhou You").getNome());

        assertTrue(buscar("Da Qiao").podeJogarNaRota(Rota.ROAMING));
        assertTrue(buscar("Heino").podeJogarNaRota(Rota.CLASH_LANE));
        assertTrue(buscar("Kongming").podeJogarNaRota(Rota.JUNGLE));
        assertTrue(buscar("Mozi").podeJogarNaRota(Rota.ROAMING));
        assertTrue(buscar("Sima Yi").podeJogarNaRota(Rota.JUNGLE));
        assertTrue(buscar("Ziya").podeJogarNaRota(Rota.ROAMING));
    }

    @Test
    void deveVersionarOTierAtualDoMid() {
        Map<TierMeta, Long> quantidadePorTier = heroiService
            .listarPorRota(Rota.MID_LANE)
            .stream()
            .collect(Collectors.groupingBy(
                heroi -> heroi.getDadosMeta().tier(),
                Collectors.counting()
            ));

        assertEquals(5L, quantidadePorTier.get(TierMeta.S));
        assertEquals(5L, quantidadePorTier.get(TierMeta.A));
        assertEquals(10L, quantidadePorTier.get(TierMeta.B));
        assertEquals(8L, quantidadePorTier.get(TierMeta.C));

        heroiService.listarPorRota(Rota.MID_LANE).forEach(heroi -> {
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
    void deveCadastrarConfrontosDirecionaisImportantes() {
        ConfrontoMidLane angela = conhecimentoMidService.analisarConfronto(
            "Angela",
            "Diaochan"
        );
        ConfrontoMidLane milady = conhecimentoMidService.analisarConfronto(
            "Milady",
            "Mai Shiranui"
        );
        ConfrontoMidLane ladyZhen = conhecimentoMidService.analisarConfronto(
            "Lady Zhen",
            "Sima Yi"
        );

        assertEquals(9, angela.vantagem());
        assertEquals(8, milady.vantagem());
        assertEquals(8, ladyZhen.vantagem());
    }

    @Test
    void deveRecomendarLiangContraAssassinosMoveis() {
        List<ConfrontoMidLane> counters = conhecimentoMidService
            .melhoresRespostasParaMid("Kongming", 10);

        assertTrue(counters.stream().anyMatch(confronto ->
            confronto.vencedor().equals("Liang")
                && confronto.vantagem() == 9
        ));
    }

    @Test
    void deveCadastrarCombosDeControleEArtillery() {
        SinergiaMidEquipe yixing = conhecimentoMidService.analisarCombo(
            "Yixing",
            "Lian Po"
        );
        SinergiaMidEquipe ganMo = conhecimentoMidService.analisarCombo(
            "Gan & Mo",
            "Kui"
        );
        SinergiaMidEquipe xiaoQiao = conhecimentoMidService.analisarCombo(
            "Xiao Qiao",
            "Donghuang"
        );
        SinergiaMidEquipe lorion = conhecimentoMidService.analisarCombo(
            "Lorion",
            "Lian Po"
        );

        assertEquals(10, yixing.nota());
        assertEquals(10, ganMo.nota());
        assertEquals(9, xiaoQiao.nota());
        assertEquals(10, lorion.nota());
    }

    @Test
    void deveRecomendarControladoresContraDiveEAltaMobilidade() {
        List<RecomendacaoComposicaoMidResponse> recomendacoes =
            conhecimentoMidService.recomendarContraComposicao(
                List.of(
                    TipoComposicao.DIVE,
                    TipoComposicao.ALTA_MOBILIDADE
                ),
                10
            );

        RecomendacaoComposicaoMidResponse liang = recomendacoes.stream()
            .filter(resultado -> resultado.mid().equals("Liang"))
            .findFirst()
            .orElseThrow();

        assertTrue(liang.pontuacao() > 50);
        assertTrue(liang.respondeA().contains(TipoComposicao.DIVE));
        assertTrue(
            liang.respondeA().contains(TipoComposicao.ALTA_MOBILIDADE)
        );
    }

    @Test
    void deveManterUmPerfilParaCadaMidSemDuplicarIds() {
        assertEquals(28, conhecimentoMidService.listarMids().size());
        conhecimentoMidService.listarMids().forEach(resultado -> {
            assertEquals(resultado.heroi().getNome(), resultado.perfil().heroi());
            assertFalse(resultado.perfil().arquetipos().isEmpty());
            assertFalse(resultado.perfil().observacoes().isEmpty());
        });

        long idsUnicos = heroiService.listarTodos()
            .stream()
            .map(Heroi::getId)
            .distinct()
            .count();

        assertEquals(heroiService.listarTodos().size(), idsUnicos);
    }

    @Test
    void devePontuarConfrontoESinergiaParaOMotor() {
        Heroi liang = buscar("Liang");
        Heroi kongming = buscar("Kongming");
        Heroi yixing = buscar("Yixing");
        Heroi lianPo = buscar("Lian Po");

        assertTrue(
            conhecimentoMidService.pontuarConfronto(liang, kongming) > 0
        );
        assertTrue(
            conhecimentoMidService.pontuarConfronto(kongming, liang) < 0
        );
        assertTrue(
            conhecimentoMidService.pontuarSinergia(
                yixing,
                List.of(lianPo)
            ) > 0
        );
    }

    private Heroi buscar(String nome) {
        return heroiService.buscarPorNome(nome).orElseThrow();
    }
}
