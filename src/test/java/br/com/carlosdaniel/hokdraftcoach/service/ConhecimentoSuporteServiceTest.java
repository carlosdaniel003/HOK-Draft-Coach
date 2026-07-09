package br.com.carlosdaniel.hokdraftcoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoSuporteResponse;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaBotLane;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoMidRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.CatalogoSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.PerfilSuporteRepository;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaBotLaneRepository;

class ConhecimentoSuporteServiceTest {

    private HeroiService heroiService;
    private ConhecimentoSuporteService conhecimentoSuporteService;

    @BeforeEach
    void configurar() {
        CatalogoSuporteRepository catalogo = new CatalogoSuporteRepository();
        CatalogoMidRepository catalogoMid = new CatalogoMidRepository();
        PerfilSuporteRepository perfis = new PerfilSuporteRepository();
        SinergiaBotLaneRepository sinergias = new SinergiaBotLaneRepository();

        heroiService = new HeroiServiceCompleto(catalogo, catalogoMid);
        conhecimentoSuporteService = new ConhecimentoSuporteService(
            heroiService,
            catalogo,
            perfis,
            sinergias
        );
    }

    @Test
    void deveCadastrarOsVinteETresSuportesInformados() {
        Set<String> esperados = Set.of(
            "Annette", "Lapulapu", "Cai Yan", "Da Qiao", "Dolia",
            "Donghuang", "Dun", "Dyadia", "Guiguzi", "Kui", "Lian Po",
            "Liu Bang", "Liu Shan", "Ming", "Mozi", "Sakeer", "Sun Bin",
            "Xiang Yu", "Yaria", "Yuhuan", "Zhang Fei", "Zhuangzi", "Ziya"
        );

        Set<String> cadastrados = heroiService.listarPorRota(Rota.ROAMING)
            .stream()
            .map(Heroi::getNome)
            .collect(Collectors.toSet());

        assertEquals(23, cadastrados.size());
        assertEquals(esperados, cadastrados);
    }

    @Test
    void deveResolverAliasesERotasFlexiveisDosSuportes() {
        assertEquals("Lapulapu", buscar("Lapu-Lapu").getNome());
        assertEquals("Sakeer", buscar("Sakker").getNome());
        assertEquals("Yaria", buscar("Yao").getNome());
        assertEquals("Kui", buscar("Zhong Kui").getNome());

        assertTrue(buscar("Dun").podeJogarNaRota(Rota.CLASH_LANE));
        assertTrue(buscar("Dun").podeJogarNaRota(Rota.ROAMING));
        assertTrue(buscar("Mozi").podeJogarNaRota(Rota.MID_LANE));
        assertTrue(buscar("Mozi").podeJogarNaRota(Rota.ROAMING));
    }

    @Test
    void devePriorizarLianPoParaMarcoPolo() {
        List<SinergiaBotLane> recomendacoes = conhecimentoSuporteService
            .melhoresSuportesParaAtirador("Marco Polo", 10);

        assertFalse(recomendacoes.isEmpty());
        assertEquals("Lian Po", recomendacoes.getFirst().suporte());
        assertEquals(10, recomendacoes.getFirst().nota());

        Set<String> suportes = recomendacoes.stream()
            .map(SinergiaBotLane::suporte)
            .collect(Collectors.toSet());

        assertTrue(suportes.contains("Lapulapu"));
        assertTrue(suportes.contains("Sakeer"));
        assertTrue(suportes.contains("Da Qiao"));
        assertTrue(suportes.contains("Zhang Fei"));
        assertTrue(suportes.contains("Yaria"));
        assertTrue(suportes.contains("Dolia"));
    }

    @Test
    void deveReconhecerZhuangziComoRespostaAControlePesado() {
        List<RecomendacaoComposicaoSuporteResponse> recomendacoes =
            conhecimentoSuporteService.recomendarContraComposicao(
                List.of(TipoComposicao.CONTROLE_PESADO),
                10
            );

        RecomendacaoComposicaoSuporteResponse zhuangzi = recomendacoes.stream()
            .filter(resultado -> resultado.suporte().equals("Zhuangzi"))
            .findFirst()
            .orElseThrow();

        assertTrue(zhuangzi.pontuacao() > 50);
        assertTrue(
            zhuangzi.respondeA().contains(TipoComposicao.CONTROLE_PESADO)
        );
    }

    @Test
    void deveManterUmPerfilEstrategicoParaCadaSuporte() {
        assertEquals(23, conhecimentoSuporteService.listarSuportes().size());
        conhecimentoSuporteService.listarSuportes().forEach(resultado -> {
            assertEquals(resultado.heroi().getNome(), resultado.perfil().heroi());
            assertFalse(resultado.perfil().arquetipos().isEmpty());
            assertFalse(resultado.perfil().observacoes().isEmpty());
        });
    }

    @Test
    void devePontuarSinergiaParaUsoNoMotorDeRecomendacao() {
        Heroi marco = buscar("Marco Polo");
        Heroi lianPo = buscar("Lian Po");
        Heroi ziya = buscar("Ziya");

        assertTrue(
            conhecimentoSuporteService.pontuarSinergia(
                lianPo,
                List.of(marco)
            ) > conhecimentoSuporteService.pontuarSinergia(
                ziya,
                List.of(marco)
            )
        );
    }

    private Heroi buscar(String nome) {
        return heroiService.buscarPorNome(nome).orElseThrow();
    }
}
