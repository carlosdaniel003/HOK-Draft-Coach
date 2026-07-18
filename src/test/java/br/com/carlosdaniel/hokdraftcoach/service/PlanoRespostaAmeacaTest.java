package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.AlvoPrioritarioAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilAmeacaHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.PapelAmeaca;

class PlanoRespostaAmeacaTest {

    private final PerfilAmeacaHeroiResponse carregador = perfil(
        "Chano",
        PapelAmeaca.AMEACA_PRINCIPAL
    );

    @Test
    void deveTratarIniciadorComoJanelaENaoComoAlvoParaMatar() {
        String plano = PlanoRespostaAmeaca.criar(
            carregador,
            List.of(
                alvo("Lian Po", PapelAmeaca.INICIADOR, 95),
                alvo("Chano", PapelAmeaca.AMEACA_PRINCIPAL, 70)
            )
        );

        assertTrue(plano.contains("não é necessário eliminá-lo"));
        assertTrue(plano.contains("avance sobre Chano"));
    }

    @Test
    void deveMandarForcarProtecaoETrocarOFoco() {
        String plano = PlanoRespostaAmeaca.criar(
            carregador,
            List.of(
                alvo("Dolia", PapelAmeaca.PROTETOR, 94),
                alvo("Chano", PapelAmeaca.AMEACA_PRINCIPAL, 70)
            )
        );

        assertTrue(plano.contains("não gaste o combo inteiro no protetor"));
        assertTrue(plano.contains("mude imediatamente para Chano"));
    }

    @Test
    void deveEvitarPerseguirSuporteQuandoLiberaOCarregador() {
        String plano = PlanoRespostaAmeaca.criar(
            carregador,
            List.of(
                alvo("Ming", PapelAmeaca.HABILITADOR, 93),
                alvo("Chano", PapelAmeaca.AMEACA_PRINCIPAL, 70)
            )
        );

        assertTrue(plano.contains("não persiga o suporte"));
        assertTrue(plano.contains("troque o foco para Chano"));
    }

    @Test
    void deveProduzirPlanosDiferentesPorPapel() {
        String iniciador = PlanoRespostaAmeaca.criar(
            carregador,
            List.of(
                alvo("Lian Po", PapelAmeaca.INICIADOR, 95),
                alvo("Chano", PapelAmeaca.AMEACA_PRINCIPAL, 70)
            )
        );
        String protetor = PlanoRespostaAmeaca.criar(
            carregador,
            List.of(
                alvo("Dolia", PapelAmeaca.PROTETOR, 95),
                alvo("Chano", PapelAmeaca.AMEACA_PRINCIPAL, 70)
            )
        );

        assertNotEquals(iniciador, protetor);
    }

    private PerfilAmeacaHeroiResponse perfil(
        String heroi,
        PapelAmeaca papel
    ) {
        return new PerfilAmeacaHeroiResponse(
            heroi,
            90,
            30,
            20,
            20,
            45,
            List.of(papel),
            List.of()
        );
    }

    private AlvoPrioritarioAmeacaResponse alvo(
        String heroi,
        PapelAmeaca papel,
        int prioridade
    ) {
        return new AlvoPrioritarioAmeacaResponse(
            heroi,
            papel,
            prioridade,
            List.of(DimensaoEstrategica.CONTROLE),
            "Teste"
        );
    }
}
