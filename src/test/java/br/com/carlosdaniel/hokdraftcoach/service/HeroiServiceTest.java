package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;

class HeroiServiceTest {

    private HeroiService heroiService;

    @BeforeEach
    void configurar() {
        heroiService = new HeroiService();
    }

    @Test
    void deveCadastrarKongmingComoPickFlexDeMidEJungle() {
        Heroi kongming = buscar("Kongming");

        assertEquals(Rota.MID_LANE, kongming.getRota());
        assertTrue(kongming.isFlex());
        assertTrue(kongming.podeJogarNaRota(Rota.MID_LANE));
        assertTrue(kongming.podeJogarNaRota(Rota.JUNGLE));
    }

    @Test
    void deveRetornarKongmingNasDuasConsultasDeRota() {
        assertTrue(
            heroiService.listarPorRota(Rota.MID_LANE)
                .stream()
                .anyMatch(heroi -> heroi.getNome().equals("Kongming"))
        );

        assertTrue(
            heroiService.listarPorRota(Rota.JUNGLE)
                .stream()
                .anyMatch(heroi -> heroi.getNome().equals("Kongming"))
        );
    }

    @Test
    void deveCadastrarOsVinteHeroisAtuaisDaFarmLane() {
        Set<String> esperados = Set.of(
            "Luara",
            "Agu",
            "Alessio",
            "Loong",
            "Arli",
            "Chano",
            "Chicha",
            "Consorte Yu",
            "Di Renjie",
            "Erin",
            "Fang",
            "Ser do Fluxo (Atirador)",
            "Garo",
            "Hou Yi",
            "Huang Zhong",
            "Lady Sun",
            "Luban No.7",
            "Marco Polo",
            "Meng Ya",
            "Shouyue"
        );

        Set<String> cadastrados = heroiService
            .listarPorRota(Rota.FARM_LANE)
            .stream()
            .map(Heroi::getNome)
            .collect(Collectors.toSet());

        assertEquals(20, cadastrados.size());
        assertEquals(esperados, cadastrados);
    }

    @Test
    void devePreservarAsRotasDosPicksFlexDaFarmLane() {
        Heroi agu = buscar("Agu");
        Heroi chano = buscar("Chano");
        Heroi chicha = buscar("Chicha");
        Heroi fang = buscar("Fang");

        assertTrue(agu.podeJogarNaRota(Rota.JUNGLE));
        assertTrue(agu.podeJogarNaRota(Rota.FARM_LANE));

        assertTrue(chano.podeJogarNaRota(Rota.FARM_LANE));
        assertTrue(chano.podeJogarNaRota(Rota.JUNGLE));

        assertTrue(chicha.podeJogarNaRota(Rota.CLASH_LANE));
        assertTrue(chicha.podeJogarNaRota(Rota.JUNGLE));
        assertTrue(chicha.podeJogarNaRota(Rota.FARM_LANE));

        assertTrue(fang.podeJogarNaRota(Rota.FARM_LANE));
        assertTrue(fang.podeJogarNaRota(Rota.JUNGLE));
    }

    @Test
    void deveResolverAliasesEntreClienteBrasileiroEFontesExternas() {
        assertEquals("Loong", buscar("Ao'yin").getNome());
        assertEquals("Agu", buscar("Agudo").getNome());
        assertEquals("Consorte Yu", buscar("Consort Yu").getNome());
        assertEquals(
            "Ser do Fluxo (Atirador)",
            buscar("Flowborn (Marksman)").getNome()
        );
        assertEquals("Luban No.7", buscar("Luban").getNome());
        assertEquals("Arli", buscar("Gongsun Li").getNome());
    }

    @Test
    void deveVersionarOTierAtualDaFarmLane() {
        Map<TierMeta, Long> quantidadePorTier = heroiService
            .listarPorRota(Rota.FARM_LANE)
            .stream()
            .collect(Collectors.groupingBy(
                heroi -> heroi.getDadosMeta().tier(),
                Collectors.counting()
            ));

        assertEquals(1L, quantidadePorTier.get(TierMeta.S));
        assertEquals(9L, quantidadePorTier.get(TierMeta.A));
        assertEquals(7L, quantidadePorTier.get(TierMeta.B));
        assertEquals(3L, quantidadePorTier.get(TierMeta.C));

        heroiService.listarPorRota(Rota.FARM_LANE).forEach(heroi -> {
            assertEquals(
                "S15-HOK-PLUS-2.0",
                heroi.getDadosMeta().versao()
            );
            assertEquals(
                LocalDate.of(2026, 7, 2),
                heroi.getDadosMeta().atualizadoEm()
            );
            assertEquals(
                "HOKSTATS_TIER_LIST",
                heroi.getDadosMeta().fonte()
            );
            assertFalse(heroi.getDadosMeta().oficial());
        });
    }

    @Test
    void deveManterClassesEDanosEspeciaisDaFarmLane() {
        assertEquals(ClasseHeroi.LUTADOR, buscar("Chicha").getClasse());
        assertEquals(ClasseHeroi.ATIRADOR, buscar("Agu").getClasse());
        assertEquals(TipoDano.MAGICO, buscar("Erin").getTipoDano());
        assertEquals(TipoDano.MISTO, buscar("Marco Polo").getTipoDano());
    }

    private Heroi buscar(String nome) {
        return heroiService.buscarPorNome(nome).orElseThrow();
    }
}
