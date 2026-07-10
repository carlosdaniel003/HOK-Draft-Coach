package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

class RecomendacaoProximoPickPorFuncaoServiceTest {

    private HeroiServicePorFuncao heroiService;
    private RecomendacaoProximoPickPorFuncaoService service;

    @BeforeEach
    void configurar() {
        heroiService = new HeroiServicePorFuncao(new HeroiService());
        InferenciaFuncoesService inferencia =
            new InferenciaFuncoesService(heroiService);
        service = new RecomendacaoProximoPickPorFuncaoService(
            heroiService,
            inferencia
        );
    }

    @Test
    void deveRecomendarSomenteHeroisDaFuncaoSelecionadaParaMeuSlot() {
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

        RecomendacaoProximoPickResponse resposta = service.recomendar(request);

        assertEquals("MINHA_VEZ", resposta.estadoDraft());
        assertNotNull(resposta.recomendacaoPrincipal());
        assertNotEquals("Chicha", resposta.recomendacaoPrincipal().heroi());
        assertTrue(resposta.alternativas().stream().noneMatch(
            recomendacao -> recomendacao.heroi().equals("Chicha")
        ));

        Stream<RecomendacaoPickResponse> recomendacoes = Stream.concat(
            Stream.of(resposta.recomendacaoPrincipal()),
            resposta.alternativas().stream()
        );

        assertTrue(recomendacoes.allMatch(recomendacao ->
            !recomendacao.rotasRecomendadas().isEmpty()
                && recomendacao.rotasRecomendadas().stream()
                    .allMatch(Rota.FARM_LANE::equals)
        ));
    }

    @Test
    void deveLiberarCatalogoCompletoDepoisDaRecomendacao() {
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

        service.recomendar(request);

        assertTrue(heroiService.listarTodos().stream().anyMatch(heroi ->
            !heroi.getRotasPossiveis().contains(Rota.FARM_LANE)
        ));
    }
}
