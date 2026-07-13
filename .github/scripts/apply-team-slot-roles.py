from pathlib import Path
import re


ROOT = Path(".")


def read(path: str) -> str:
    return Path(path).read_text(encoding="utf-8")


def write(path: str, content: str) -> None:
    target = Path(path)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(content, encoding="utf-8")


def replace_once(path: str, old: str, new: str, label: str) -> None:
    text = read(path)
    if new in text:
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: esperado 1 bloco em {path}, encontrado {count}")
    write(path, text.replace(old, new, 1))


def replace_regex(path: str, pattern: str, replacement: str, label: str) -> None:
    text = read(path)
    updated, count = re.subn(pattern, replacement, text, count=1, flags=re.S)
    if count == 0 and replacement in text:
        return
    if count != 1:
        raise SystemExit(f"{label}: esperado 1 bloco em {path}, encontrado {count}")
    write(path, updated)


def append_once(path: str, marker: str, content: str) -> None:
    text = read(path)
    if marker in text:
        return
    write(path, text.rstrip() + "\n\n" + content.strip() + "\n")


# ---------------------------------------------------------------------------
# DTOs: funções explícitas por slot, mantendo construtores antigos compatíveis.
# ---------------------------------------------------------------------------
write(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/dto/FuncaoSlotRequest.java",
    '''package br.com.carlosdaniel.hokdraftcoach.dto;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FuncaoSlotRequest(
    @NotNull @Min(1) @Max(5) Integer ordem,
    @NotNull Rota funcao
) {
}
'''
)

write(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/dto/RecomendacaoProximoPickRequest.java",
    '''package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecomendacaoProximoPickRequest(
    LadoDraft meuLado,
    @Min(1) @Max(5) Integer minhaOrdem,
    @NotNull @Size(max = 3) List<Long> bansAzul,
    @NotNull @Size(max = 3) List<Long> bansVermelho,
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksAzul,
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksVermelho,
    @NotNull @Size(max = 5) List<Rota> funcoesPreferidas,
    @Valid @NotNull @Size(max = 5) List<FuncaoSlotRequest> funcoesAliadas
) {

    public RecomendacaoProximoPickRequest {
        funcoesPreferidas = funcoesPreferidas == null
            ? List.of()
            : List.copyOf(funcoesPreferidas);
        funcoesAliadas = funcoesAliadas == null
            ? List.of()
            : List.copyOf(funcoesAliadas);
    }

    public RecomendacaoProximoPickRequest(
        LadoDraft meuLado,
        Integer minhaOrdem,
        List<Long> bansAzul,
        List<Long> bansVermelho,
        List<PickSemFuncaoRequest> picksAzul,
        List<PickSemFuncaoRequest> picksVermelho,
        List<Rota> funcoesPreferidas
    ) {
        this(
            meuLado,
            minhaOrdem,
            bansAzul,
            bansVermelho,
            picksAzul,
            picksVermelho,
            funcoesPreferidas,
            List.of()
        );
    }

    public List<Rota> funcoesDaOrdem(Integer ordem) {
        if (ordem == null) {
            return List.of();
        }
        if (ordem.equals(minhaOrdem) && !funcoesPreferidas.isEmpty()) {
            return funcoesPreferidas;
        }
        return funcoesAliadas.stream()
            .filter(item -> ordem.equals(item.ordem()))
            .map(FuncaoSlotRequest::funcao)
            .distinct()
            .toList();
    }

    public List<FuncaoSlotRequest> funcoesDaEquipe() {
        Map<Integer, Rota> porOrdem = new TreeMap<>();
        funcoesAliadas.stream()
            .filter(item -> item != null && item.ordem() != null && item.funcao() != null)
            .forEach(item -> porOrdem.put(item.ordem(), item.funcao()));
        if (
            minhaOrdem != null
                && funcoesPreferidas.size() == 1
        ) {
            porOrdem.put(minhaOrdem, funcoesPreferidas.getFirst());
        }
        return porOrdem.entrySet().stream()
            .map(item -> new FuncaoSlotRequest(item.getKey(), item.getValue()))
            .toList();
    }
}
'''
)

write(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/dto/InferenciaFuncoesRequest.java",
    '''package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InferenciaFuncoesRequest(
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksAzul,
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksVermelho,
    @Valid @NotNull @Size(max = 5) List<FuncaoSlotRequest> funcoesAzul,
    @Valid @NotNull @Size(max = 5) List<FuncaoSlotRequest> funcoesVermelho
) {

    public InferenciaFuncoesRequest {
        funcoesAzul = funcoesAzul == null ? List.of() : List.copyOf(funcoesAzul);
        funcoesVermelho = funcoesVermelho == null
            ? List.of()
            : List.copyOf(funcoesVermelho);
    }

    public InferenciaFuncoesRequest(
        List<PickSemFuncaoRequest> picksAzul,
        List<PickSemFuncaoRequest> picksVermelho
    ) {
        this(picksAzul, picksVermelho, List.of(), List.of());
    }
}
'''
)

# ---------------------------------------------------------------------------
# Restrição obrigatória da recomendação pela função do próximo slot aliado.
# ---------------------------------------------------------------------------
write(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/service/RecomendacaoProximoPickPorFuncaoService.java",
    '''package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.FuncaoSlotRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.PickSemFuncaoRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoProximoPickResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
@Primary
public class RecomendacaoProximoPickPorFuncaoService
    extends RecomendacaoProximoPickService {

    public RecomendacaoProximoPickPorFuncaoService(
        HeroiServicePorFuncao heroiService,
        InferenciaFuncoesService inferenciaFuncoesService
    ) {
        super(heroiService, inferenciaFuncoesService);
    }

    @Override
    public RecomendacaoProximoPickResponse recomendar(
        RecomendacaoProximoPickRequest request
    ) {
        validarFuncoesInformadas(request);
        Integer ordemAlvo = proximaOrdemAliada(request);
        List<Rota> funcoesAlvo = request.funcoesDaOrdem(ordemAlvo);
        if (funcoesAlvo.isEmpty()) {
            return super.recomendar(request);
        }

        try (
            HeroiServicePorFuncao.EscopoFuncao ignored =
                HeroiServicePorFuncao.restringirA(funcoesAlvo)
        ) {
            return super.recomendar(request);
        }
    }

    private void validarFuncoesInformadas(
        RecomendacaoProximoPickRequest request
    ) {
        Set<Integer> ordens = new HashSet<>();
        Set<Rota> funcoes = new HashSet<>();

        for (FuncaoSlotRequest item : request.funcoesAliadas()) {
            if (item == null || item.ordem() == null || item.funcao() == null) {
                throw new RegraNegocioException(
                    "Toda função aliada deve informar ordem e função."
                );
            }
            if (!ordens.add(item.ordem())) {
                throw new RegraNegocioException(
                    "A função do jogador " + item.ordem()
                        + " foi informada mais de uma vez."
                );
            }
            if (
                request.minhaOrdem() != null
                    && request.minhaOrdem().equals(item.ordem())
            ) {
                throw new RegraNegocioException(
                    "A sua função deve ser informada no campo Minha função."
                );
            }
            if (!funcoes.add(item.funcao())) {
                throw new RegraNegocioException(
                    "A função " + item.funcao()
                        + " foi atribuída a mais de um jogador aliado."
                );
            }
        }

        if (request.funcoesPreferidas().size() == 1) {
            Rota minhaFuncao = request.funcoesPreferidas().getFirst();
            if (!funcoes.add(minhaFuncao)) {
                throw new RegraNegocioException(
                    "A função " + minhaFuncao
                        + " já foi atribuída a outro jogador aliado."
                );
            }
        }
    }

    private Integer proximaOrdemAliada(
        RecomendacaoProximoPickRequest request
    ) {
        if (request.meuLado() == null) {
            return null;
        }
        List<PickSemFuncaoRequest> aliados =
            request.meuLado() == LadoDraft.AZUL
                ? request.picksAzul()
                : request.picksVermelho();

        for (int ordem = 1; ordem <= 5; ordem += 1) {
            int ordemAtual = ordem;
            boolean preenchido = aliados.stream()
                .anyMatch(pick -> pick.ordem().equals(ordemAtual));
            if (!preenchido) {
                return ordem;
            }
        }
        return null;
    }
}
'''
)

# Motor base: passa funções fixas à inferência, usa a função do slot alvo e
# restringe também as rotas avaliadas de heróis flex.
service_path = "src/main/java/br/com/carlosdaniel/hokdraftcoach/service/RecomendacaoProximoPickService.java"
replace_once(
    service_path,
    '''            new InferenciaFuncoesRequest(
                request.picksAzul(),
                request.picksVermelho()
            )
''',
    '''            new InferenciaFuncoesRequest(
                request.picksAzul(),
                request.picksVermelho(),
                request.meuLado() == LadoDraft.AZUL
                    ? request.funcoesDaEquipe()
                    : List.of(),
                request.meuLado() == LadoDraft.VERMELHO
                    ? request.funcoesDaEquipe()
                    : List.of()
            )
''',
    "Funções fixas na inferência do motor"
)
replace_once(
    service_path,
    '''        List<Rota> funcoesAlvo = recomendacaoParaUsuario
            ? request.funcoesPreferidas()
            : List.of();
''',
    '''        List<Rota> funcoesAlvo = request.funcoesDaOrdem(
            ordemAlvoAliada
        );
''',
    "Função do slot alvo"
)
replace_once(
    service_path,
    '''            List<Rota> rotasValidas = candidato.getRotasPossiveis()
                .stream()
                .filter(hipoteseAliada.rotasAbertas()::contains)
                .toList();
''',
    '''            List<Rota> rotasValidas = candidato.getRotasPossiveis()
                .stream()
                .filter(hipoteseAliada.rotasAbertas()::contains)
                .filter(rota ->
                    funcoesPreferidas.isEmpty()
                        || funcoesPreferidas.contains(rota)
                )
                .toList();
''',
    "Restrição rígida das rotas avaliadas"
)

# Serviço detalhado: inclui as funções no cache e adapta o contexto ao slot.
dna_path = "src/main/java/br/com/carlosdaniel/hokdraftcoach/service/RecomendacaoProximoPickDnaService.java"
replace_once(
    dna_path,
    '''            List.copyOf(request.picksVermelho()),
            List.copyOf(request.funcoesPreferidas())
        );
''',
    '''            List.copyOf(request.picksVermelho()),
            List.copyOf(request.funcoesPreferidas()),
            List.copyOf(request.funcoesAliadas())
        );
''',
    "Funções aliadas na chave do cache"
)
replace_once(
    dna_path,
    '''        boolean alvoEhUsuario = ordemAlvo.equals(request.minhaOrdem());
        return new RecomendacaoProximoPickRequest(
            request.meuLado(),
            ordemAlvo,
            request.bansAzul(),
            request.bansVermelho(),
            request.picksAzul(),
            request.picksVermelho(),
            alvoEhUsuario ? request.funcoesPreferidas() : List.of()
        );
''',
    '''        return new RecomendacaoProximoPickRequest(
            request.meuLado(),
            ordemAlvo,
            request.bansAzul(),
            request.bansVermelho(),
            request.picksAzul(),
            request.picksVermelho(),
            request.funcoesDaOrdem(ordemAlvo),
            request.funcoesAliadas().stream()
                .filter(item -> !ordemAlvo.equals(item.ordem()))
                .toList()
        );
''',
    "Função explícita no contexto do próximo aliado"
)

# ---------------------------------------------------------------------------
# Inferência: uma função selecionada fixa a rota do herói daquele slot.
# ---------------------------------------------------------------------------
inferencia_path = "src/main/java/br/com/carlosdaniel/hokdraftcoach/service/InferenciaFuncoesService.java"
replace_once(
    inferencia_path,
    "import br.com.carlosdaniel.hokdraftcoach.dto.AtribuicaoFuncaoResponse;\n",
    "import br.com.carlosdaniel.hokdraftcoach.dto.AtribuicaoFuncaoResponse;\n"
    "import br.com.carlosdaniel.hokdraftcoach.dto.FuncaoSlotRequest;\n",
    "Import da função por slot"
)
replace_once(
    inferencia_path,
    '    private static final String VERSAO_MOTOR = "FUNCOES-0.1";\n',
    '    private static final String VERSAO_MOTOR = "FUNCOES-0.2";\n',
    "Versão da inferência"
)
replace_once(
    inferencia_path,
    '''        ChaveInferencia chave = new ChaveInferencia(
            List.copyOf(request.picksAzul()),
            List.copyOf(request.picksVermelho())
        );
''',
    '''        ChaveInferencia chave = new ChaveInferencia(
            List.copyOf(request.picksAzul()),
            List.copyOf(request.picksVermelho()),
            List.copyOf(request.funcoesAzul()),
            List.copyOf(request.funcoesVermelho())
        );
''',
    "Funções fixas na chave da inferência"
)
replace_once(
    inferencia_path,
    '''        InferenciaEquipeResponse equipeAzul = inferirEquipe(
            "AZUL",
            picksAzul
        );
        InferenciaEquipeResponse equipeVermelha = inferirEquipe(
            "VERMELHO",
            picksVermelho
        );
''',
    '''        InferenciaEquipeResponse equipeAzul = inferirEquipe(
            "AZUL",
            picksAzul,
            request.funcoesAzul()
        );
        InferenciaEquipeResponse equipeVermelha = inferirEquipe(
            "VERMELHO",
            picksVermelho,
            request.funcoesVermelho()
        );
''',
    "Funções fixas por equipe"
)
replace_once(
    inferencia_path,
    '''    private InferenciaEquipeResponse inferirEquipe(
        String lado,
        List<PickInterno> picks
    ) {
        List<HipoteseInterna> hipoteses = new ArrayList<>();

        gerarHipoteses(
            picks,
            0,
            EnumSet.noneOf(Rota.class),
            new ArrayList<>(),
            0,
            hipoteses
        );
''',
    '''    private InferenciaEquipeResponse inferirEquipe(
        String lado,
        List<PickInterno> picks,
        List<FuncaoSlotRequest> funcoesInformadas
    ) {
        List<HipoteseInterna> hipoteses = new ArrayList<>();
        Map<Integer, Rota> funcoesFixas = mapearFuncoesFixas(
            funcoesInformadas
        );

        gerarHipoteses(
            picks,
            0,
            EnumSet.noneOf(Rota.class),
            new ArrayList<>(),
            0,
            hipoteses,
            funcoesFixas
        );
''',
    "Mapa de funções fixas"
)
replace_once(
    inferencia_path,
    '''    private void gerarHipoteses(
        List<PickInterno> picks,
        int indice,
        Set<Rota> rotasOcupadas,
        List<AtribuicaoInterna> atribuicoes,
        int pontuacao,
        List<HipoteseInterna> resultado
    ) {
''',
    '''    private void gerarHipoteses(
        List<PickInterno> picks,
        int indice,
        Set<Rota> rotasOcupadas,
        List<AtribuicaoInterna> atribuicoes,
        int pontuacao,
        List<HipoteseInterna> resultado,
        Map<Integer, Rota> funcoesFixas
    ) {
''',
    "Assinatura da geração de hipóteses"
)
replace_once(
    inferencia_path,
    '''        PickInterno pick = picks.get(indice);
        List<Rota> rotasOrdenadas = ordenarRotas(pick.heroi());
''',
    '''        PickInterno pick = picks.get(indice);
        List<Rota> rotasOrdenadas = ordenarRotas(
            pick.heroi(),
            funcoesFixas.get(pick.ordem())
        );
''',
    "Rota fixa do pick"
)
replace_once(
    inferencia_path,
    '''                pontuacao + bonus,
                resultado
            );
''',
    '''                pontuacao + bonus,
                resultado,
                funcoesFixas
            );
''',
    "Recursão com funções fixas"
)
replace_once(
    inferencia_path,
    '''    private List<Rota> ordenarRotas(Heroi heroi) {
        return heroi.getRotasPossiveis()
            .stream()
            .sorted(
                Comparator.comparingInt(
                    rota -> rota == heroi.getRota() ? 0 : 1
                )
            )
            .toList();
    }
''',
    '''    private List<Rota> ordenarRotas(
        Heroi heroi,
        Rota funcaoFixa
    ) {
        if (funcaoFixa != null) {
            return heroi.podeJogarNaRota(funcaoFixa)
                ? List.of(funcaoFixa)
                : List.of();
        }
        return heroi.getRotasPossiveis()
            .stream()
            .sorted(
                Comparator.comparingInt(
                    rota -> rota == heroi.getRota() ? 0 : 1
                )
            )
            .toList();
    }

    private Map<Integer, Rota> mapearFuncoesFixas(
        List<FuncaoSlotRequest> funcoesInformadas
    ) {
        Map<Integer, Rota> porOrdem = new LinkedHashMap<>();
        Set<Rota> funcoesUsadas = new HashSet<>();
        for (FuncaoSlotRequest item : funcoesInformadas) {
            if (item == null || item.ordem() == null || item.funcao() == null) {
                throw new RegraNegocioException(
                    "Toda função fixa deve informar ordem e função."
                );
            }
            if (porOrdem.putIfAbsent(item.ordem(), item.funcao()) != null) {
                throw new RegraNegocioException(
                    "A ordem " + item.ordem()
                        + " possui mais de uma função fixa."
                );
            }
            if (!funcoesUsadas.add(item.funcao())) {
                throw new RegraNegocioException(
                    "A função " + item.funcao()
                        + " foi atribuída a mais de um slot."
                );
            }
        }
        return Map.copyOf(porOrdem);
    }
''',
    "Ordenação com função fixa"
)
replace_once(
    inferencia_path,
    '''                new PickInterno(
                    prefixo + request.ordem(),
                    heroi
                )
''',
    '''                new PickInterno(
                    request.ordem(),
                    prefixo + request.ordem(),
                    heroi
                )
''',
    "Ordem no pick interno"
)
replace_once(
    inferencia_path,
    '''    private record ChaveInferencia(
        List<PickSemFuncaoRequest> picksAzul,
        List<PickSemFuncaoRequest> picksVermelho
    ) {
    }

    private record PickInterno(
        String slot,
        Heroi heroi
    ) {
''',
    '''    private record ChaveInferencia(
        List<PickSemFuncaoRequest> picksAzul,
        List<PickSemFuncaoRequest> picksVermelho,
        List<FuncaoSlotRequest> funcoesAzul,
        List<FuncaoSlotRequest> funcoesVermelho
    ) {
    }

    private record PickInterno(
        Integer ordem,
        String slot,
        Heroi heroi
    ) {
''',
    "Registros internos da inferência"
)

# ---------------------------------------------------------------------------
# Catálogo: nome canônico Agu e Chicha somente Jungle/Top.
# ---------------------------------------------------------------------------
for root in [Path("src/main/java"), Path("src/test/java"), Path("docs")]:
    if not root.exists():
        continue
    for path in root.rglob("*"):
        if path.suffix not in {".java", ".md"}:
            continue
        text = path.read_text(encoding="utf-8")
        updated = text.replace("Agudo", "Agu")
        if updated != text:
            path.write_text(updated, encoding="utf-8")

# Preserva Agudo como alias aceito.
replace_once(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/service/HeroiService.java",
    '''            "Agu",
            List.of("Agu"),
            ClasseHeroi.ATIRADOR,
''',
    '''            "Agu",
            List.of("Agudo"),
            ClasseHeroi.ATIRADOR,
''',
    "Alias Agudo no catálogo base"
)
replace_once(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/repository/CatalogoJungleRepository.java",
    'j(22L, "Agu", List.of("Agu", "Aguu"), ClasseHeroi.ATIRADOR,',
    'j(22L, "Agu", List.of("Agudo", "Aguu"), ClasseHeroi.ATIRADOR,',
    "Alias Agudo no catálogo jungle"
)
replace_once(
    "src/test/java/br/com/carlosdaniel/hokdraftcoach/service/HeroiServiceTest.java",
    'assertEquals("Agu", buscar("Agu").getNome());',
    'assertEquals("Agu", buscar("Agudo").getNome());',
    "Teste do alias Agudo"
)

replace_once(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/repository/CatalogoJungleRepository.java",
    '''        j(25L, "Chicha", List.of(), ClasseHeroi.LUTADOR,
            List.of(Rota.CLASH_LANE, Rota.JUNGLE, Rota.FARM_LANE),
            "Lutadora de velocidade de ataque e limpeza após o primeiro engage",
''',
    '''        j(25L, "Chicha", List.of(), ClasseHeroi.LUTADOR,
            List.of(Rota.JUNGLE, Rota.CLASH_LANE),
            "Lutadora de selva e Top Lane com velocidade de ataque e limpeza",
''',
    "Rotas da Chicha no catálogo jungle"
)
replace_once(
    "src/main/java/br/com/carlosdaniel/hokdraftcoach/repository/CatalogoClashRepository.java",
    '''        t(212L, "Chicha", List.of(), ClasseHeroi.LUTADOR,
            List.of(Rota.CLASH_LANE, Rota.JUNGLE, Rota.FARM_LANE),
            "Lutadora de velocidade de ataque e limpeza após o primeiro engage",
''',
    '''        t(212L, "Chicha", List.of(), ClasseHeroi.LUTADOR,
            List.of(Rota.CLASH_LANE, Rota.JUNGLE),
            "Lutadora de Top Lane e selva com velocidade de ataque e limpeza",
''',
    "Rotas da Chicha no catálogo clash"
)
replace_once(
    "src/test/java/br/com/carlosdaniel/hokdraftcoach/service/ConhecimentoJungleServiceTest.java",
    '        assertTrue(buscar("Chicha").podeJogarNaRota(Rota.FARM_LANE));\n',
    '        assertFalse(buscar("Chicha").podeJogarNaRota(Rota.FARM_LANE));\n',
    "Chicha fora de ADC no teste jungle"
)
replace_once(
    "src/test/java/br/com/carlosdaniel/hokdraftcoach/service/ConhecimentoClashServiceTest.java",
    '        assertTrue(buscar("Chicha").podeJogarNaRota(Rota.FARM_LANE));\n',
    '        assertFalse(buscar("Chicha").podeJogarNaRota(Rota.FARM_LANE));\n',
    "Chicha fora de ADC no teste clash"
)
replace_once(
    "src/test/java/br/com/carlosdaniel/hokdraftcoach/service/ConhecimentoJungleServiceTest.java",
    '        assertEquals("Agu", buscar("Aguu").getNome());\n',
    '        assertEquals("Agu", buscar("Aguu").getNome());\n'
    '        assertEquals("Agu", buscar("Agudo").getNome());\n',
    "Alias legado Agu"
)

# ---------------------------------------------------------------------------
# Interface: rótulos novos e módulo de funções dos quatro aliados.
# ---------------------------------------------------------------------------
index_path = "src/main/resources/static/index.html"
index = read(index_path)
index = index.replace(">Clash Lane<", ">Top Lane<")
index = index.replace(">Farm Lane<", ">ADC<")
index = index.replace(">Roaming<", ">Suporte<")
if '<script src="/js/team-roles.js" defer></script>' not in index:
    index = index.replace(
        '<script src="/js/next-pick.js" defer></script>',
        '<script src="/js/next-pick.js" defer></script>\n'
        '    <script src="/js/team-roles.js" defer></script>'
    )
write(index_path, index)

write(
    "src/main/resources/static/js/team-roles.js",
    '''const FUNCOES_SLOTS_ALIADOS = Object.freeze([
    Object.freeze({ codigo: "CLASH_LANE", nome: "Top Lane" }),
    Object.freeze({ codigo: "JUNGLE", nome: "Jungle" }),
    Object.freeze({ codigo: "MID_LANE", nome: "Mid Lane" }),
    Object.freeze({ codigo: "FARM_LANE", nome: "ADC" }),
    Object.freeze({ codigo: "ROAMING", nome: "Suporte" })
]);

Object.assign(NOMES_ROTAS, {
    CLASH_LANE: "Top Lane",
    JUNGLE: "Jungle",
    MID_LANE: "Mid Lane",
    FARM_LANE: "ADC",
    ROAMING: "Suporte"
});

estado.funcoesAliadas = estado.funcoesAliadas ?? {
    AZUL: Array(5).fill(""),
    VERMELHO: Array(5).fill("")
};

const preferenciaFuncaoEquipe = document.querySelector("#preferencia-funcao");
const criarSlotsPickBaseFuncoes = criarSlotsPick;
const registrarEventosSlotsBaseFuncoes = registrarEventosSlots;
const abrirModalBaseFuncoes = abrirModal;
const selecionarHeroiBaseFuncoes = selecionarHeroi;
const montarRequestProximoPickBaseFuncoes = montarRequestProximoPick;
const formatarEnumBaseFuncoes = formatarEnum;

criarSlotsPick = function criarSlotsPickComFuncoes(lado, leitura) {
    const meuLado = meuLadoSelect.value;
    const minhaOrdem = Number(minhaOrdemSelect.value);

    return estado.picks[lado]
        .map((heroi, indice) => {
            const preenchido = Boolean(heroi);
            const ativo = slotPertenceRodadaAtual(
                lado,
                indice,
                leitura
            );
            const meuSlot =
                meuLado === lado && minhaOrdem === indice + 1;
            const rodadaSlot = buscarRodadaDoSlot(lado, indice);
            const bloqueado =
                leitura.fase === "BANS"
                || (
                    leitura.fase === "PICKS"
                    && rodadaSlot > leitura.indiceRodadaAtual
                    && !preenchido
                );
            const exibirFuncaoAliada =
                meuLado === lado
                && Boolean(minhaOrdem)
                && minhaOrdem !== indice + 1;
            const funcaoSelecionada =
                estado.funcoesAliadas[lado]?.[indice] ?? "";

            return `
                <div class="slot-pick-wrapper ${exibirFuncaoAliada ? "slot-pick-wrapper--com-funcao" : ""}">
                    <button
                        class="
                            slot-pick
                            ${preenchido ? "slot-pick--preenchido" : ""}
                            ${ativo ? "slot-pick--ativo" : ""}
                            ${meuSlot ? "slot-pick--meu" : ""}
                            ${bloqueado ? "slot-pick--bloqueado" : ""}
                        "
                        type="button"
                        data-tipo="PICK"
                        data-lado="${lado}"
                        data-indice="${indice}"
                    >
                        <span class="slot-pick__numero">
                            ${LADOS[lado].prefixo}${indice + 1}
                        </span>

                        <span class="slot-pick__conteudo">
                            <span class="slot-pick__ordem">
                                Jogador ${indice + 1}
                                · Rodada ${rodadaSlot + 1}
                            </span>

                            <span class="slot-pick__nome">
                                ${
                                    preenchido
                                        ? escaparHtml(heroi.nome)
                                        : ativo
                                            ? "Escolha atual"
                                            : "Aguardando pick"
                                }
                            </span>

                            ${preenchido ? criarChipsRotas(heroi) : ""}
                        </span>

                        <span class="slot-pick__acao">
                            ${preenchido ? "↻" : "+"}
                        </span>
                    </button>
                    ${
                        exibirFuncaoAliada
                            ? criarSeletorFuncaoAliada(
                                lado,
                                indice,
                                funcaoSelecionada
                            )
                            : ""
                    }
                </div>
            `;
        })
        .join("");
};

registrarEventosSlots = function registrarEventosSlotsComFuncoes() {
    registrarEventosSlotsBaseFuncoes();
    document
        .querySelectorAll("[data-funcao-aliada]")
        .forEach((select) => {
            select.addEventListener("change", () => {
                atualizarFuncaoAliada(
                    select.dataset.lado,
                    Number(select.dataset.indice),
                    select.value
                );
            });
        });
};

abrirModal = function abrirModalComFuncaoFixa(tipo, lado, indice) {
    abrirModalBaseFuncoes(tipo, lado, indice);
    const funcao = tipo === "PICK"
        ? obterFuncaoDefinidaSlot(lado, indice)
        : "";

    filtroRota.disabled = Boolean(funcao);
    filtroRota.value = funcao;
    if (funcao) {
        modalDescricao.textContent =
            `Função definida: ${NOMES_ROTAS[funcao]}. `
            + "Somente heróis compatíveis com essa função são exibidos.";
    }
    renderizarHeroisModal();
};

selecionarHeroi = function selecionarHeroiComFuncaoFixa(heroiId) {
    if (estado.slotAtual?.tipo === "PICK") {
        const funcao = obterFuncaoDefinidaSlot(
            estado.slotAtual.lado,
            estado.slotAtual.indice
        );
        const heroi = estado.herois.find(
            (item) => Number(item.id) === Number(heroiId)
        );
        if (
            funcao
                && heroi
                && !obterRotasHeroi(heroi).includes(funcao)
        ) {
            return;
        }
    }
    selecionarHeroiBaseFuncoes(heroiId);
};

montarRequestProximoPick = function montarRequestComFuncoesAliadas() {
    const request = montarRequestProximoPickBaseFuncoes();
    request.funcoesAliadas = montarFuncoesTimeSelecionado(false);
    return request;
};

nomeRotaProximoPick = function nomeRotaAtualizado(rota) {
    return NOMES_ROTAS[rota] ?? formatarEnumBaseFuncoes(rota);
};

formatarEnum = function formatarEnumComRotas(valor) {
    return NOMES_ROTAS[valor] ?? formatarEnumBaseFuncoes(valor);
};

atualizarInferenciaFuncoes = async function atualizarInferenciaComFuncoes() {
    const versaoAtual = ++estado.versaoRequisicaoInferencia;

    estado.inferenciaCarregando = true;
    estado.inferenciaErro = "";
    renderizarHipotesesFuncao();

    const ladoSelecionado = meuLadoSelect.value;
    const funcoesSelecionadas = montarFuncoesTimeSelecionado(true);
    const corpo = {
        picksAzul: montarPicksInferencia("AZUL"),
        picksVermelho: montarPicksInferencia("VERMELHO"),
        funcoesAzul: ladoSelecionado === "AZUL"
            ? funcoesSelecionadas
            : [],
        funcoesVermelho: ladoSelecionado === "VERMELHO"
            ? funcoesSelecionadas
            : []
    };

    try {
        const resposta = await fetch("/api/draft/inferir-funcoes", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(corpo)
        });

        if (!resposta.ok) {
            throw new Error(await extrairMensagemErro(resposta));
        }

        const inferencia = await resposta.json();
        if (versaoAtual !== estado.versaoRequisicaoInferencia) {
            return;
        }
        estado.inferencia = inferencia;
    } catch (erro) {
        if (versaoAtual !== estado.versaoRequisicaoInferencia) {
            return;
        }
        estado.inferencia = null;
        estado.inferenciaErro = erro.message;
    } finally {
        if (versaoAtual === estado.versaoRequisicaoInferencia) {
            estado.inferenciaCarregando = false;
            renderizarHipotesesFuncao();
        }
    }
};

function criarSeletorFuncaoAliada(lado, indice, selecionada) {
    const usadas = funcoesUsadasNoTime(lado, indice);
    const opcoes = FUNCOES_SLOTS_ALIADOS.map((funcao) => {
        const selecionadaAgora = funcao.codigo === selecionada;
        const indisponivel = usadas.has(funcao.codigo) && !selecionadaAgora;
        return `
            <option
                value="${funcao.codigo}"
                ${selecionadaAgora ? "selected" : ""}
                ${indisponivel ? "disabled" : ""}
            >${funcao.nome}</option>
        `;
    }).join("");

    return `
        <label class="slot-funcao-aliada">
            <span>FUNÇÃO DO JOGADOR ${indice + 1}</span>
            <select
                data-funcao-aliada
                data-lado="${lado}"
                data-indice="${indice}"
                aria-label="Função do jogador ${indice + 1}"
            >
                <option value="">Selecionar função</option>
                ${opcoes}
            </select>
        </label>
    `;
}

function funcoesUsadasNoTime(lado, indiceIgnorado) {
    const usadas = new Set();
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (
        meuLadoSelect.value === lado
            && minhaOrdem
            && minhaOrdem - 1 !== indiceIgnorado
            && preferenciaFuncaoEquipe.value
    ) {
        usadas.add(preferenciaFuncaoEquipe.value);
    }
    estado.funcoesAliadas[lado].forEach((funcao, indice) => {
        if (indice !== indiceIgnorado && funcao) {
            usadas.add(funcao);
        }
    });
    return usadas;
}

function atualizarFuncaoAliada(lado, indice, funcao) {
    if (!estado.funcoesAliadas[lado]) {
        return;
    }
    if (funcao && funcoesUsadasNoTime(lado, indice).has(funcao)) {
        renderizarTudo();
        return;
    }
    estado.funcoesAliadas[lado][indice] = funcao;
    renderizarTudo();
    atualizarInferenciaFuncoes();
}

function obterFuncaoDefinidaSlot(lado, indice) {
    if (meuLadoSelect.value !== lado) {
        return "";
    }
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (minhaOrdem === indice + 1) {
        return preferenciaFuncaoEquipe.value;
    }
    return estado.funcoesAliadas[lado]?.[indice] ?? "";
}

function montarFuncoesTimeSelecionado(incluirUsuario) {
    const lado = meuLadoSelect.value;
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (!LADOS[lado] || !minhaOrdem) {
        return [];
    }

    const funcoes = [];
    for (let indice = 0; indice < 5; indice += 1) {
        const ordem = indice + 1;
        if (ordem === minhaOrdem) {
            if (incluirUsuario && preferenciaFuncaoEquipe.value) {
                funcoes.push({
                    ordem,
                    funcao: preferenciaFuncaoEquipe.value
                });
            }
            continue;
        }
        const funcao = estado.funcoesAliadas[lado][indice];
        if (funcao) {
            funcoes.push({ ordem, funcao });
        }
    }
    return funcoes;
}

function sincronizarContextoFuncoes() {
    const lado = meuLadoSelect.value;
    const minhaOrdem = Number(minhaOrdemSelect.value);
    if (LADOS[lado] && minhaOrdem) {
        estado.funcoesAliadas[lado][minhaOrdem - 1] = "";
        const minhaFuncao = preferenciaFuncaoEquipe.value;
        if (minhaFuncao) {
            estado.funcoesAliadas[lado] = estado.funcoesAliadas[lado]
                .map((funcao, indice) =>
                    indice !== minhaOrdem - 1 && funcao === minhaFuncao
                        ? ""
                        : funcao
                );
        }
    }
    renderizarTudo();
    atualizarInferenciaFuncoes();
}

meuLadoSelect.addEventListener("change", sincronizarContextoFuncoes);
minhaOrdemSelect.addEventListener("change", sincronizarContextoFuncoes);
preferenciaFuncaoEquipe.addEventListener("change", sincronizarContextoFuncoes);

botaoLimpar.addEventListener("click", () => {
    estado.funcoesAliadas.AZUL = Array(5).fill("");
    estado.funcoesAliadas.VERMELHO = Array(5).fill("");
    preferenciaFuncaoEquipe.value = "";
    renderizarTudo();
    atualizarInferenciaFuncoes();
});
'''
)

append_once(
    "src/main/resources/static/css/game-draft.css",
    ".slot-funcao-aliada {",
    '''.slot-pick-wrapper {
    min-width: 0;
    display: grid;
    gap: 5px;
}

.slot-pick-wrapper .slot-pick {
    width: 100%;
}

.slot-funcao-aliada {
    display: grid;
    grid-template-columns: auto minmax(120px, 1fr);
    align-items: center;
    gap: 8px;
    padding: 6px 8px;
    border: 1px solid rgba(69, 105, 151, 0.34);
    border-radius: 7px;
    background: rgba(8, 19, 35, 0.82);
}

.slot-funcao-aliada > span {
    color: #71849e;
    font-size: 0.48rem;
    font-weight: 850;
    letter-spacing: 0.04em;
}

.slot-funcao-aliada select {
    min-width: 0;
    padding: 5px 24px 5px 7px;
    color: #eaf2ff;
    border: 1px solid #29415f;
    border-radius: 6px;
    outline: none;
    background: #101f34;
    font-size: 0.62rem;
    font-weight: 800;
    cursor: pointer;
}

.slot-funcao-aliada select:focus {
    border-color: #5aa9ff;
    box-shadow: 0 0 0 2px rgba(72, 157, 255, 0.13);
}

.slot-funcao-aliada option:disabled {
    color: #5d6878;
}

@media (max-width: 900px) {
    .slot-funcao-aliada {
        grid-template-columns: 1fr;
        gap: 4px;
    }
}
'''
)

# ---------------------------------------------------------------------------
# Testes de regressão.
# ---------------------------------------------------------------------------
funcao_test = "src/test/java/br/com/carlosdaniel/hokdraftcoach/service/RecomendacaoProximoPickPorFuncaoServiceTest.java"
replace_once(
    funcao_test,
    "import static org.junit.jupiter.api.Assertions.assertTrue;\n",
    "import static org.junit.jupiter.api.Assertions.assertTrue;\n"
    "import static org.junit.jupiter.api.Assertions.assertThrows;\n",
    "Import assertThrows"
)
replace_once(
    funcao_test,
    "import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;\n",
    "import br.com.carlosdaniel.hokdraftcoach.dto.FuncaoSlotRequest;\n"
    "import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoPickResponse;\n",
    "Import da função por slot no teste"
)
replace_once(
    funcao_test,
    "import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;\n",
    "import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;\n"
    "import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;\n",
    "Import da exceção no teste"
)
if "deveRestringirRecomendacaoAoSlotDoAliado" not in read(funcao_test):
    text = read(funcao_test)
    insertion = '''

    @Test
    void deveRestringirRecomendacaoAoSlotDoAliado() {
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                5,
                List.of(1L, 2L, 3L),
                List.of(4L, 5L, 6L),
                List.of(),
                List.of(),
                List.of(Rota.ROAMING),
                List.of(
                    new FuncaoSlotRequest(1, Rota.MID_LANE),
                    new FuncaoSlotRequest(2, Rota.JUNGLE),
                    new FuncaoSlotRequest(3, Rota.CLASH_LANE),
                    new FuncaoSlotRequest(4, Rota.FARM_LANE)
                )
            );

        RecomendacaoProximoPickResponse resposta = service.recomendar(request);

        assertEquals("VEZ_ALIADA", resposta.estadoDraft());
        assertNotNull(resposta.recomendacaoPrincipal());
        Stream<RecomendacaoPickResponse> recomendacoes = Stream.concat(
            Stream.of(resposta.recomendacaoPrincipal()),
            resposta.alternativas().stream()
        );
        assertTrue(recomendacoes.allMatch(recomendacao ->
            recomendacao.rotasRecomendadas().equals(List.of(Rota.MID_LANE))
        ));
        assertNotEquals("Chicha", resposta.recomendacaoPrincipal().heroi());
    }

    @Test
    void deveRejeitarFuncaoDuplicadaEntreAliados() {
        RecomendacaoProximoPickRequest request =
            new RecomendacaoProximoPickRequest(
                LadoDraft.AZUL,
                5,
                List.of(1L, 2L, 3L),
                List.of(4L, 5L, 6L),
                List.of(),
                List.of(),
                List.of(Rota.ROAMING),
                List.of(
                    new FuncaoSlotRequest(1, Rota.MID_LANE),
                    new FuncaoSlotRequest(2, Rota.MID_LANE)
                )
            );

        assertThrows(
            RegraNegocioException.class,
            () -> service.recomendar(request)
        );
    }
'''
    write(funcao_test, text.rsplit("\n}", 1)[0] + insertion + "\n}\n")

inferencia_test = "src/test/java/br/com/carlosdaniel/hokdraftcoach/service/InferenciaFuncoesServiceTest.java"
replace_once(
    inferencia_test,
    "import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaEquipeResponse;\n",
    "import br.com.carlosdaniel.hokdraftcoach.dto.FuncaoSlotRequest;\n"
    "import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaEquipeResponse;\n",
    "Import da função fixa no teste de inferência"
)
if "deveFixarFuncaoInformadaParaHeroiFlex" not in read(inferencia_test):
    text = read(inferencia_test)
    insertion = '''

    @Test
    void deveFixarFuncaoInformadaParaHeroiFlex() {
        InferenciaFuncoesRequest request = new InferenciaFuncoesRequest(
            List.of(new PickSemFuncaoRequest(1, 20L)),
            List.of(),
            List.of(new FuncaoSlotRequest(1, Rota.JUNGLE)),
            List.of()
        );

        InferenciaEquipeResponse azul = service.inferir(request).equipeAzul();

        assertEquals(1, azul.totalHipoteses());
        assertEquals(
            Rota.JUNGLE,
            azul.hipoteses().getFirst().atribuicoes().getFirst().rota()
        );
        assertTrue(azul.ambiguidades().getFirst().funcaoConfirmada());
    }

    @Test
    void deveInvalidarHeroiForaDaFuncaoInformada() {
        InferenciaFuncoesRequest request = new InferenciaFuncoesRequest(
            List.of(new PickSemFuncaoRequest(1, 20L)),
            List.of(),
            List.of(new FuncaoSlotRequest(1, Rota.FARM_LANE)),
            List.of()
        );

        InferenciaEquipeResponse azul = service.inferir(request).equipeAzul();

        assertFalse(azul.composicaoCompativel());
        assertTrue(azul.hipoteses().isEmpty());
    }
'''
    write(inferencia_test, text.rsplit("\n}", 1)[0] + insertion + "\n}\n")

print("Alterações de funções por slot aplicadas com sucesso.")
