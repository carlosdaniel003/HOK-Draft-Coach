from pathlib import Path
import re

ROOT = Path('.')


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')


def write(path: str, content: str) -> None:
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(content, encoding='utf-8')


def replace_once(path: str, old: str, new: str, label: str) -> None:
    content = read(path)
    if old not in content:
        raise RuntimeError(f'Bloco não encontrado: {label}')
    write(path, content.replace(old, new, 1))


def replace_regex_once(path: str, pattern: str, replacement: str, label: str) -> None:
    content = read(path)
    updated, count = re.subn(pattern, replacement, content, count=1, flags=re.DOTALL)
    if count != 1:
        raise RuntimeError(f'Bloco não encontrado ou ambíguo: {label} ({count})')
    write(path, updated)


utility_path = 'src/main/java/br/com/carlosdaniel/hokdraftcoach/service/PlanoRespostaAmeaca.java'
if (ROOT / utility_path).exists():
    print('Plano contextual já aplicado; nenhuma transformação necessária.')
    raise SystemExit(0)

write(
    utility_path,
    '''package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.dto.AlvoPrioritarioAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilAmeacaHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.model.PapelAmeaca;

final class PlanoRespostaAmeaca {

    private static final int MARGEM_TROCA_FOCO = 10;

    private PlanoRespostaAmeaca() {
    }

    static int prioridade(
        PapelAmeaca papel,
        int valorPapel,
        int vulnerabilidade,
        boolean conectado,
        boolean protegido
    ) {
        int calculada = switch (papel) {
            case AMEACA_PRINCIPAL -> valorPapel
                + Math.min(8, vulnerabilidade / 12)
                - (protegido ? 3 : 0);
            case INICIADOR -> (int) Math.round(valorPapel * 0.72)
                + (conectado ? 10 : 3)
                + Math.min(7, vulnerabilidade / 15);
            case HABILITADOR -> (int) Math.round(valorPapel * 0.68)
                + (conectado ? 9 : 2)
                + Math.min(8, vulnerabilidade / 12);
            case PROTETOR -> (int) Math.round(valorPapel * 0.64)
                + (conectado ? 7 : 1)
                + Math.min(8, vulnerabilidade / 12);
            case ELO_FRACO -> (int) Math.round(vulnerabilidade * 0.60);
        };
        return Math.max(0, Math.min(100, calculada));
    }

    static String criar(
        PerfilAmeacaHeroiResponse ameaca,
        List<AlvoPrioritarioAmeacaResponse> alvos
    ) {
        if (ameaca == null || alvos == null || alvos.isEmpty()) {
            return "Não foi possível estabelecer uma prioridade de resposta.";
        }

        AlvoPrioritarioAmeacaResponse alvoAmeaca = alvos.stream()
            .filter(alvo -> alvo.papel() == PapelAmeaca.AMEACA_PRINCIPAL)
            .findFirst()
            .orElseGet(() -> alvos.stream()
                .filter(alvo -> mesmoHeroi(alvo.heroi(), ameaca.heroi()))
                .findFirst()
                .orElse(alvos.getFirst()));
        AlvoPrioritarioAmeacaResponse primeiro = alvos.getFirst();
        AlvoPrioritarioAmeacaResponse foco = primeiro;

        if (
            primeiro.papel() != PapelAmeaca.AMEACA_PRINCIPAL
                && primeiro.prioridade() - alvoAmeaca.prioridade()
                    < MARGEM_TROCA_FOCO
        ) {
            foco = alvoAmeaca;
        }

        return switch (foco.papel()) {
            case AMEACA_PRINCIPAL -> planoContraAmeaca(
                ameaca,
                buscar(alvos, PapelAmeaca.INICIADOR)
            );
            case INICIADOR -> planoContraIniciador(foco, ameaca);
            case HABILITADOR -> planoContraHabilitador(foco, ameaca);
            case PROTETOR -> planoContraProtetor(foco, ameaca);
            case ELO_FRACO -> planoContraEloFraco(foco, ameaca);
        };
    }

    private static String planoContraAmeaca(
        PerfilAmeacaHeroiResponse ameaca,
        AlvoPrioritarioAmeacaResponse iniciador
    ) {
        String origemJanela = iniciador == null
            || mesmoHeroi(iniciador.heroi(), ameaca.heroi())
                ? "quando tentar ocupar uma posição segura"
                : "depois da entrada de " + iniciador.heroi();
        return "Janela a negar: não permita que " + ameaca.heroi()
            + " cause dano livre " + origemJanela + ". "
            + "Recurso a guardar: preserve controle duro, peel ou mobilidade para a primeira ativação do carregador, em vez de descarregar tudo na linha de frente. "
            + "Conversão: depois de forçar recuo ou habilidade defensiva de "
            + ameaca.heroi() + ", transforme a vantagem em objetivo.";
    }

    private static String planoContraIniciador(
        AlvoPrioritarioAmeacaResponse iniciador,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: absorva ou desengaje a primeira entrada de "
            + iniciador.heroi() + "; não é necessário eliminá-lo. "
            + "Recurso a guardar: mantenha peel, controle e reposicionamento para interromper o combo inicial. "
            + "Conversão: com as habilidades de entrada em recarga, avance sobre "
            + ameaca.heroi() + " ou force o objetivo antes da próxima iniciação.";
    }

    private static String planoContraHabilitador(
        AlvoPrioritarioAmeacaResponse habilitador,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: separe " + habilitador.heroi() + " de "
            + ameaca.heroi()
            + " e force sua utilidade principal antes do confronto decisivo. "
            + "Recurso a guardar: use controle ou zoneamento para negar cura, amplificação, conexão ou reset; não persiga o suporte se isso liberar o carregador. "
            + "Conversão: quando a utilidade estiver indisponível, troque o foco para "
            + ameaca.heroi() + ".";
    }

    private static String planoContraProtetor(
        AlvoPrioritarioAmeacaResponse protetor,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: force " + protetor.heroi()
            + " a gastar escudo, cura ou peel em um alvo secundário. "
            + "Recurso a guardar: preserve anti-cura, controle e uma segunda entrada; não gaste o combo inteiro no protetor. "
            + "Conversão: depois que a proteção sair, mude imediatamente para "
            + ameaca.heroi() + ".";
    }

    private static String planoContraEloFraco(
        AlvoPrioritarioAmeacaResponse eloFraco,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: puna " + eloFraco.heroi()
            + " apenas quando estiver isolado e sem cobertura. "
            + "Recurso a guardar: não comprometa ultimates ou mobilidade demais em uma isca, pois "
            + ameaca.heroi() + " continua sendo a condição principal. "
            + "Conversão: transforme a vantagem numérica em visão ou objetivo e reinicie a luta em posição favorável.";
    }

    private static AlvoPrioritarioAmeacaResponse buscar(
        List<AlvoPrioritarioAmeacaResponse> alvos,
        PapelAmeaca papel
    ) {
        return alvos.stream()
            .filter(alvo -> alvo.papel() == papel)
            .findFirst()
            .orElse(null);
    }

    private static boolean mesmoHeroi(String primeiro, String segundo) {
        return primeiro != null
            && segundo != null
            && primeiro.equalsIgnoreCase(segundo);
    }
}
'''
)

composition_path = 'src/main/java/br/com/carlosdaniel/hokdraftcoach/service/AnaliseAmeacaComposicaoService.java'
replace_once(
    composition_path,
    '            planoResposta(ameaca, alvos)\n',
    '            PlanoRespostaAmeaca.criar(ameaca, alvos)\n',
    'uso do plano contextual no refinamento'
)
replace_regex_once(
    composition_path,
    r'    private List<AlvoPrioritarioAmeacaResponse> reconstruirAlvos\(.*?\n    private AlvoPrioritarioAmeacaResponse alvo\(',
    '''    private List<AlvoPrioritarioAmeacaResponse> reconstruirAlvos(
        PerfilAmeacaHeroiResponse ameaca,
        PerfilAmeacaHeroiResponse protetor,
        PerfilAmeacaHeroiResponse iniciador,
        PerfilAmeacaHeroiResponse habilitador,
        PerfilAmeacaHeroiResponse eloFraco,
        List<SinergiaGrupoResponse> sinergias
    ) {
        List<AlvoPrioritarioAmeacaResponse> candidatos = new ArrayList<>();
        boolean conectaIniciador = iniciador != null && conectado(
            sinergias,
            ameaca.heroi(),
            iniciador.heroi()
        );
        boolean conectaHabilitador = habilitador != null && conectado(
            sinergias,
            ameaca.heroi(),
            habilitador.heroi()
        );
        boolean conectaProtetor = protetor != null && conectado(
            sinergias,
            ameaca.heroi(),
            protetor.heroi()
        );
        boolean ameacaProtegida = conectaIniciador
            || conectaHabilitador
            || conectaProtetor;

        candidatos.add(alvo(
            ameaca,
            PapelAmeaca.AMEACA_PRINCIPAL,
            PlanoRespostaAmeaca.prioridade(
                PapelAmeaca.AMEACA_PRINCIPAL,
                ameaca.potencialVitoria(),
                ameaca.vulnerabilidade(),
                false,
                ameacaProtegida
            )
        ));
        if (iniciador != null) {
            candidatos.add(alvo(
                iniciador,
                PapelAmeaca.INICIADOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.INICIADOR,
                    iniciador.iniciacao(),
                    iniciador.vulnerabilidade(),
                    conectaIniciador,
                    false
                )
            ));
        }
        if (habilitador != null) {
            candidatos.add(alvo(
                habilitador,
                PapelAmeaca.HABILITADOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.HABILITADOR,
                    habilitador.habilitacao(),
                    habilitador.vulnerabilidade(),
                    conectaHabilitador,
                    false
                )
            ));
        }
        if (protetor != null) {
            candidatos.add(alvo(
                protetor,
                PapelAmeaca.PROTETOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.PROTETOR,
                    protetor.protecao(),
                    protetor.vulnerabilidade(),
                    conectaProtetor,
                    false
                )
            ));
        }
        if (eloFraco != null) {
            candidatos.add(alvo(
                eloFraco,
                PapelAmeaca.ELO_FRACO,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.ELO_FRACO,
                    eloFraco.vulnerabilidade(),
                    eloFraco.vulnerabilidade(),
                    false,
                    false
                )
            ));
        }

        Map<String, AlvoPrioritarioAmeacaResponse> unicos =
            new LinkedHashMap<>();
        candidatos.forEach(candidato -> unicos.merge(
            normalizar(candidato.heroi()),
            candidato,
            (atual, novo) -> atual.prioridade() >= novo.prioridade()
                ? atual
                : novo
        ));
        return unicos.values().stream()
            .sorted(
                Comparator.comparingInt(
                    AlvoPrioritarioAmeacaResponse::prioridade
                ).reversed()
            )
            .limit(4)
            .toList();
    }

    private AlvoPrioritarioAmeacaResponse alvo(''',
    'prioridades contextuais no refinamento'
)
replace_regex_once(
    composition_path,
    r'    private String planoResposta\(.*?\n    private PerfilAmeacaHeroiResponse buscarPerfil\(',
    '    private PerfilAmeacaHeroiResponse buscarPerfil(',
    'remoção do plano fixo refinado'
)

base_path = 'src/main/java/br/com/carlosdaniel/hokdraftcoach/service/AnaliseAmeacaService.java'
replace_once(
    base_path,
    '''            planoResposta(
                maiorAmeacaResposta,
                alvos,
                dna
            )
''',
    '''            PlanoRespostaAmeaca.criar(
                maiorAmeacaResposta,
                alvos
            )
''',
    'uso do plano contextual base'
)
replace_regex_once(
    base_path,
    r'    private List<AlvoPrioritarioAmeacaResponse> montarAlvos\(.*?\n    private AlvoPrioritarioAmeacaResponse alvo\(',
    '''    private List<AlvoPrioritarioAmeacaResponse> montarAlvos(
        PerfilBruto ameaca,
        PerfilBruto protetor,
        PerfilBruto iniciador,
        PerfilBruto habilitador,
        PerfilBruto eloFraco,
        List<SinergiaGrupoResponse> sinergias
    ) {
        boolean conectaIniciador = conecta(sinergias, iniciador, ameaca);
        boolean conectaHabilitador = conecta(sinergias, habilitador, ameaca);
        boolean conectaProtetor = conecta(sinergias, protetor, ameaca);
        boolean ameacaProtegida = conectaIniciador
            || conectaHabilitador
            || conectaProtetor;

        List<AlvoPrioritarioAmeacaResponse> candidatos = List.of(
            alvo(
                ameaca,
                PapelAmeaca.AMEACA_PRINCIPAL,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.AMEACA_PRINCIPAL,
                    ameaca.potencialVitoria(),
                    ameaca.vulnerabilidade(),
                    false,
                    ameacaProtegida
                ),
                ameaca,
                sinergias
            ),
            alvo(
                iniciador,
                PapelAmeaca.INICIADOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.INICIADOR,
                    iniciador.iniciacao(),
                    iniciador.vulnerabilidade(),
                    conectaIniciador,
                    false
                ),
                ameaca,
                sinergias
            ),
            alvo(
                habilitador,
                PapelAmeaca.HABILITADOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.HABILITADOR,
                    habilitador.habilitacao(),
                    habilitador.vulnerabilidade(),
                    conectaHabilitador,
                    false
                ),
                ameaca,
                sinergias
            ),
            alvo(
                protetor,
                PapelAmeaca.PROTETOR,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.PROTETOR,
                    protetor.protecao(),
                    protetor.vulnerabilidade(),
                    conectaProtetor,
                    false
                ),
                ameaca,
                sinergias
            ),
            alvo(
                eloFraco,
                PapelAmeaca.ELO_FRACO,
                PlanoRespostaAmeaca.prioridade(
                    PapelAmeaca.ELO_FRACO,
                    eloFraco.vulnerabilidade(),
                    eloFraco.vulnerabilidade(),
                    false,
                    false
                ),
                ameaca,
                sinergias
            )
        );

        Map<String, AlvoPrioritarioAmeacaResponse> unicos =
            new LinkedHashMap<>();
        candidatos.forEach(candidato -> unicos.merge(
            normalizar(candidato.heroi()),
            candidato,
            (atual, novo) -> atual.prioridade() >= novo.prioridade()
                ? atual
                : novo
        ));

        return unicos.values().stream()
            .sorted(
                Comparator.comparingInt(
                    AlvoPrioritarioAmeacaResponse::prioridade
                ).reversed()
            )
            .limit(4)
            .toList();
    }

    private AlvoPrioritarioAmeacaResponse alvo(''',
    'prioridades contextuais no motor base'
)
replace_regex_once(
    base_path,
    r'    private String planoResposta\(.*?\n    private PerfilAmeacaHeroiResponse resposta\(',
    '    private PerfilAmeacaHeroiResponse resposta(',
    'remoção do plano fixo base'
)

existing_test = 'src/test/java/br/com/carlosdaniel/hokdraftcoach/service/AnaliseAmeacaBlindPickServiceTest.java'
replace_regex_once(
    existing_test,
    r'    @Test\n    void devePriorizarQuebrarAJanelaDeLianPoAntesDoDanoDeMarcoPolo\(\) \{.*?\n    \}\n\n    @Test',
    '''    @Test
    void deveResponderAoCarregadorSemMandarEliminarALinhaDeFrente() {
        AnaliseAmeacasResponse ameacas = motor.analisarAmeacas(
            List.of("Marco Polo", "Lian Po", "Dolia")
        );

        AlvoPrioritarioAmeacaResponse marcoPolo = ameacas.alvosPrioritarios()
            .stream()
            .filter(alvo -> alvo.heroi().equals("Marco Polo"))
            .findFirst()
            .orElseThrow();
        AlvoPrioritarioAmeacaResponse lianPo = ameacas.alvosPrioritarios()
            .stream()
            .filter(alvo -> alvo.heroi().equals("Lian Po"))
            .findFirst()
            .orElseThrow();

        assertTrue(marcoPolo.prioridade() > lianPo.prioridade());
        assertTrue(ameacas.planoResposta().contains("Marco Polo"));
        assertTrue(
            ameacas.planoResposta().contains(
                "em vez de descarregar tudo na linha de frente"
            )
        );
        assertFalse(ameacas.planoResposta().startsWith("Embora "));
    }

    @Test''',
    'cenário real sem viés de suporte'
)

write(
    'src/test/java/br/com/carlosdaniel/hokdraftcoach/service/PlanoRespostaAmeacaTest.java',
    '''package br.com.carlosdaniel.hokdraftcoach.service;

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
'''
)

print('Plano de resposta contextual aplicado com sucesso.')
