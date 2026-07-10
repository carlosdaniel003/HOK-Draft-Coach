from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: esperado 1 bloco, encontrado {count}")
    return text.replace(old, new, 1)


hero_path = Path("src/main/java/br/com/carlosdaniel/hokdraftcoach/service/HeroiService.java")
hero = hero_path.read_text(encoding="utf-8")
hero = replace_once(
    hero,
    'private static final String VERSAO_META_FARM = "S15-HOK-PLUS-2.0";',
    'private static final String VERSAO_META_FARM = "S15-HOK-PLUS-2.1";',
    "Versão do meta da Farm Lane",
)
hero = replace_once(
    hero,
    'private static final LocalDate DATA_META_FARM = LocalDate.of(2026, 7, 2);',
    'private static final LocalDate DATA_META_FARM = LocalDate.of(2026, 7, 10);',
    "Data do meta da Farm Lane",
)
hero = replace_once(
    hero,
    '''        criarHeroiFarm(
            25L,
            "Chicha",
            List.of(),
            ClasseHeroi.LUTADOR,
            List.of(Rota.CLASH_LANE, Rota.JUNGLE, Rota.FARM_LANE),
            "Lutadora flex de velocidade de ataque e limpeza",
            1,
            TipoDano.FISICO,
            new AtributosHeroi(4, 6, 7, 4, 9, 6),
            TierMeta.A,
            "flex", "velocidade de ataque", "limpeza", "duelo", "lutadora"
        ),''',
    '''        criarHeroiFlex(
            25L,
            "Chicha",
            List.of(),
            ClasseHeroi.LUTADOR,
            Rota.JUNGLE,
            List.of(Rota.JUNGLE, Rota.CLASH_LANE),
            "Lutadora de selva com velocidade de ataque, limpeza e duelo",
            1,
            TipoDano.FISICO,
            new AtributosHeroi(4, 6, 7, 4, 9, 6),
            "flex", "jungle", "velocidade de ataque", "limpeza", "duelo", "lutadora"
        ),''',
    "Cadastro da Chicha",
)
hero_path.write_text(hero, encoding="utf-8")

infer_path = Path("src/main/java/br/com/carlosdaniel/hokdraftcoach/service/InferenciaFuncoesService.java")
infer = infer_path.read_text(encoding="utf-8")
infer = replace_once(
    infer,
    "import java.util.Set;\n",
    "import java.util.Set;\nimport java.util.concurrent.ConcurrentHashMap;\nimport java.util.concurrent.ConcurrentMap;\n",
    "Imports de cache da inferência",
)
infer = replace_once(
    infer,
    "    private static final int PONTOS_ROTA_SECUNDARIA = 12;\n",
    "    private static final int PONTOS_ROTA_SECUNDARIA = 12;\n    private static final int LIMITE_CACHE_INFERENCIA = 128;\n",
    "Limite do cache da inferência",
)
infer = replace_once(
    infer,
    "    private final HeroiService heroiService;\n",
    "    private final HeroiService heroiService;\n    private final ConcurrentMap<ChaveInferencia, InferenciaFuncoesResponse> cacheInferencias =\n        new ConcurrentHashMap<>();\n",
    "Campo de cache da inferência",
)
infer = replace_once(
    infer,
    '''    public InferenciaFuncoesResponse inferir(
        InferenciaFuncoesRequest request
    ) {
        List<PickInterno> picksAzul = carregarPicks(''',
    '''    public InferenciaFuncoesResponse inferir(
        InferenciaFuncoesRequest request
    ) {
        ChaveInferencia chave = new ChaveInferencia(
            List.copyOf(request.picksAzul()),
            List.copyOf(request.picksVermelho())
        );
        if (
            cacheInferencias.size() >= LIMITE_CACHE_INFERENCIA
                && !cacheInferencias.containsKey(chave)
        ) {
            cacheInferencias.clear();
        }
        return cacheInferencias.computeIfAbsent(
            chave,
            ignorada -> calcularInferencia(request)
        );
    }

    private InferenciaFuncoesResponse calcularInferencia(
        InferenciaFuncoesRequest request
    ) {
        List<PickInterno> picksAzul = carregarPicks(''',
    "Wrapper de cache da inferência",
)
infer = replace_once(
    infer,
    '''    private record PickInterno(
        String slot,
        Heroi heroi
    ) {''',
    '''    private record ChaveInferencia(
        List<PickSemFuncaoRequest> picksAzul,
        List<PickSemFuncaoRequest> picksVermelho
    ) {
    }

    private record PickInterno(
        String slot,
        Heroi heroi
    ) {''',
    "Chave do cache da inferência",
)
infer_path.write_text(infer, encoding="utf-8")

analysis_path = Path("src/main/java/br/com/carlosdaniel/hokdraftcoach/service/AnaliseAmeacaComposicaoService.java")
analysis = analysis_path.read_text(encoding="utf-8")
analysis = replace_once(
    analysis,
    "import java.util.Set;\n",
    "import java.util.Set;\nimport java.util.concurrent.ConcurrentHashMap;\nimport java.util.concurrent.ConcurrentMap;\n",
    "Imports de cache da análise",
)
analysis = replace_once(
    analysis,
    "public class AnaliseAmeacaComposicaoService\n    extends AnaliseTemporalSinergiaService {\n\n",
    "public class AnaliseAmeacaComposicaoService\n    extends AnaliseTemporalSinergiaService {\n\n    private static final int LIMITE_CACHE_ANALISE = 256;\n\n",
    "Limite do cache estratégico",
)
analysis = replace_once(
    analysis,
    '''    private final HeroiService heroiService;
    private final AnaliseAmeacaService analiseAmeacaService;
''',
    '''    private final HeroiService heroiService;
    private final AnaliseAmeacaService analiseAmeacaService;
    private final ConcurrentMap<ChaveDiagnostico, DiagnosticoComposicaoResponse> cacheDiagnosticos =
        new ConcurrentHashMap<>();
    private final ConcurrentMap<ChaveRecomendacao, List<RecomendacaoDnaResponse>> cacheRecomendacoes =
        new ConcurrentHashMap<>();
''',
    "Campos de cache estratégico",
)
analysis = replace_once(
    analysis,
    '''    @Override
    public DiagnosticoComposicaoResponse diagnosticar(
        List<String> aliados,
        List<String> inimigos
    ) {
        DiagnosticoComposicaoResponse base = super.diagnosticar(''',
    '''    @Override
    public DiagnosticoComposicaoResponse diagnosticar(
        List<String> aliados,
        List<String> inimigos
    ) {
        ChaveDiagnostico chave = new ChaveDiagnostico(
            copiarNomes(aliados),
            copiarNomes(inimigos)
        );
        if (
            cacheDiagnosticos.size() >= LIMITE_CACHE_ANALISE
                && !cacheDiagnosticos.containsKey(chave)
        ) {
            cacheDiagnosticos.clear();
        }
        return cacheDiagnosticos.computeIfAbsent(
            chave,
            ignorada -> calcularDiagnostico(aliados, inimigos)
        );
    }

    private DiagnosticoComposicaoResponse calcularDiagnostico(
        List<String> aliados,
        List<String> inimigos
    ) {
        DiagnosticoComposicaoResponse base = super.diagnosticar(''',
    "Cache de diagnósticos",
)
analysis = replace_once(
    analysis,
    '''    @Override
    public List<RecomendacaoDnaResponse> recomendar(
        List<String> aliados,
        List<String> inimigos,
        Rota rota,
        int limite
    ) {
        List<RecomendacaoDnaResponse> base = super.recomendar(''',
    '''    @Override
    public List<RecomendacaoDnaResponse> recomendar(
        List<String> aliados,
        List<String> inimigos,
        Rota rota,
        int limite
    ) {
        ChaveRecomendacao chave = new ChaveRecomendacao(
            copiarNomes(aliados),
            copiarNomes(inimigos),
            rota,
            limite
        );
        if (
            cacheRecomendacoes.size() >= LIMITE_CACHE_ANALISE
                && !cacheRecomendacoes.containsKey(chave)
        ) {
            cacheRecomendacoes.clear();
        }
        return cacheRecomendacoes.computeIfAbsent(
            chave,
            ignorada -> calcularRecomendacao(aliados, inimigos, rota, limite)
        );
    }

    private List<RecomendacaoDnaResponse> calcularRecomendacao(
        List<String> aliados,
        List<String> inimigos,
        Rota rota,
        int limite
    ) {
        List<RecomendacaoDnaResponse> base = super.recomendar(''',
    "Cache de recomendações estratégicas",
)
analysis = replace_once(
    analysis,
    "    private String normalizar(String valor) {",
    '''    private List<String> copiarNomes(List<String> nomes) {
        return nomes == null ? List.of() : List.copyOf(nomes);
    }

    private record ChaveDiagnostico(
        List<String> aliados,
        List<String> inimigos
    ) {
    }

    private record ChaveRecomendacao(
        List<String> aliados,
        List<String> inimigos,
        Rota rota,
        int limite
    ) {
    }

    private String normalizar(String valor) {''',
    "Chaves do cache estratégico",
)
analysis_path.write_text(analysis, encoding="utf-8")

js_path = Path("src/main/resources/static/js/next-pick.js")
js = js_path.read_text(encoding="utf-8")
js = replace_once(
    js,
    '''let resultadoAtualProximoPick = null;
let abaAnaliseAtual = "now";''',
    '''let resultadoAtualProximoPick = null;
let abaAnaliseAtual = "now";
let controladorConsultaProximoPick = null;
let assinaturaUltimaConsultaProximoPick = null;''',
    "Estado de cancelamento da consulta",
)
js = replace_once(js, "        180\n", "        90\n", "Debounce da recomendação")
js = replace_once(
    js,
    '''async function consultarRecomendacaoProximoPick() {
    const versaoAtual = ++versaoConsultaProximoPick;
    renderizarCarregamentoProximoPick();

    try {
        const resposta = await fetch(
            "/api/draft/recomendar-proximo-pick",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(montarRequestProximoPick())
            }
        );''',
    '''async function consultarRecomendacaoProximoPick() {
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
        );''',
    "Cancelamento de fetch duplicado",
)
js = replace_once(
    js,
    '''        resultadoAtualProximoPick = resultado;
        renderizarRecomendacaoProximoPick(resultado);
    } catch (erro) {
        if (versaoAtual !== versaoConsultaProximoPick) {''',
    '''        assinaturaUltimaConsultaProximoPick = assinatura;
        resultadoAtualProximoPick = resultado;
        renderizarRecomendacaoProximoPick(resultado);
    } catch (erro) {
        if (erro.name === "AbortError") {
            return;
        }
        if (versaoAtual !== versaoConsultaProximoPick) {''',
    "Tratamento de consulta cancelada",
)
js_path.write_text(js, encoding="utf-8")

hero_test_path = Path("src/test/java/br/com/carlosdaniel/hokdraftcoach/service/HeroiServiceTest.java")
hero_test = hero_test_path.read_text(encoding="utf-8")
hero_test = hero_test.replace(
    "void deveCadastrarOsVinteHeroisAtuaisDaFarmLane()",
    "void deveCadastrarOsDezenoveHeroisAtuaisDaFarmLane()",
)
hero_test = hero_test.replace('            "Chicha",\n', "", 1)
hero_test = replace_once(
    hero_test,
    "        assertEquals(20, cadastrados.size());",
    "        assertEquals(19, cadastrados.size());",
    "Quantidade de heróis da Farm Lane",
)
hero_test = replace_once(
    hero_test,
    '''        assertTrue(chicha.podeJogarNaRota(Rota.CLASH_LANE));
        assertTrue(chicha.podeJogarNaRota(Rota.JUNGLE));
        assertTrue(chicha.podeJogarNaRota(Rota.FARM_LANE));''',
    '''        assertEquals(Rota.JUNGLE, chicha.getRota());
        assertTrue(chicha.podeJogarNaRota(Rota.CLASH_LANE));
        assertTrue(chicha.podeJogarNaRota(Rota.JUNGLE));
        assertFalse(chicha.podeJogarNaRota(Rota.FARM_LANE));''',
    "Rotas da Chicha",
)
hero_test = replace_once(
    hero_test,
    "        assertEquals(9L, quantidadePorTier.get(TierMeta.A));",
    "        assertEquals(8L, quantidadePorTier.get(TierMeta.A));",
    "Quantidade de tier A",
)
hero_test = replace_once(
    hero_test,
    '                "S15-HOK-PLUS-2.0",',
    '                "S15-HOK-PLUS-2.1",',
    "Versão esperada do meta",
)
hero_test = replace_once(
    hero_test,
    "                LocalDate.of(2026, 7, 2),",
    "                LocalDate.of(2026, 7, 10),",
    "Data esperada do meta",
)
hero_test_path.write_text(hero_test, encoding="utf-8")

infer_test_path = Path("src/test/java/br/com/carlosdaniel/hokdraftcoach/service/InferenciaFuncoesServiceTest.java")
infer_test = infer_test_path.read_text(encoding="utf-8")
infer_test = replace_once(
    infer_test,
    "import static org.junit.jupiter.api.Assertions.assertThrows;\n",
    "import static org.junit.jupiter.api.Assertions.assertThrows;\nimport static org.junit.jupiter.api.Assertions.assertSame;\n",
    "Import assertSame",
)
infer_test = replace_once(
    infer_test,
    '''    @Test
    void deveRejeitarMesmoHeroiNosDoisLados() {''',
    '''    @Test
    void deveReutilizarInferenciaQuandoODraftNaoMudou() {
        InferenciaFuncoesRequest request = request(
            List.of(20L, 14L),
            List.of(17L)
        );

        InferenciaFuncoesResponse primeira = service.inferir(request);
        InferenciaFuncoesResponse segunda = service.inferir(request);

        assertSame(primeira, segunda);
    }

    @Test
    void deveRejeitarMesmoHeroiNosDoisLados() {''',
    "Teste do cache de inferência",
)
infer_test_path.write_text(infer_test, encoding="utf-8")

role_test_path = Path("src/test/java/br/com/carlosdaniel/hokdraftcoach/service/RecomendacaoProximoPickPorFuncaoServiceTest.java")
role_test = role_test_path.read_text(encoding="utf-8")
role_test = replace_once(
    role_test,
    "import static org.junit.jupiter.api.Assertions.assertNotNull;\n",
    "import static org.junit.jupiter.api.Assertions.assertNotNull;\nimport static org.junit.jupiter.api.Assertions.assertNotEquals;\n",
    "Import assertNotEquals",
)
role_test = replace_once(
    role_test,
    '''        assertNotNull(resposta.recomendacaoPrincipal());

        Stream<RecomendacaoPickResponse> recomendacoes = Stream.concat(''',
    '''        assertNotNull(resposta.recomendacaoPrincipal());
        assertNotEquals("Chicha", resposta.recomendacaoPrincipal().heroi());
        assertTrue(resposta.alternativas().stream().noneMatch(
            recomendacao -> recomendacao.heroi().equals("Chicha")
        ));

        Stream<RecomendacaoPickResponse> recomendacoes = Stream.concat(''',
    "Regressão da Chicha na Farm Lane",
)
role_test_path.write_text(role_test, encoding="utf-8")
