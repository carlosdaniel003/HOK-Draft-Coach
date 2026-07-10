from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if new in text:
        return text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: esperado 1 bloco, encontrado {count}")
    return text.replace(old, new, 1)


controller_path = Path(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/controller/DraftController.java"
)
controller = controller_path.read_text(encoding="utf-8")
controller = replace_once(
    controller,
    "import br.com.carlosdaniel.hokdraftcoach.service.RecomendacaoProximoPickDnaService;\n",
    "import br.com.carlosdaniel.hokdraftcoach.service.RecomendacaoProximoPickDnaService;\n"
    "import br.com.carlosdaniel.hokdraftcoach.service.RecomendacaoProximoPickService;\n",
    "Import do motor rápido",
)
controller = replace_once(
    controller,
    "    private final InferenciaFuncoesService inferenciaFuncoesService;\n"
    "    private final RecomendacaoProximoPickDnaService proximoPickDnaService;\n",
    "    private final InferenciaFuncoesService inferenciaFuncoesService;\n"
    "    private final RecomendacaoProximoPickService proximoPickRapidoService;\n"
    "    private final RecomendacaoProximoPickDnaService proximoPickDnaService;\n",
    "Campo do motor rápido",
)
controller = replace_once(
    controller,
    "        DraftDnaService draftDnaService,\n"
    "        InferenciaFuncoesService inferenciaFuncoesService,\n"
    "        RecomendacaoProximoPickDnaService proximoPickDnaService\n"
    "    ) {\n"
    "        this.draftDnaService = draftDnaService;\n"
    "        this.inferenciaFuncoesService = inferenciaFuncoesService;\n"
    "        this.proximoPickDnaService = proximoPickDnaService;\n",
    "        DraftDnaService draftDnaService,\n"
    "        InferenciaFuncoesService inferenciaFuncoesService,\n"
    "        RecomendacaoProximoPickService proximoPickRapidoService,\n"
    "        RecomendacaoProximoPickDnaService proximoPickDnaService\n"
    "    ) {\n"
    "        this.draftDnaService = draftDnaService;\n"
    "        this.inferenciaFuncoesService = inferenciaFuncoesService;\n"
    "        this.proximoPickRapidoService = proximoPickRapidoService;\n"
    "        this.proximoPickDnaService = proximoPickDnaService;\n",
    "Construtor do motor rápido",
)
controller = replace_once(
    controller,
    "    @PostMapping(\"/recomendar-proximo-pick\")\n"
    "    public RecomendacaoProximoPickResponse recomendarProximoPick(\n",
    "    @PostMapping(\"/recomendar-proximo-pick/rapido\")\n"
    "    public RecomendacaoProximoPickResponse recomendarProximoPickRapido(\n"
    "        @Valid @RequestBody RecomendacaoProximoPickRequest request\n"
    "    ) {\n"
    "        return proximoPickRapidoService.recomendar(request);\n"
    "    }\n\n"
    "    @PostMapping(\"/recomendar-proximo-pick\")\n"
    "    public RecomendacaoProximoPickResponse recomendarProximoPick(\n",
    "Endpoint rápido",
)
controller_path.write_text(controller, encoding="utf-8")


service_path = Path(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/service/"
    "RecomendacaoProximoPickDnaService.java"
)
service = service_path.read_text(encoding="utf-8")
service = replace_once(
    service,
    "import java.util.Set;\n",
    "import java.util.Set;\n"
    "import java.util.concurrent.ConcurrentHashMap;\n"
    "import java.util.concurrent.ConcurrentMap;\n",
    "Imports de cache detalhado",
)
service = replace_once(
    service,
    "    private static final int LIMITE_ALTERNATIVAS = 2;\n\n"
    "    private final RecomendacaoProximoPickService recomendacaoBase;\n",
    "    private static final int LIMITE_ALTERNATIVAS = 2;\n"
    "    private static final int LIMITE_CACHE_RECOMENDACOES = 128;\n\n"
    "    private final RecomendacaoProximoPickService recomendacaoBase;\n",
    "Limite do cache detalhado",
)
service = replace_once(
    service,
    "    private final ProjecaoRespostaInimigaService projecaoService;\n"
    "    private final ExplicacaoRecomendacaoService explicacaoService;\n",
    "    private final ProjecaoRespostaInimigaService projecaoService;\n"
    "    private final ExplicacaoRecomendacaoService explicacaoService;\n"
    "    private final ConcurrentMap<\n"
    "        RecomendacaoProximoPickRequest,\n"
    "        RecomendacaoProximoPickResponse\n"
    "    > cacheRecomendacoes = new ConcurrentHashMap<>();\n",
    "Mapa de cache detalhado",
)
service = replace_once(
    service,
    "    public RecomendacaoProximoPickResponse recomendar(\n"
    "        RecomendacaoProximoPickRequest request\n"
    "    ) {\n"
    "        RecomendacaoProximoPickRequest requestAlvo =\n",
    "    public RecomendacaoProximoPickResponse recomendar(\n"
    "        RecomendacaoProximoPickRequest request\n"
    "    ) {\n"
    "        RecomendacaoProximoPickRequest chave = copiarRequest(request);\n"
    "        if (\n"
    "            cacheRecomendacoes.size() >= LIMITE_CACHE_RECOMENDACOES\n"
    "                && !cacheRecomendacoes.containsKey(chave)\n"
    "        ) {\n"
    "            cacheRecomendacoes.clear();\n"
    "        }\n"
    "        return cacheRecomendacoes.computeIfAbsent(\n"
    "            chave,\n"
    "            this::calcularRecomendacao\n"
    "        );\n"
    "    }\n\n"
    "    private RecomendacaoProximoPickResponse calcularRecomendacao(\n"
    "        RecomendacaoProximoPickRequest request\n"
    "    ) {\n"
    "        RecomendacaoProximoPickRequest requestAlvo =\n",
    "Wrapper de cache detalhado",
)
service = replace_once(
    service,
    "    private RecomendacaoProximoPickRequest requestParaProximoAliado(\n",
    "    private RecomendacaoProximoPickRequest copiarRequest(\n"
    "        RecomendacaoProximoPickRequest request\n"
    "    ) {\n"
    "        return new RecomendacaoProximoPickRequest(\n"
    "            request.meuLado(),\n"
    "            request.minhaOrdem(),\n"
    "            List.copyOf(request.bansAzul()),\n"
    "            List.copyOf(request.bansVermelho()),\n"
    "            List.copyOf(request.picksAzul()),\n"
    "            List.copyOf(request.picksVermelho()),\n"
    "            List.copyOf(request.funcoesPreferidas())\n"
    "        );\n"
    "    }\n\n"
    "    private RecomendacaoProximoPickRequest requestParaProximoAliado(\n",
    "Cópia imutável da chave de cache",
)
service_path.write_text(service, encoding="utf-8")


js_path = Path("src/main/resources/static/js/next-pick.js")
js = js_path.read_text(encoding="utf-8")
js = replace_once(
    js,
    "let controladorConsultaProximoPick = null;\n"
    "let assinaturaUltimaConsultaProximoPick = null;\n",
    "let controladorConsultaRapidaProximoPick = null;\n"
    "let controladorConsultaDetalhadaProximoPick = null;\n"
    "let assinaturaUltimaConsultaProximoPick = null;\n",
    "Controladores separados do frontend",
)
old_query = '''async function consultarRecomendacaoProximoPick() {
    const request = montarRequestProximoPick();
    const assinatura = JSON.stringify(request);

    if (
        assinatura === assinaturaUltimaConsultaProximoPick
            && resultadoAtualProximoPick
    ) {
        return;
    }

    const versaoAtual = ++versaoConsultaProximoPick;
    controladorConsultaProximoPick?.abort();
    controladorConsultaProximoPick = new AbortController();
    renderizarCarregamentoProximoPick();

    try {
        const resposta = await fetch(
            "/api/draft/recomendar-proximo-pick",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: assinatura,
                signal: controladorConsultaProximoPick.signal
            }
        );

        if (!resposta.ok) {
            throw new Error(await extrairErroProximoPick(resposta));
        }

        const resultado = await resposta.json();

        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        assinaturaUltimaConsultaProximoPick = assinatura;
        resultadoAtualProximoPick = resultado;
        renderizarRecomendacaoProximoPick(resultado);
    } catch (erro) {
        if (erro.name === "AbortError") {
            return;
        }
        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        resultadoAtualProximoPick = null;
        renderizarErroProximoPick(erro.message);
    }
}
'''
new_query = '''async function consultarRecomendacaoProximoPick() {
    const request = montarRequestProximoPick();
    const assinatura = JSON.stringify(request);

    if (
        assinatura === assinaturaUltimaConsultaProximoPick
            && resultadoAtualProximoPick
    ) {
        return;
    }

    const versaoAtual = ++versaoConsultaProximoPick;
    controladorConsultaRapidaProximoPick?.abort();
    controladorConsultaDetalhadaProximoPick?.abort();
    controladorConsultaRapidaProximoPick = new AbortController();
    controladorConsultaDetalhadaProximoPick = new AbortController();
    renderizarCarregamentoProximoPick();

    try {
        const resultadoRapido = await solicitarRecomendacaoProximoPick(
            "/api/draft/recomendar-proximo-pick/rapido",
            assinatura,
            controladorConsultaRapidaProximoPick.signal
        );

        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        assinaturaUltimaConsultaProximoPick = assinatura;
        resultadoAtualProximoPick = resultadoRapido;
        renderizarRecomendacaoProximoPick(resultadoRapido);
        renderizarRefinamentoProximoPick();

        try {
            const resultadoDetalhado = await solicitarRecomendacaoProximoPick(
                "/api/draft/recomendar-proximo-pick",
                assinatura,
                controladorConsultaDetalhadaProximoPick.signal
            );

            if (versaoAtual !== versaoConsultaProximoPick) {
                return;
            }

            resultadoAtualProximoPick = resultadoDetalhado;
            renderizarRecomendacaoProximoPick(resultadoDetalhado);
        } catch (erroDetalhado) {
            if (erroDetalhado.name === "AbortError") {
                return;
            }
            if (versaoAtual !== versaoConsultaProximoPick) {
                return;
            }

            renderizarRecomendacaoProximoPick(resultadoAtualProximoPick);
        }
    } catch (erro) {
        if (erro.name === "AbortError") {
            return;
        }
        if (versaoAtual !== versaoConsultaProximoPick) {
            return;
        }

        resultadoAtualProximoPick = null;
        renderizarErroProximoPick(erro.message);
    }
}

async function solicitarRecomendacaoProximoPick(url, assinatura, signal) {
    const resposta = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: assinatura,
        signal
    });

    if (!resposta.ok) {
        throw new Error(await extrairErroProximoPick(resposta));
    }

    return resposta.json();
}

function renderizarRefinamentoProximoPick() {
    statusProximoPick.textContent = "Refinando";
    statusProximoPick.className = "coach-status coach-status--carregando";
}
'''
js = replace_once(js, old_query, new_query, "Consulta progressiva no frontend")
js = replace_once(
    js,
    "            Recalculando necessidades, ameaças, projeções e respostas possíveis...\n",
    "            Calculando a recomendação inicial...\n",
    "Mensagem de carregamento inicial",
)
js_path.write_text(js, encoding="utf-8")


test_path = Path(
    "src/test/java/br/com/carlosdaniel/hokdraftcoach/service/"
    "IntegracaoDnaMotorDraftTest.java"
)
test = test_path.read_text(encoding="utf-8")
test = replace_once(
    test,
    "import static org.junit.jupiter.api.Assertions.assertNotNull;\n",
    "import static org.junit.jupiter.api.Assertions.assertNotNull;\n"
    "import static org.junit.jupiter.api.Assertions.assertSame;\n",
    "Import do teste de cache",
)
cache_test = '''    @Test
    void deveReutilizarAnaliseDetalhadaParaOMesmoEstadoDoDraft() {
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                1,
                List.of(1L, 2L, 3L),
                List.of(1L, 4L, 5L),
                List.of(),
                List.of(),
                List.of(Rota.ROAMING)
            );

        RecomendacaoProximoPickResponse primeira =
            proximoPickDnaService.recomendar(request);
        RecomendacaoProximoPickResponse segunda =
            proximoPickDnaService.recomendar(request);

        assertSame(primeira, segunda);
    }

'''
test = replace_once(
    test,
    "    @Test\n    void deveProjetarRespostasInimigasDesdeOPrimeiroPick() {\n",
    cache_test
    + "    @Test\n    void deveProjetarRespostasInimigasDesdeOPrimeiroPick() {\n",
    "Teste de cache detalhado",
)
test_path.write_text(test, encoding="utf-8")


controller_test_path = Path(
    "src/test/java/br/com/carlosdaniel/hokdraftcoach/controller/"
    "DraftControllerTest.java"
)
controller_test_path.parent.mkdir(parents=True, exist_ok=True)
controller_test_path.write_text(
    '''package br.com.carlosdaniel.hokdraftcoach.controller;

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
''',
    encoding="utf-8",
)
