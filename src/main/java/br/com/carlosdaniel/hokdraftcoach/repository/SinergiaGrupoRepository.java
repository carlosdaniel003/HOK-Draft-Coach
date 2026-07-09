package br.com.carlosdaniel.hokdraftcoach.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.RegraSinergiaGrupo;
import br.com.carlosdaniel.hokdraftcoach.model.RequisitoSinergiaGrupo;
import br.com.carlosdaniel.hokdraftcoach.model.TipoSinergiaGrupo;

@Repository
public class SinergiaGrupoRepository {

    private final List<RegraSinergiaGrupo> regras = List.of(
        new RegraSinergiaGrupo(
            "LIAN_PO_MARCO_POLO_DOLIA",
            TipoSinergiaGrupo.RESET_DE_HABILIDADES,
            100,
            List.of("Lian Po", "Marco Polo", "Dolia"),
            List.of(),
            "Lian Po cria a entrada, Marco Polo converte o controle em dano contínuo e Dolia permite uma segunda ultimate decisiva.",
            List.of(
                "Lian Po inicia e mantém os alvos ocupados.",
                "Marco Polo entra com a ultimate sobre o controle.",
                "Dolia reinicia a ultimate de maior impacto.",
                "A equipe repete a janela de dano ou guarda o reset para a segunda rotação."
            ),
            List.of(
                "Duas janelas de ultimate em área.",
                "Controle, dano sustentado e reset no mesmo plano.",
                "Alta capacidade de virar lutas em objetivos."
            )
        ),
        new RegraSinergiaGrupo(
            "GUIGUZI_MAGO_AREA_ADC_AREA",
            TipoSinergiaGrupo.AGRUPAMENTO_SEGUIDO_DE_DANO_EM_AREA,
            96,
            List.of("Guiguzi"),
            List.of(
                requisito(
                    "MAGO_AREA",
                    "Mago com dano ou controle em área",
                    1,
                    List.of(ClasseHeroi.MAGO),
                    List.of(
                        "dano em área",
                        "controle em área",
                        "artilharia",
                        "zona"
                    ),
                    null,
                    0
                ),
                requisito(
                    "ADC_AREA",
                    "Atirador com ultimate ou dano relevante em área",
                    1,
                    List.of(ClasseHeroi.ATIRADOR),
                    List.of(
                        "dano em área",
                        "artilharia",
                        "ultimate em área",
                        "controle global"
                    ),
                    null,
                    0
                )
            ),
            "Guiguzi agrupa os inimigos e abre uma janela simultânea para o mago e o ADC descarregarem dano em área.",
            List.of(
                "Guiguzi entra camuflado e agrupa os alvos.",
                "O mago aplica controle ou dano em área.",
                "O ADC usa a ultimate enquanto os inimigos ainda estão agrupados."
            ),
            List.of(
                "Grande conversão de agrupamento em dano coletivo.",
                "Dificulta desengage individual.",
                "Excelente luta em corredores e objetivos."
            )
        ),
        new RegraSinergiaGrupo(
            "CONTROLE_EM_CADEIA_COM_DANO_AREA",
            TipoSinergiaGrupo.CONTROLE_EM_CADEIA,
            88,
            List.of(),
            List.of(
                requisito(
                    "CONTROLADORES",
                    "Dois heróis com controle confiável",
                    2,
                    List.of(),
                    List.of(),
                    DimensaoEstrategica.CONTROLE,
                    70
                ),
                requisito(
                    "DANO_AREA",
                    "Um finalizador com dano em área",
                    1,
                    List.of(),
                    List.of(
                        "dano em área",
                        "artilharia",
                        "ultimate em área"
                    ),
                    null,
                    0
                )
            ),
            "Dois controles consecutivos prolongam a janela para que um terceiro herói converta o alvo ou a área em dano.",
            List.of(
                "Primeiro controle força purificação ou mobilidade.",
                "Segundo controle impede a saída.",
                "O finalizador aplica dano durante a janela estendida."
            ),
            List.of(
                "Redundância útil de controle.",
                "Maior segurança para ultimates canalizadas.",
                "Boa resposta contra heróis móveis."
            )
        ),
        new RegraSinergiaGrupo(
            "SEQUENCIA_DE_ENGAGE_COM_FOLLOW_UP",
            TipoSinergiaGrupo.SEQUENCIA_DE_ENGAGE,
            86,
            List.of(),
            List.of(
                requisito(
                    "INICIADORES",
                    "Dois iniciadores que entram em tempos diferentes",
                    2,
                    List.of(),
                    List.of(),
                    DimensaoEstrategica.ENGAGE,
                    65
                ),
                requisito(
                    "FOLLOW_UP",
                    "Um herói capaz de acompanhar a entrada",
                    1,
                    List.of(),
                    List.of(
                        "dano em área",
                        "explosão",
                        "dano sustentado",
                        "finalização"
                    ),
                    null,
                    0
                )
            ),
            "A primeira entrada força respostas defensivas e a segunda mantém a luta aberta para o causador de dano.",
            List.of(
                "Primeiro iniciador força mobilidade ou purificação.",
                "Segundo iniciador entra após os recursos defensivos.",
                "O follow-up converte a sequência em eliminações."
            ),
            List.of(
                "Mais consistência contra desengage.",
                "Duas ondas de pressão.",
                "Maior segurança para o follow-up."
            )
        ),
        new RegraSinergiaGrupo(
            "PROTECAO_EM_CAMADAS_DO_HIPERCARREGADOR",
            TipoSinergiaGrupo.PROTECAO_EM_CAMADAS,
            90,
            List.of(),
            List.of(
                requisito(
                    "HIPERCARREGADOR",
                    "Atirador de escalamento e dano sustentado",
                    1,
                    List.of(ClasseHeroi.ATIRADOR),
                    List.of(
                        "fim de jogo",
                        "hipercarregador",
                        "carregadora",
                        "dano sustentado"
                    ),
                    null,
                    0
                ),
                requisito(
                    "FRONTLINE",
                    "Linha de frente capaz de absorver a primeira rotação",
                    1,
                    List.of(),
                    List.of(),
                    DimensaoEstrategica.LINHA_DE_FRENTE,
                    65
                ),
                requisito(
                    "PROTETOR",
                    "Peel, cura ou escudo para a retaguarda",
                    1,
                    List.of(),
                    List.of(),
                    DimensaoEstrategica.PROTECAO,
                    62
                )
            ),
            "Uma camada frontal impede acesso direto e uma segunda camada protege o carregador quando o dive atravessa a frontline.",
            List.of(
                "A frontline ocupa espaço e recebe a primeira entrada.",
                "O protetor guarda controle, cura ou escudo para o dive.",
                "O hipercarregador mantém DPS durante toda a luta."
            ),
            List.of(
                "Front-to-back consistente.",
                "Maior tolerância a assassinos.",
                "Excelente aproveitamento de escalamento."
            )
        ),
        new RegraSinergiaGrupo(
            "DOLIA_ENGAGE_ULTIMATE_AREA",
            TipoSinergiaGrupo.COMBO_DE_ULTIMATES,
            91,
            List.of("Dolia"),
            List.of(
                requisito(
                    "ULTIMATE_DE_ENGAGE",
                    "Iniciador com ultimate decisiva",
                    1,
                    List.of(),
                    List.of(
                        "iniciação",
                        "controle em área",
                        "agrupamento"
                    ),
                    DimensaoEstrategica.ENGAGE,
                    62
                ),
                requisito(
                    "ULTIMATE_DE_DANO",
                    "Causador de dano em área para receber o reset",
                    1,
                    List.of(),
                    List.of(
                        "dano em área",
                        "artilharia",
                        "finalização",
                        "ultimate em área"
                    ),
                    null,
                    0
                )
            ),
            "Dolia transforma uma composição de engage e dano em área em duas rotações possíveis de ultimate.",
            List.of(
                "O iniciador abre a luta.",
                "O causador de dano usa a ultimate sobre a entrada.",
                "Dolia decide qual ultimate reiniciar conforme a luta."
            ),
            List.of(
                "Flexibilidade entre segundo engage e segundo dano.",
                "Grande valor em lutas por objetivo.",
                "Pressão adicional após a primeira rotação."
            )
        ),
        new RegraSinergiaGrupo(
            "AGRUPAMENTO_E_DUAS_FONTES_DE_AREA",
            TipoSinergiaGrupo.WOMBO_COMBO,
            92,
            List.of(),
            List.of(
                requisito(
                    "AGRUPADOR",
                    "Herói que agrupa ou prende múltiplos inimigos",
                    1,
                    List.of(),
                    List.of(
                        "agrupamento",
                        "gancho",
                        "controle em área",
                        "provocação"
                    ),
                    null,
                    0
                ),
                requisito(
                    "FONTES_DE_AREA",
                    "Duas fontes diferentes de dano ou controle em área",
                    2,
                    List.of(),
                    List.of(
                        "dano em área",
                        "artilharia",
                        "controle em área",
                        "ultimate em área"
                    ),
                    null,
                    0
                )
            ),
            "O agrupador reduz o espaço entre os alvos e permite sobrepor duas habilidades de área.",
            List.of(
                "Agrupar ou fixar os inimigos.",
                "Primeira fonte de área impede a dispersão.",
                "Segunda fonte de área finaliza a luta."
            ),
            List.of(
                "Alta explosão coletiva.",
                "Excelente em terrenos estreitos.",
                "Converte um único engage em múltiplas eliminações."
            )
        )
    );

    public List<RegraSinergiaGrupo> listar() {
        return regras;
    }

    private static RequisitoSinergiaGrupo requisito(
        String codigo,
        String descricao,
        int quantidade,
        List<ClasseHeroi> classes,
        List<String> tags,
        DimensaoEstrategica dimensao,
        int minimo
    ) {
        return new RequisitoSinergiaGrupo(
            codigo,
            descricao,
            quantidade,
            classes,
            tags,
            dimensao,
            minimo
        );
    }
}
