package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;

class DnaComposicaoServiceTest {

    private HeroiService heroiService;
    private DnaHeroiService dnaHeroiService;
    private DnaComposicaoService dnaComposicaoService;

    @BeforeEach
    void configurar() {
        heroiService = new HeroiServiceCompleto(
            new CatalogoSuporteRepository(),
            new CatalogoMidRepository(),
            new CatalogoJungleRepository(),
            new CatalogoClashRepository()
        );
        dnaHeroiService = new DnaHeroiService();
        dnaComposicaoService = new DnaComposicaoService(
            heroiService,
            dnaHeroiService
        );
    }

    @Test
    void deveGerarAsDezenoveDimensoesParaTodoHeroi() {
        heroiService.listarTodos().forEach(heroi -> {
            DnaHeroi dna = dnaHeroiService.calcular(heroi);

            assertEquals(DimensaoEstrategica.values().length, dna.vetor().size());
            dna.vetor().values().forEach(valor -> {
                assertTrue(valor >= 0);
                assertTrue(valor <= 100);
            });
        });
    }

    @Test
    void deveDiagnosticarDanoSemIniciacao() {
        DiagnosticoComposicaoResponse resposta =
            dnaComposicaoService.diagnosticar(
                List.of("Garo", "Luban No.7"),
                List.of()
            );

        assertTrue(resposta.diagnosticoConcluido());
        assertTrue(resposta.diagnosticos().stream().anyMatch(
            diagnostico -> diagnostico.codigo().equals("DANO_SEM_INICIACAO")
        ));
        assertTrue(resposta.prioridades().stream().anyMatch(
            prioridade -> prioridade.dimensao() == DimensaoEstrategica.ENGAGE
        ));
    }

    @Test
    void deveDiagnosticarTresTanquesSemRespostaSuficiente() {
        DiagnosticoComposicaoResponse resposta =
            dnaComposicaoService.diagnosticar(
                List.of("Angela", "Daji"),
                List.of("Ata", "Bai Qi", "Lian Po")
            );

        assertTrue(resposta.diagnosticos().stream().anyMatch(
            diagnostico -> diagnostico.codigo().equals("SEM_RESPOSTA_A_TANQUES")
        ));
        assertTrue(resposta.prioridades().stream().anyMatch(
            prioridade ->
                prioridade.dimensao() == DimensaoEstrategica.ANTI_TANQUE
        ));
    }

    @Test
    void deveDiagnosticarDiveSemPeel() {
        DiagnosticoComposicaoResponse resposta =
            dnaComposicaoService.diagnosticar(
                List.of("Garo", "Luban No.7"),
                List.of("Lam", "Sima Yi", "Mulan")
            );

        assertTrue(resposta.diagnosticos().stream().anyMatch(
            diagnostico -> diagnostico.codigo().equals("VULNERAVEL_A_DIVE")
        ));
    }

    @Test
    void deveDiagnosticarSustainSemAntiCura() {
        DiagnosticoComposicaoResponse resposta =
            dnaComposicaoService.diagnosticar(
                List.of("Garo", "Angela"),
                List.of("Fatih", "Mi Yue", "Cai Yan")
            );

        assertTrue(resposta.diagnosticos().stream().anyMatch(
            diagnostico -> diagnostico.codigo().equals("SEM_ANTI_CURA")
        ));
        assertTrue(resposta.prioridades().stream().anyMatch(
            prioridade -> prioridade.dimensao() == DimensaoEstrategica.ANTI_CURA
        ));
    }

    @Test
    void deveRecomendarAntesDeTudoQuemCorrigeODeficit() {
        DnaComposicao antes = dnaComposicaoService.gerarDnaPorNomes(
            List.of("Garo", "Luban No.7")
        );
        List<RecomendacaoDnaResponse> recomendacoes =
            dnaComposicaoService.recomendar(
                List.of("Garo", "Luban No.7"),
                List.of("Ata", "Bai Qi"),
                Rota.ROAMING,
                10
            );

        assertFalse(recomendacoes.isEmpty());
        RecomendacaoDnaResponse recomendada = recomendacoes.getFirst();
        assertTrue(
            recomendada.corrige().contains(DimensaoEstrategica.ENGAGE)
                || recomendada.corrige().contains(DimensaoEstrategica.PEEL)
                || recomendada.corrige().contains(
                    DimensaoEstrategica.LINHA_DE_FRENTE
                )
        );

        DnaComposicao depois = dnaComposicaoService.gerarDnaPorNomes(
            List.of("Garo", "Luban No.7", recomendada.heroi())
        );
        assertTrue(
            depois.valor(DimensaoEstrategica.ENGAGE)
                > antes.valor(DimensaoEstrategica.ENGAGE)
                || depois.valor(DimensaoEstrategica.PEEL)
                    > antes.valor(DimensaoEstrategica.PEEL)
                || depois.valor(DimensaoEstrategica.LINHA_DE_FRENTE)
                    > antes.valor(DimensaoEstrategica.LINHA_DE_FRENTE)
        );
    }

    @Test
    void deveRegistrarDistribuicaoDeDanoEArquetipos() {
        DnaComposicao dna = dnaComposicaoService.gerarDnaPorNomes(
            List.of("Marco Polo", "Angela", "Lian Po")
        );

        assertEquals(3, dna.herois().size());
        assertTrue(dna.distribuicaoDano().fisico() > 0);
        assertTrue(dna.distribuicaoDano().magico() > 0);
        assertFalse(dna.niveis().isEmpty());
    }

    @Test
    void deveRecusarComposicaoInvalida() {
        assertThrows(
            RegraNegocioException.class,
            () -> dnaComposicaoService.gerarDnaPorNomes(
                List.of("Garo", "Garo")
            )
        );
        assertThrows(
            RegraNegocioException.class,
            () -> dnaComposicaoService.gerarDnaPorNomes(
                List.of("Herói inexistente")
            )
        );
    }
}
