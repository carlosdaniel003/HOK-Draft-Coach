package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

class HeroiServiceTest {

    private HeroiService heroiService;

    @BeforeEach
    void configurar() {
        heroiService = new HeroiService();
    }

    @Test
    void deveCadastrarKongmingComoPickFlexDeMidEJungle() {
        Heroi kongming = heroiService.listarTodos()
            .stream()
            .filter(heroi -> heroi.getNome().equals("Kongming"))
            .findFirst()
            .orElseThrow();

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
}
