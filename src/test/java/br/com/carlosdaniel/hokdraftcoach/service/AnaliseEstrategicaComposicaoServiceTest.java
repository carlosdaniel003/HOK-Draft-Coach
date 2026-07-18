package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.CondicaoVitoriaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.model.CondicaoVitoriaTipo;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TipoRespostaCondicaoVitoria;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoClashRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoJungleRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;

class AnaliseEstrategicaComposicaoServiceTest {

    private AnaliseEstrategicaComposicaoService service;

    @BeforeEach
    void configurar() {
        HeroiService heroiService = new HeroiServiceCompleto(
            new CatalogoSuporteRepository(),
            new CatalogoMidRepository(),
            new CatalogoJungleRepository(),
            new CatalogoClashRepository()
        );
        DnaHeroiService dnaHeroiService = new DnaHeroiService();
        EconomiaRecursosService economia = new EconomiaRecursosService();
        CondicaoVitoriaService condicoes = new CondicaoVitoriaService(
            dnaHeroiService,
            economia
        );
        NecessidadePenalidadeService necessidades =
            new NecessidadePenalidadeService(dnaHeroiService, economia);
        service = new AnaliseEstrategicaComposicaoService(
            heroiService,
            dnaHeroiService,
            condicoes,
            economia,
            necessidades
        );
    }

    @Test
    void deveDescobrirPlanoDeHouYiMingELinhaDeFrente() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Garo"),
            List.of("Hou Yi", "Ming", "Lian Po")
        );

        CondicaoVitoriaResponse principal = resposta
            .condicoesVitoriaInimigas()
            .stream()
            .filter(CondicaoVitoriaResponse::principal)
            .findFirst()
            .orElseThrow();

        assertTrue(
            principal.tipo() == CondicaoVitoriaTipo.PROTEGER_HIPERCARREGADOR
        );
        assertTrue(principal.titulo().contains("Hou Yi"));
        assertTrue(principal.executores().contains("Ming"));
        assertTrue(principal.respostasAdversarias().stream().anyMatch(
            respostaCondicao -> respostaCondicao.tipo()
                == TipoRespostaCondicaoVitoria.ACESSAR_E_ELIMINAR_CARREGADOR
        ));
        assertTrue(principal.respostasAdversarias().stream().anyMatch(
            respostaCondicao -> respostaCondicao.tipo()
                == TipoRespostaCondicaoVitoria
                    .SEPARAR_CARREGADOR_DO_AMPLIFICADOR
        ));
        assertTrue(principal.respostasAdversarias().stream().anyMatch(
            respostaCondicao -> respostaCondicao.tipo()
                == TipoRespostaCondicaoVitoria
                    .QUEBRAR_FORMACAO_DA_LINHA_DE_FRENTE
        ));
    }

    @Test
    void deveCalcularNecessidadesImediatamente() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Garo", "Luban No.7"),
            List.of()
        );

        assertTrue(resposta.necessidades().stream().anyMatch(
            necessidade -> necessidade.dimensao()
                == DimensaoEstrategica.ENGAGE
        ));
        assertTrue(resposta.necessidades().stream().anyMatch(
            necessidade -> necessidade.dimensao()
                == DimensaoEstrategica.LINHA_DE_FRENTE
        ));
        assertTrue(
            resposta.necessidades().getFirst().urgencia()
                >= resposta.necessidades().getLast().urgencia()
        );
    }

    @Test
    void devePenalizarTresIniciadoresSemFollowUp() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Guiguzi", "Lian Po", "Liu Shan"),
            List.of()
        );

        assertTrue(resposta.penalidades().stream().anyMatch(
            penalidade -> penalidade.codigo()
                .equals("INICIACAO_REDUNDANTE_SEM_FOLLOW_UP")
        ));
    }

    @Test
    void devePenalizarDoisSuportesDeCuraSemDps() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Cai Yan", "Dolia"),
            List.of()
        );

        assertTrue(resposta.penalidades().stream().anyMatch(
            penalidade -> penalidade.codigo()
                .equals("SUPORTES_DE_SUSTENTACAO_SEM_DPS")
        ));
    }

    @Test
    void deveIdentificarConflitoDeRecursosEExigirEscolhaEconomica() {
        DiagnosticoComposicaoResponse resposta = service.diagnosticar(
            List.of("Garo", "Hou Yi", "Wukong"),
            List.of()
        );

        assertTrue(
            resposta.economiaNossaComposicao().carregadoresDependentes() >= 3
        );
        assertFalse(resposta.economiaNossaComposicao().economiaViavel());
        assertTrue(resposta.necessidades().stream().anyMatch(
            necessidade -> necessidade.codigo()
                .equals("BAIXA_DEPENDENCIA_RECURSOS")
        ));
        assertTrue(resposta.penalidades().stream().anyMatch(
            penalidade -> penalidade.codigo().equals("CONFLITO_DE_RECURSOS")
        ));
    }

    @Test
    void deveFavorecerTopQueConsegueCederRecursos() {
        List<RecomendacaoDnaResponse> recomendacoes = service.recomendar(
            List.of("Garo", "Hou Yi", "Wukong"),
            List.of(),
            Rota.CLASH_LANE,
            50
        );

        RecomendacaoDnaResponse arthur = recomendacoes.stream()
            .filter(recomendacao -> recomendacao.heroi().equals("Arthur"))
            .findFirst()
            .orElseThrow();
        RecomendacaoDnaResponse florentino = recomendacoes.stream()
            .filter(recomendacao -> recomendacao.heroi().equals("Florentino"))
            .findFirst()
            .orElseThrow();

        assertTrue(
            arthur.dependenciaRecursos() < florentino.dependenciaRecursos()
        );
        assertTrue(arthur.ajusteEconomico() > florentino.ajusteEconomico());
        assertNotNull(arthur.motivos());
    }
}
