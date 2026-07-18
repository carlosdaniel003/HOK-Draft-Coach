package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.CondicaoVitoriaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilEconomicoHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RespostaCondicaoVitoriaResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.CondicaoVitoriaTipo;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.TipoRespostaCondicaoVitoria;

@Service
public class CondicaoVitoriaService {

    private final DnaHeroiService dnaHeroiService;
    private final EconomiaRecursosService economiaRecursosService;

    public CondicaoVitoriaService(
        DnaHeroiService dnaHeroiService,
        EconomiaRecursosService economiaRecursosService
    ) {
        this.dnaHeroiService = dnaHeroiService;
        this.economiaRecursosService = economiaRecursosService;
    }

    public List<CondicaoVitoriaResponse> descobrir(
        List<Heroi> equipe,
        DnaComposicao dna
    ) {
        if (equipe == null || equipe.isEmpty()) {
            return List.of();
        }

        Map<Heroi, DnaHeroi> individuais = new LinkedHashMap<>();
        equipe.forEach(heroi -> individuais.put(
            heroi,
            dnaHeroiService.calcular(heroi)
        ));
        List<CondicaoVitoriaResponse> candidatas = new ArrayList<>();

        adicionarHipercarregador(candidatas, equipe, dna, individuais);
        adicionarSplitPush(candidatas, dna, individuais);
        adicionarCerco(candidatas, dna, individuais);
        adicionarLutasLongas(candidatas, dna, individuais);
        adicionarPickoff(candidatas, dna, individuais);
        adicionarDive(candidatas, dna, individuais);
        adicionarWomboCombo(candidatas, dna, individuais);
        adicionarObjetivos(candidatas, dna, individuais);
        adicionarEscalamento(candidatas, dna, individuais);

        List<CondicaoVitoriaResponse> ordenadas = candidatas.stream()
            .filter(condicao -> condicao.forca() >= 58)
            .sorted(
                Comparator.comparingInt(CondicaoVitoriaResponse::forca)
                    .reversed()
            )
            .limit(3)
            .toList();

        List<CondicaoVitoriaResponse> resultado = new ArrayList<>();
        for (int indice = 0; indice < ordenadas.size(); indice++) {
            resultado.add(ordenadas.get(indice).comoPrincipal(indice == 0));
        }
        return List.copyOf(resultado);
    }

    private void adicionarHipercarregador(
        List<CondicaoVitoriaResponse> candidatas,
        List<Heroi> equipe,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        Heroi carry = equipe.stream()
            .max(Comparator.comparingDouble(heroi -> notaCarry(
                heroi,
                individuais.get(heroi)
            )))
            .orElse(null);
        if (carry == null) {
            return;
        }

        DnaHeroi dnaCarry = individuais.get(carry);
        PerfilEconomicoHeroiResponse economiaCarry =
            economiaRecursosService.perfil(carry);
        if (
            dnaCarry.valor(DimensaoEstrategica.DPS) < 75
                || dnaCarry.valor(DimensaoEstrategica.ESCALAMENTO) < 60
        ) {
            return;
        }

        Heroi amplificador = equipe.stream()
            .filter(heroi -> !heroi.equals(carry))
            .filter(heroi -> possuiTag(
                heroi,
                "amplificação",
                "dupla",
                "hipercarregador",
                "proteção",
                "escudo",
                "cura"
            ))
            .max(Comparator.comparingInt(heroi ->
                individuais.get(heroi).valor(DimensaoEstrategica.PROTECAO)
            ))
            .orElse(null);
        List<Heroi> frontline = equipe.stream()
            .filter(heroi -> !heroi.equals(carry))
            .filter(heroi ->
                individuais.get(heroi).valor(
                    DimensaoEstrategica.LINHA_DE_FRENTE
                ) >= 62
                    || heroi.getClasse() == ClasseHeroi.TANQUE
            )
            .toList();

        int forca = mediaPonderada(
            dnaCarry.valor(DimensaoEstrategica.DPS), 25,
            dnaCarry.valor(DimensaoEstrategica.ESCALAMENTO), 20,
            economiaCarry.dependenciaRecursos(), 20,
            dna.valor(DimensaoEstrategica.PROTECAO), 15,
            dna.valor(DimensaoEstrategica.LINHA_DE_FRENTE), 10,
            dna.valor(DimensaoEstrategica.PEEL), 10
        );
        if (amplificador != null) {
            forca = limitar(forca + 6, 0, 100);
        }
        if (!frontline.isEmpty()) {
            forca = limitar(forca + 5, 0, 100);
        }

        List<String> executores = new ArrayList<>();
        executores.add(carry.getNome());
        if (amplificador != null) {
            executores.add(amplificador.getNome());
        }
        frontline.stream()
            .map(Heroi::getNome)
            .filter(nome -> !executores.contains(nome))
            .limit(2)
            .forEach(executores::add);

        String amplificadorNome = amplificador == null
            ? "seu protetor"
            : amplificador.getNome();
        List<RespostaCondicaoVitoriaResponse> respostas = List.of(
            resposta(
                TipoRespostaCondicaoVitoria.ACESSAR_E_ELIMINAR_CARREGADOR,
                100,
                "Acessar e eliminar " + carry.getNome(),
                "Ignorar a troca frontal e alcançar o carregador antes que ele mantenha DPS livre.",
                List.of(
                    DimensaoEstrategica.DIVE,
                    DimensaoEstrategica.EXPLOSAO,
                    DimensaoEstrategica.MOBILIDADE
                ),
                List.of(carry.getNome())
            ),
            resposta(
                TipoRespostaCondicaoVitoria.SEPARAR_CARREGADOR_DO_AMPLIFICADOR,
                94,
                "Separar " + amplificadorNome + " de " + carry.getNome(),
                "Deslocamentos, pickoff ou controle de zona quebram a dupla e reduzem proteção e amplificação.",
                List.of(
                    DimensaoEstrategica.CONTROLE,
                    DimensaoEstrategica.MOBILIDADE,
                    DimensaoEstrategica.ENGAGE
                ),
                amplificador == null
                    ? List.of(carry.getNome())
                    : List.of(amplificador.getNome(), carry.getNome())
            ),
            resposta(
                TipoRespostaCondicaoVitoria.QUEBRAR_FORMACAO_DA_LINHA_DE_FRENTE,
                90,
                "Impedir que a linha de frente mantenha a formação",
                "Flanco, deslocamento, anti-tanque e pressão lateral forçam a frontline a abandonar o carregador.",
                List.of(
                    DimensaoEstrategica.ANTI_TANQUE,
                    DimensaoEstrategica.CONTROLE,
                    DimensaoEstrategica.PRESSAO_LATERAL
                ),
                frontline.stream().map(Heroi::getNome).toList()
            )
        );

        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.PROTEGER_HIPERCARREGADOR,
            forca,
            false,
            "Proteger " + carry.getNome() + " como hipercarregador",
            "Manter " + carry.getNome()
                + " atacando livremente enquanto proteção e linha de frente sustentam uma luta front-to-back.",
            executores,
            List.of(
                carry.getNome() + " precisa receber prioridade de ouro e sobreviver à primeira entrada.",
                "A linha de frente precisa impedir acesso direto à retaguarda.",
                amplificadorNome + " precisa permanecer em alcance de proteção."
            ),
            List.of(
                "Dive ou explosão direta sobre " + carry.getNome() + ".",
                "Separação entre carregador, amplificador e linha de frente.",
                "Pressão lateral que impeça a equipe de permanecer agrupada."
            ),
            respostas
        ));
    }

    private void adicionarSplitPush(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.PRESSAO_LATERAL), 60,
            dna.valor(DimensaoEstrategica.MOBILIDADE), 20,
            dna.valor(DimensaoEstrategica.WAVE_CLEAR), 20
        );
        Heroi executor = melhor(individuais, DimensaoEstrategica.PRESSAO_LATERAL);
        if (executor == null || forca < 58) {
            return;
        }
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.SPLIT_PUSH,
            forca,
            false,
            "Vencer pela pressão lateral de " + executor.getNome(),
            "Criar uma ameaça permanente na rota lateral e obrigar o inimigo a escolher entre defender estruturas ou contestar objetivos.",
            List.of(executor.getNome()),
            List.of(
                executor.getNome() + " precisa vencer ou sobreviver ao duelo lateral.",
                "Os outros quatro jogadores precisam evitar luta desfavorável enquanto a pressão cresce."
            ),
            List.of(
                "Wave clear seguro e resposta global.",
                "Engage rápido sobre os quatro aliados agrupados.",
                "Objetivos iniciados antes que a pressão lateral alcance a torre."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.RESPONDER_PRESSAO_LATERAL,
                    100,
                    "Enviar uma resposta lateral estável",
                    "Usar duelista, resposta global ou wave clear que não dependa de múltiplos jogadores.",
                    List.of(
                        DimensaoEstrategica.PRESSAO_LATERAL,
                        DimensaoEstrategica.WAVE_CLEAR,
                        DimensaoEstrategica.MOBILIDADE
                    ),
                    List.of(executor.getNome())
                ),
                resposta(
                    TipoRespostaCondicaoVitoria.FORCAR_OBJETIVO_DO_LADO_OPOSTO,
                    88,
                    "Converter a ausência lateral em objetivo",
                    "Forçar luta ou objetivo antes que o split pusher consiga retornar.",
                    List.of(
                        DimensaoEstrategica.ENGAGE,
                        DimensaoEstrategica.OBJETIVOS
                    ),
                    List.of()
                )
            )
        ));
    }

    private void adicionarCerco(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.POKE), 35,
            dna.valor(DimensaoEstrategica.ALCANCE), 25,
            dna.valor(DimensaoEstrategica.WAVE_CLEAR), 25,
            dna.valor(DimensaoEstrategica.PROTECAO), 15
        );
        if (forca < 58) {
            return;
        }
        List<String> executores = melhores(
            individuais,
            DimensaoEstrategica.POKE,
            2
        );
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.CERCO_DE_TORRES,
            forca,
            false,
            "Cercar torres e desgastar antes da luta",
            "Controlar ondas, ocupar alcance e retirar vida do inimigo até que estruturas e objetivos não possam mais ser defendidos.",
            executores,
            List.of(
                "Manter prioridade de wave e visão lateral.",
                "Evitar engage direto antes que o poke crie vantagem."
            ),
            List.of(
                "Engage rápido que feche a distância.",
                "Flancos sobre a artilharia.",
                "Sustain capaz de neutralizar o desgaste."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.ENGAGE_SOBRE_POKE,
                    100,
                    "Iniciar antes que o cerco se estabeleça",
                    "Fechar a distância enquanto a composição de poke ainda está reposicionando.",
                    List.of(
                        DimensaoEstrategica.ENGAGE,
                        DimensaoEstrategica.MOBILIDADE
                    ),
                    executores
                ),
                resposta(
                    TipoRespostaCondicaoVitoria.FLANQUEAR_ARTILHARIA,
                    92,
                    "Flanquear os causadores de poke",
                    "Atacar por ângulos laterais para impedir que a artilharia use o alcance máximo.",
                    List.of(
                        DimensaoEstrategica.DIVE,
                        DimensaoEstrategica.EXPLOSAO
                    ),
                    executores
                )
            )
        ));
    }

    private void adicionarLutasLongas(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.DPS), 35,
            dna.valor(DimensaoEstrategica.SUSTAIN), 25,
            dna.valor(DimensaoEstrategica.LINHA_DE_FRENTE), 25,
            dna.valor(DimensaoEstrategica.PEEL), 15
        );
        if (forca < 58) {
            return;
        }
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.LUTAS_LONGAS,
            forca,
            false,
            "Vencer lutas prolongadas",
            "Absorver a primeira rotação inimiga e vencer conforme DPS, cura, escudos e resistência acumulam valor.",
            unir(
                melhores(individuais, DimensaoEstrategica.DPS, 1),
                melhores(individuais, DimensaoEstrategica.SUSTAIN, 1),
                melhores(individuais, DimensaoEstrategica.LINHA_DE_FRENTE, 1)
            ),
            List.of(
                "Evitar perder um carregador na primeira rotação.",
                "Manter contato suficiente para sustentar e aplicar DPS continuamente."
            ),
            List.of(
                "Explosão concentrada antes da cura.",
                "Anti-cura e execução.",
                "Pressão lateral que impeça a luta agrupada."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.FORCAR_LUTAS_CURTAS,
                    100,
                    "Resolver a luta na primeira rotação",
                    "Usar burst e pickoff para impedir que sustain e DPS prolongado entrem em funcionamento.",
                    List.of(
                        DimensaoEstrategica.EXPLOSAO,
                        DimensaoEstrategica.CONTROLE
                    ),
                    List.of()
                ),
                resposta(
                    TipoRespostaCondicaoVitoria.APLICAR_ANTI_CURA,
                    90,
                    "Reduzir a sustentação",
                    "Anti-cura e execução diminuem a vantagem acumulada em lutas extensas.",
                    List.of(DimensaoEstrategica.ANTI_CURA),
                    melhores(individuais, DimensaoEstrategica.SUSTAIN, 2)
                )
            )
        ));
    }

    private void adicionarPickoff(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.CONTROLE), 35,
            dna.valor(DimensaoEstrategica.EXPLOSAO), 30,
            dna.valor(DimensaoEstrategica.MOBILIDADE), 20,
            dna.valor(DimensaoEstrategica.ALCANCE), 15
        );
        if (forca < 62) {
            return;
        }
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.PICKOFF,
            forca,
            false,
            "Criar eliminações antes dos objetivos",
            "Controlar visão e capturar um alvo isolado para transformar o mapa em uma sequência de vantagens numéricas.",
            unir(
                melhores(individuais, DimensaoEstrategica.CONTROLE, 1),
                melhores(individuais, DimensaoEstrategica.EXPLOSAO, 1)
            ),
            List.of(
                "O inimigo precisa circular por áreas sem visão.",
                "Controle e dano devem alcançar o mesmo alvo."
            ),
            List.of(
                "Formação agrupada e linha de frente à frente dos carries.",
                "Purificação, desengage e visão preventiva."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.PROTEGER_RETAGUARDA,
                    94,
                    "Circular em formação protegida",
                    "Linha de frente, peel e desengage reduzem oportunidades de captura isolada.",
                    List.of(
                        DimensaoEstrategica.PEEL,
                        DimensaoEstrategica.DESENGAGE,
                        DimensaoEstrategica.LINHA_DE_FRENTE
                    ),
                    List.of()
                )
            )
        ));
    }

    private void adicionarDive(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.DIVE), 45,
            dna.valor(DimensaoEstrategica.EXPLOSAO), 25,
            dna.valor(DimensaoEstrategica.CONTROLE), 15,
            dna.valor(DimensaoEstrategica.MOBILIDADE), 15
        );
        if (forca < 62) {
            return;
        }
        List<String> executores = melhores(
            individuais,
            DimensaoEstrategica.DIVE,
            3
        );
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.DIVE_NA_RETAGUARDA,
            forca,
            false,
            "Colapsar sobre a retaguarda",
            "Ignorar a frontline e usar mobilidade, controle e explosão para remover os carregadores inimigos.",
            executores,
            List.of(
                "Os mergulhadores precisam alcançar o mesmo alvo.",
                "A entrada deve ocorrer antes que a retaguarda reposicione."
            ),
            List.of(
                "Peel em cadeia e desengage.",
                "Carregadores móveis ou resistentes à primeira rotação."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.PROTEGER_RETAGUARDA,
                    100,
                    "Adicionar peel e desengage",
                    "Interromper a primeira entrada para que os carregadores mantenham distância.",
                    List.of(
                        DimensaoEstrategica.PEEL,
                        DimensaoEstrategica.DESENGAGE,
                        DimensaoEstrategica.PROTECAO
                    ),
                    executores
                )
            )
        ));
    }

    private void adicionarWomboCombo(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.ENGAGE), 30,
            dna.valor(DimensaoEstrategica.CONTROLE), 30,
            dna.valor(DimensaoEstrategica.EXPLOSAO), 30,
            dna.valor(DimensaoEstrategica.MOBILIDADE), 10
        );
        if (forca < 66) {
            return;
        }
        List<String> executores = unir(
            melhores(individuais, DimensaoEstrategica.ENGAGE, 1),
            melhores(individuais, DimensaoEstrategica.CONTROLE, 1),
            melhores(individuais, DimensaoEstrategica.EXPLOSAO, 1)
        );
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.WOMBO_COMBO,
            forca,
            false,
            "Vencer com iniciação e combo em área",
            "Agrupar ou imobilizar múltiplos inimigos e converter o controle em dano explosivo coordenado.",
            executores,
            List.of(
                "As habilidades decisivas precisam estar disponíveis ao mesmo tempo.",
                "O iniciador precisa encontrar múltiplos alvos."
            ),
            List.of(
                "Formação espalhada.",
                "Desengage ou interrupção do iniciador.",
                "Pressão lateral que impeça o agrupamento."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.ESPALHAR_FORMACAO,
                    100,
                    "Evitar agrupar múltiplos alvos",
                    "Manter espaçamento reduz o valor das habilidades em área.",
                    List.of(DimensaoEstrategica.MOBILIDADE),
                    List.of()
                ),
                resposta(
                    TipoRespostaCondicaoVitoria.INTERROMPER_INICIADOR,
                    94,
                    "Interromper ou zonear o iniciador",
                    "Controle antecipado e desengage impedem que o combo seja iniciado.",
                    List.of(
                        DimensaoEstrategica.DESENGAGE,
                        DimensaoEstrategica.CONTROLE
                    ),
                    melhores(individuais, DimensaoEstrategica.ENGAGE, 1)
                )
            )
        ));
    }

    private void adicionarObjetivos(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.OBJETIVOS), 50,
            dna.valor(DimensaoEstrategica.DPS), 20,
            dna.valor(DimensaoEstrategica.LINHA_DE_FRENTE), 20,
            dna.valor(DimensaoEstrategica.WAVE_CLEAR), 10
        );
        if (forca < 65) {
            return;
        }
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.CONTROLE_DE_OBJETIVOS,
            forca,
            false,
            "Controlar objetivos e converter em estruturas",
            "Usar dano, zoneamento e resistência para iniciar objetivos com segurança e obrigar o inimigo a entrar em posição ruim.",
            unir(
                melhores(individuais, DimensaoEstrategica.OBJETIVOS, 2),
                melhores(individuais, DimensaoEstrategica.LINHA_DE_FRENTE, 1)
            ),
            List.of(
                "A equipe precisa chegar primeiro à região do objetivo.",
                "Wave clear e visão devem impedir contestação limpa."
            ),
            List.of(
                "Pickoff antes do objetivo.",
                "Pressão lateral simultânea.",
                "Engage quando a equipe estiver presa no terreno."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.CRIAR_PICKOFF_ANTES_DO_OBJETIVO,
                    100,
                    "Criar vantagem numérica antes da disputa",
                    "Capturar um alvo antes que a composição consiga montar a zona do objetivo.",
                    List.of(
                        DimensaoEstrategica.CONTROLE,
                        DimensaoEstrategica.EXPLOSAO,
                        DimensaoEstrategica.MOBILIDADE
                    ),
                    List.of()
                )
            )
        ));
    }

    private void adicionarEscalamento(
        List<CondicaoVitoriaResponse> candidatas,
        DnaComposicao dna,
        Map<Heroi, DnaHeroi> individuais
    ) {
        int forca = mediaPonderada(
            dna.valor(DimensaoEstrategica.ESCALAMENTO), 60,
            dna.valor(DimensaoEstrategica.DPS), 20,
            dna.valor(DimensaoEstrategica.PROTECAO), 20
        );
        if (forca < 68) {
            return;
        }
        List<String> executores = melhores(
            individuais,
            DimensaoEstrategica.ESCALAMENTO,
            3
        );
        candidatas.add(new CondicaoVitoriaResponse(
            CondicaoVitoriaTipo.ESCALAMENTO_TARDIO,
            forca,
            false,
            "Alcançar os picos de itens do fim de jogo",
            "Estabilizar o início e crescer até que os carregadores superem o dano e a resistência inimigos.",
            executores,
            List.of(
                "Evitar perdas irreversíveis no início.",
                "Garantir rotas e campos suficientes para os heróis dependentes."
            ),
            List.of(
                "Invasão e negação de recursos.",
                "Objetivos e torres acelerados antes dos picos de itens."
            ),
            List.of(
                resposta(
                    TipoRespostaCondicaoVitoria.NEGAR_RECURSOS,
                    100,
                    "Negar ouro aos carregadores",
                    "Invasão, pressão de rota e controle de visão atrasam os picos de itens.",
                    List.of(
                        DimensaoEstrategica.MOBILIDADE,
                        DimensaoEstrategica.OBJETIVOS
                    ),
                    executores
                ),
                resposta(
                    TipoRespostaCondicaoVitoria.ACELERAR_O_JOGO,
                    94,
                    "Acelerar torres e objetivos",
                    "Converter vantagem inicial antes que a composição de escalamento fique completa.",
                    List.of(
                        DimensaoEstrategica.OBJETIVOS,
                        DimensaoEstrategica.ENGAGE,
                        DimensaoEstrategica.WAVE_CLEAR
                    ),
                    List.of()
                )
            )
        ));
    }

    private double notaCarry(Heroi heroi, DnaHeroi dna) {
        PerfilEconomicoHeroiResponse economia = economiaRecursosService.perfil(
            heroi
        );
        return dna.valor(DimensaoEstrategica.DPS) * 0.45
            + dna.valor(DimensaoEstrategica.ESCALAMENTO) * 0.30
            + economia.dependenciaRecursos() * 0.25;
    }

    private Heroi melhor(
        Map<Heroi, DnaHeroi> individuais,
        DimensaoEstrategica dimensao
    ) {
        return individuais.entrySet().stream()
            .max(Comparator.comparingInt(entry -> entry.getValue().valor(dimensao)))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private List<String> melhores(
        Map<Heroi, DnaHeroi> individuais,
        DimensaoEstrategica dimensao,
        int limite
    ) {
        return individuais.entrySet().stream()
            .sorted(
                Comparator.<Map.Entry<Heroi, DnaHeroi>>comparingInt(
                    entry -> entry.getValue().valor(dimensao)
                ).reversed()
            )
            .filter(entry -> entry.getValue().valor(dimensao) >= 50)
            .limit(limite)
            .map(entry -> entry.getKey().getNome())
            .toList();
    }

    @SafeVarargs
    private final List<String> unir(List<String>... listas) {
        Set<String> resultado = new LinkedHashSet<>();
        for (List<String> lista : listas) {
            resultado.addAll(lista);
        }
        return List.copyOf(resultado);
    }

    private RespostaCondicaoVitoriaResponse resposta(
        TipoRespostaCondicaoVitoria tipo,
        int prioridade,
        String titulo,
        String descricao,
        List<DimensaoEstrategica> capacidades,
        List<String> alvos
    ) {
        return new RespostaCondicaoVitoriaResponse(
            tipo,
            prioridade,
            titulo,
            descricao,
            capacidades,
            alvos
        );
    }

    private int mediaPonderada(int... valoresEPesos) {
        double soma = 0;
        int somaPesos = 0;
        for (int indice = 0; indice < valoresEPesos.length; indice += 2) {
            soma += valoresEPesos[indice] * valoresEPesos[indice + 1];
            somaPesos += valoresEPesos[indice + 1];
        }
        return somaPesos == 0
            ? 0
            : limitar((int) Math.round(soma / somaPesos), 0, 100);
    }

    private boolean possuiTag(Heroi heroi, String... tags) {
        return heroi.getCaracteristicas().stream()
            .map(this::normalizar)
            .anyMatch(caracteristica -> {
                for (String tag : tags) {
                    if (caracteristica.contains(normalizar(tag))) {
                        return true;
                    }
                }
                return false;
            });
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase(Locale.ROOT);
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
