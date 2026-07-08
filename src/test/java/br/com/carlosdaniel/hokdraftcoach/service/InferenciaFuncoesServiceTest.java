package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaEquipeResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

class InferenciaFuncoesServiceTest {

    private InferenciaFuncoesService service;

    @BeforeEach
    void configurar() {
        service = new InferenciaFuncoesService(new HeroiService());
    }

    @Test
    void deveManterKongmingAmbiguoQuandoForOPickUnico() {
        InferenciaFuncoesResponse resposta = service.inferir(
            request(List.of(20L), List.of())
        );

        InferenciaEquipeResponse azul = resposta.equipeAzul();

        assertTrue(azul.composicaoCompativel());
        assertEquals(2, azul.totalHipoteses());
        assertEquals("MEDIA", azul.confiancaMelhorHipotese());
        assertFalse(azul.ambiguidades().getFirst().funcaoConfirmada());
        assertTrue(
            azul.ambiguidades().getFirst().funcoesPossiveis()
                .containsAll(List.of(Rota.MID_LANE, Rota.JUNGLE))
        );
    }

    @Test
    void deveMoverKongmingParaJungleQuandoAngelaOcupaOMid() {
        InferenciaFuncoesResponse resposta = service.inferir(
            request(List.of(20L, 14L), List.of())
        );

        InferenciaEquipeResponse azul = resposta.equipeAzul();

        assertEquals(1, azul.totalHipoteses());
        assertEquals("ALTA", azul.confiancaMelhorHipotese());
        assertTrue(
            azul.ambiguidades().stream()
                .allMatch(item -> item.funcaoConfirmada())
        );
        assertTrue(
            azul.hipoteses().getFirst().atribuicoes().stream()
                .anyMatch(item ->
                    item.heroi().equals("Kongming")
                        && item.rota() == Rota.JUNGLE
                )
        );
    }

    @Test
    void deveIdentificarComposicaoSemDistribuicaoValida() {
        InferenciaFuncoesResponse resposta = service.inferir(
            request(List.of(14L, 15L), List.of())
        );

        InferenciaEquipeResponse azul = resposta.equipeAzul();

        assertFalse(azul.composicaoCompativel());
        assertEquals(0, azul.totalHipoteses());
        assertEquals("INCOMPATIVEL", azul.confiancaMelhorHipotese());
        assertTrue(azul.hipoteses().isEmpty());
    }

    @Test
    void devePreservarAOrdemRealDoSlotComLacuna() {
        InferenciaFuncoesRequest request = new InferenciaFuncoesRequest(
            List.of(new PickSemFuncaoRequest(3, 20L)),
            List.of()
        );

        InferenciaFuncoesResponse resposta = service.inferir(request);

        assertEquals(
            "B3",
            resposta.equipeAzul()
                .hipoteses()
                .getFirst()
                .atribuicoes()
                .getFirst()
                .slot()
        );
    }

    @Test
    void deveRejeitarMesmoHeroiNosDoisLados() {
        InferenciaFuncoesRequest request = request(
            List.of(20L),
            List.of(20L)
        );

        assertThrows(
            RegraNegocioException.class,
            () -> service.inferir(request)
        );
    }

    private InferenciaFuncoesRequest request(
        List<Long> azul,
        List<Long> vermelho
    ) {
        return new InferenciaFuncoesRequest(
            criarPicks(azul),
            criarPicks(vermelho)
        );
    }

    private List<PickSemFuncaoRequest> criarPicks(List<Long> ids) {
        List<PickSemFuncaoRequest> picks = new ArrayList<>();

        for (int indice = 0; indice < ids.size(); indice++) {
            picks.add(
                new PickSemFuncaoRequest(indice + 1, ids.get(indice))
            );
        }

        return picks;
    }
}
