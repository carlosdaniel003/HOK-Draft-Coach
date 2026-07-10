package br.com.carlosdaniel.hokdraftcoach.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.service.DraftDnaService;
import br.com.carlosdaniel.hokdraftcoach.service.InferenciaFuncoesService;
import br.com.carlosdaniel.hokdraftcoach.service.RecomendacaoProximoPickDnaService;
import br.com.carlosdaniel.hokdraftcoach.service.RecomendacaoProximoPickService;

class DraftControllerTest {

    @Test
    void deveSepararRespostaRapidaDaAnaliseDetalhada() {
        DraftDnaService draftDna = mock(DraftDnaService.class);
        InferenciaFuncoesService inferencia = mock(InferenciaFuncoesService.class);
        RecomendacaoProximoPickService rapido =
            mock(RecomendacaoProximoPickService.class);
        RecomendacaoProximoPickDnaService detalhado =
            mock(RecomendacaoProximoPickDnaService.class);
        DraftController controller = new DraftController(
            draftDna,
            inferencia,
            rapido,
            detalhado
        );
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                1,
                List.of(1L, 2L, 3L),
                List.of(4L, 5L, 6L),
                List.of(),
                List.of(),
                List.of(Rota.FARM_LANE)
            );
        RecomendacaoProximoPickResponse respostaRapida =
            mock(RecomendacaoProximoPickResponse.class);
        RecomendacaoProximoPickResponse respostaDetalhada =
            mock(RecomendacaoProximoPickResponse.class);

        when(rapido.recomendar(request)).thenReturn(respostaRapida);
        when(detalhado.recomendar(request)).thenReturn(respostaDetalhada);

        assertSame(
            respostaRapida,
            controller.recomendarProximoPickRapido(request)
        );
        assertSame(
            respostaDetalhada,
            controller.recomendarProximoPick(request)
        );
    }
}
