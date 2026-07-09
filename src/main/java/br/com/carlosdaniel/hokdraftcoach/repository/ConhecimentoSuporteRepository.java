package br.com.carlosdaniel.hokdraftcoach.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.carlosdaniel.hokdraftcoach.model.ArquetipoSuporte;
import br.com.carlosdaniel.hokdraftcoach.model.ConfiancaDado;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilSuporte;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaBotLane;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;

@Repository
public class ConhecimentoSuporteRepository {

    private final List<PerfilSuporte> perfis = List.of(
        perfil(
            "Annette",
            arq(ArquetipoSuporte.PROTETOR, ArquetipoSuporte.DESENGAGE,
                ArquetipoSuporte.CONTROLE_DE_AREA),
            comp(TipoComposicao.POKE, TipoComposicao.HIPERCARREGADOR,
                TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.DIVE, TipoComposicao.ENGAGE_AGRUPADO,
                TipoComposicao.ALTA_MOBILIDADE),
            comp(TipoComposicao.PICKOFF, TipoComposicao.SPLIT_PUSH),
            8, 5, 10, 4, 6, 6, 5,
            "Excelente para negar entradas agrupadas e proteger atiradores sem mobilidade."
        ),
        perfil(
            "Lapulapu",
            arq(ArquetipoSuporte.TANQUE, ArquetipoSuporte.INICIADOR,
                ArquetipoSuporte.PROTETOR),
            comp(TipoComposicao.FRONT_TO_BACK, TipoComposicao.ENGAGE_AGRUPADO,
                TipoComposicao.LUTAS_LONGAS),
            comp(TipoComposicao.DIVE, TipoComposicao.EXPLOSAO,
                TipoComposicao.ALVOS_IMOVEIS),
            comp(TipoComposicao.POKE, TipoComposicao.DESENGAGE),
            8, 8, 7, 4, 7, 5, 5,
            "Cria a janela de entrada para ultimates de área e absorve a primeira resposta inimiga."
        ),
        perfil(
            "Cai Yan",
            arq(ArquetipoSuporte.CURADOR, ArquetipoSuporte.ENCANTADOR,
                ArquetipoSuporte.PROTETOR),
            comp(TipoComposicao.HIPERCARREGADOR, TipoComposicao.LUTAS_LONGAS,
                TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.POKE, TipoComposicao.ESCUDOS_E_CURA),
            comp(TipoComposicao.EXPLOSAO, TipoComposicao.ENGAGE_AGRUPADO,
                TipoComposicao.PICKOFF),
            9, 3, 5, 10, 7, 4, 3,
            "Muito forte quando a equipe consegue permanecer agrupada; perde valor contra burst e anti-cura."
        ),
        perfil(
            "Da Qiao",
            arq(ArquetipoSuporte.MACRO, ArquetipoSuporte.DESENGAGE,
                ArquetipoSuporte.CONTROLE_DE_AREA),
            comp(TipoComposicao.SPLIT_PUSH, TipoComposicao.POKE,
                TipoComposicao.PICKOFF),
            comp(TipoComposicao.FRONT_TO_BACK, TipoComposicao.ALVOS_IMOVEIS,
                TipoComposicao.LUTAS_LONGAS),
            comp(TipoComposicao.ALTA_MOBILIDADE, TipoComposicao.DIVE),
            7, 6, 9, 5, 7, 10, 10,
            "Quanto maior a coordenação, maior o valor dos portais, retornos e rotações globais."
        ),
        perfil(
            "Dolia",
            arq(ArquetipoSuporte.ENCANTADOR, ArquetipoSuporte.AMPLIFICADOR,
                ArquetipoSuporte.CURADOR),
            comp(TipoComposicao.ULTIMATES_DECISIVAS, TipoComposicao.LUTAS_LONGAS,
                TipoComposicao.ENGAGE_AGRUPADO),
            comp(TipoComposicao.POKE, TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.PICKOFF, TipoComposicao.EXPLOSAO),
            8, 5, 7, 9, 6, 6, 8,
            "Prioriza aliados cuja ultimate define a luta; Marco Polo e Loong aproveitam muito esse reset."
        ),
        perfil(
            "Donghuang",
            arq(ArquetipoSuporte.TANQUE, ArquetipoSuporte.PICKOFF,
                ArquetipoSuporte.INICIADOR),
            comp(TipoComposicao.PICKOFF, TipoComposicao.EXPLOSAO,
                TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.DIVE, TipoComposicao.ALTA_MOBILIDADE,
                TipoComposicao.HIPERCARREGADOR),
            comp(TipoComposicao.POKE, TipoComposicao.ESCUDOS_E_CURA),
            8, 9, 6, 6, 8, 5, 5,
            "A supressão pune um único carregador ou mergulhador, mesmo quando ele possui muita mobilidade."
        ),
        perfil(
            "Dun",
            arq(ArquetipoSuporte.TANQUE, ArquetipoSuporte.PROTETOR,
                ArquetipoSuporte.INICIADOR),
            comp(TipoComposicao.FRONT_TO_BACK, TipoComposicao.LUTAS_LONGAS),
            comp(TipoComposicao.DIVE, TipoComposicao.EXPLOSAO),
            comp(TipoComposicao.POKE, TipoComposicao.ALTA_MOBILIDADE),
            8, 7, 7, 7, 6, 5, 4,
            "Escolha estável quando a equipe precisa simultaneamente de linha de frente e peel."
        ),
        perfil(
            "Dyadia",
            arq(ArquetipoSuporte.CURADOR, ArquetipoSuporte.ENCANTADOR,
                ArquetipoSuporte.POKE),
            comp(TipoComposicao.LUTAS_LONGAS, TipoComposicao.POKE,
                TipoComposicao.ALTA_MOBILIDADE),
            comp(TipoComposicao.FRONT_TO_BACK, TipoComposicao.ESCUDOS_E_CURA),
            comp(TipoComposicao.EXPLOSAO, TipoComposicao.PICKOFF),
            8, 4, 6, 9, 8, 8, 6,
            "Combina pressão de rota, cura e mobilidade; funciona bem como blind pick."
        ),
        perfil(
            "Guiguzi",
            arq(ArquetipoSuporte.INICIADOR, ArquetipoSuporte.PICKOFF,
                ArquetipoSuporte.MACRO),
            comp(TipoComposicao.ENGAGE_AGRUPADO, TipoComposicao.PICKOFF,
                TipoComposicao.EXPLOSAO),
            comp(TipoComposicao.POKE, TipoComposicao.ALVOS_IMOVEIS,
                TipoComposicao.SPLIT_PUSH),
            comp(TipoComposicao.DESENGAGE, TipoComposicao.CONTROLE_PESADO),
            4, 10, 3, 2, 8, 9, 8,
            "A camuflagem e o agrupamento transformam visão negada em iniciações explosivas."
        ),
        perfil(
            "Kui",
            arq(ArquetipoSuporte.PICKOFF, ArquetipoSuporte.TANQUE,
                ArquetipoSuporte.CONTROLE_DE_AREA),
            comp(TipoComposicao.PICKOFF, TipoComposicao.POKE),
            comp(TipoComposicao.ALVOS_IMOVEIS, TipoComposicao.HIPERCARREGADOR,
                TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.ALTA_MOBILIDADE, TipoComposicao.DIVE),
            6, 8, 5, 2, 8, 5, 7,
            "O gancho quebra formações front-to-back ao remover um alvo antes da luta."
        ),
        perfil(
            "Lian Po",
            arq(ArquetipoSuporte.TANQUE, ArquetipoSuporte.INICIADOR,
                ArquetipoSuporte.CONTROLE_DE_AREA),
            comp(TipoComposicao.ENGAGE_AGRUPADO, TipoComposicao.FRONT_TO_BACK,
                TipoComposicao.ULTIMATES_DECISIVAS),
            comp(TipoComposicao.DIVE, TipoComposicao.ALVOS_IMOVEIS,
                TipoComposicao.CONTROLE_PESADO),
            comp(TipoComposicao.POKE, TipoComposicao.DESENGAGE),
            8, 10, 7, 4, 8, 6, 6,
            "Resiste a interrupções e mantém alvos presos para ultimates como a de Marco Polo."
        ),
        perfil(
            "Liu Bang",
            arq(ArquetipoSuporte.PROTETOR, ArquetipoSuporte.MACRO,
                ArquetipoSuporte.TANQUE),
            comp(TipoComposicao.SPLIT_PUSH, TipoComposicao.HIPERCARREGADOR,
                TipoComposicao.DIVE),
            comp(TipoComposicao.PICKOFF, TipoComposicao.DIVE,
                TipoComposicao.EXPLOSAO),
            comp(TipoComposicao.POKE, TipoComposicao.ENGAGE_AGRUPADO),
            9, 5, 7, 5, 5, 10, 8,
            "A proteção global permite pressão lateral sem abandonar o carregador."
        ),
        perfil(
            "Liu Shan",
            arq(ArquetipoSuporte.TANQUE, ArquetipoSuporte.INICIADOR,
                ArquetipoSuporte.PICKOFF),
            comp(TipoComposicao.PICKOFF, TipoComposicao.ENGAGE_AGRUPADO,
                TipoComposicao.SPLIT_PUSH),
            comp(TipoComposicao.ALVOS_IMOVEIS, TipoComposicao.POKE),
            comp(TipoComposicao.DESENGAGE, TipoComposicao.ALTA_MOBILIDADE),
            7, 9, 6, 3, 9, 7, 5,
            "Converte qualquer vantagem da dupla em pressão de torre e controle em cadeia."
        ),
        perfil(
            "Ming",
            arq(ArquetipoSuporte.AMPLIFICADOR, ArquetipoSuporte.ENCANTADOR,
                ArquetipoSuporte.PROTETOR),
            comp(TipoComposicao.HIPERCARREGADOR, TipoComposicao.FRONT_TO_BACK,
                TipoComposicao.LUTAS_LONGAS),
            comp(TipoComposicao.ESCUDOS_E_CURA, TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.DIVE, TipoComposicao.ENGAGE_AGRUPADO,
                TipoComposicao.PICKOFF),
            9, 2, 4, 7, 8, 5, 4,
            "Excelente para multiplicar um ADC forte; perde valor quando a dupla é separada."
        ),
        perfil(
            "Mozi",
            arq(ArquetipoSuporte.POKE, ArquetipoSuporte.CONTROLE_DE_AREA,
                ArquetipoSuporte.DESENGAGE),
            comp(TipoComposicao.POKE, TipoComposicao.PICKOFF,
                TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.ALVOS_IMOVEIS, TipoComposicao.ULTIMATES_DECISIVAS,
                TipoComposicao.ENGAGE_AGRUPADO),
            comp(TipoComposicao.DIVE, TipoComposicao.ALTA_MOBILIDADE),
            7, 6, 9, 3, 9, 5, 7,
            "Combina especialmente bem com ADCs de artilharia ao encadear poke e controle."
        ),
        perfil(
            "Sakeer",
            arq(ArquetipoSuporte.CURADOR, ArquetipoSuporte.CONTROLE_DE_AREA,
                ArquetipoSuporte.PROTETOR),
            comp(TipoComposicao.LUTAS_LONGAS, TipoComposicao.FRONT_TO_BACK,
                TipoComposicao.POKE),
            comp(TipoComposicao.POKE, TipoComposicao.DIVE),
            comp(TipoComposicao.EXPLOSAO, TipoComposicao.PICKOFF),
            8, 6, 7, 9, 7, 8, 7,
            "Visão, cura e controle mantêm ADCs móveis ativos por mais tempo dentro da luta."
        ),
        perfil(
            "Sun Bin",
            arq(ArquetipoSuporte.DESENGAGE, ArquetipoSuporte.MACRO,
                ArquetipoSuporte.ENCANTADOR),
            comp(TipoComposicao.POKE, TipoComposicao.LUTAS_LONGAS,
                TipoComposicao.ALTA_MOBILIDADE),
            comp(TipoComposicao.ENGAGE_AGRUPADO, TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.EXPLOSAO, TipoComposicao.PICKOFF,
                TipoComposicao.CONTROLE_PESADO),
            7, 5, 9, 7, 7, 10, 7,
            "Acelera rotações e permite entrar e sair de lutas, mas não substitui uma linha de frente."
        ),
        perfil(
            "Xiang Yu",
            arq(ArquetipoSuporte.TANQUE, ArquetipoSuporte.PROTETOR,
                ArquetipoSuporte.DESENGAGE),
            comp(TipoComposicao.FRONT_TO_BACK, TipoComposicao.HIPERCARREGADOR),
            comp(TipoComposicao.DIVE, TipoComposicao.EXPLOSAO,
                TipoComposicao.ENGAGE_AGRUPADO),
            comp(TipoComposicao.POKE, TipoComposicao.ALTA_MOBILIDADE),
            9, 7, 9, 3, 7, 5, 4,
            "Deslocamentos e resistência expulsam lutadores e assassinos da zona do ADC."
        ),
        perfil(
            "Yaria",
            arq(ArquetipoSuporte.ENCANTADOR, ArquetipoSuporte.PROTETOR,
                ArquetipoSuporte.MACRO),
            comp(TipoComposicao.ALTA_MOBILIDADE, TipoComposicao.HIPERCARREGADOR,
                TipoComposicao.PICKOFF),
            comp(TipoComposicao.DIVE, TipoComposicao.EXPLOSAO,
                TipoComposicao.PICKOFF),
            comp(TipoComposicao.ENGAGE_AGRUPADO, TipoComposicao.POKE),
            10, 2, 6, 5, 7, 9, 5,
            "É uma das melhores parceiras para Marco Polo e Arli porque acompanha seus reposicionamentos."
        ),
        perfil(
            "Yuhuan",
            arq(ArquetipoSuporte.CURADOR, ArquetipoSuporte.POKE,
                ArquetipoSuporte.ENCANTADOR),
            comp(TipoComposicao.POKE, TipoComposicao.LUTAS_LONGAS,
                TipoComposicao.ALTA_MOBILIDADE),
            comp(TipoComposicao.POKE, TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.DIVE, TipoComposicao.EXPLOSAO),
            7, 4, 6, 8, 7, 8, 8,
            "Entrega valor por desgaste e mobilidade, mas exige execução e posicionamento precisos."
        ),
        perfil(
            "Zhang Fei",
            arq(ArquetipoSuporte.TANQUE, ArquetipoSuporte.PROTETOR,
                ArquetipoSuporte.DESENGAGE),
            comp(TipoComposicao.HIPERCARREGADOR, TipoComposicao.FRONT_TO_BACK,
                TipoComposicao.ENGAGE_AGRUPADO),
            comp(TipoComposicao.DIVE, TipoComposicao.EXPLOSAO,
                TipoComposicao.PICKOFF),
            comp(TipoComposicao.POKE, TipoComposicao.SPLIT_PUSH),
            10, 8, 10, 4, 7, 5, 4,
            "Blind pick seguro: escuda o ADC, segura dive e ainda oferece iniciação quando a fúria está pronta."
        ),
        perfil(
            "Zhuangzi",
            arq(ArquetipoSuporte.ANTI_CONTROLE, ArquetipoSuporte.DESENGAGE,
                ArquetipoSuporte.TANQUE),
            comp(TipoComposicao.FRONT_TO_BACK, TipoComposicao.LUTAS_LONGAS),
            comp(TipoComposicao.CONTROLE_PESADO, TipoComposicao.ENGAGE_AGRUPADO,
                TipoComposicao.PICKOFF),
            comp(TipoComposicao.POKE, TipoComposicao.DANO_NAO_EXISTENTE),
            8, 3, 10, 6, 5, 6, 4,
            "Counter especializado para composições de controle pesado; não deve ser escolhido apenas por dano."
        ),
        perfil(
            "Ziya",
            arq(ArquetipoSuporte.POKE, ArquetipoSuporte.CONTROLE_DE_AREA,
                ArquetipoSuporte.AMPLIFICADOR),
            comp(TipoComposicao.POKE, TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.ALVOS_IMOVEIS, TipoComposicao.FRONT_TO_BACK),
            comp(TipoComposicao.DIVE, TipoComposicao.ALTA_MOBILIDADE,
                TipoComposicao.PICKOFF),
            4, 4, 5, 2, 8, 5, 7,
            "Escala e controla corredores, porém precisa de linha de frente e proteção contra dive."
        )
    );

    private final List<SinergiaBotLane> sinergias = List.of(
        sinergia("Lian Po", "Marco Polo", 10, ConfiancaDado.ALTA,
            "O controle em área mantém alvos dentro da ultimate do Marco Polo.",
            "iniciação", "controle em área", "janela de ultimate"),
        sinergia("Lapulapu", "Marco Polo", 9, ConfiancaDado.MEDIA,
            "Absorve a resposta inimiga e cria espaço para a entrada do Marco Polo.",
            "linha de frente", "iniciação", "proteção pós-entrada"),
        sinergia("Sakeer", "Marco Polo", 9, ConfiancaDado.MEDIA,
            "Cura, visão e controle sustentam o ADC durante entradas prolongadas.",
            "sustentação", "visão", "controle"),
        sinergia("Da Qiao", "Marco Polo", 9, ConfiancaDado.MEDIA,
            "Reposicionamento e macro ampliam a mobilidade e a pressão lateral da dupla.",
            "macro", "reposicionamento", "desengage"),
        sinergia("Zhang Fei", "Marco Polo", 9, ConfiancaDado.ALTA,
            "Escudos e contra-iniciação permitem ao Marco Polo usar a ultimate com mais segurança.",
            "escudo", "peel", "contra-iniciação"),
        sinergia("Yaria", "Marco Polo", 9, ConfiancaDado.ALTA,
            "Yaria acompanha todos os deslocamentos e protege o ADC móvel sem quebrar o ritmo.",
            "mobilidade compartilhada", "escudo", "anti-assassino"),
        sinergia("Dolia", "Marco Polo", 8, ConfiancaDado.ALTA,
            "O reset da ultimate cria uma segunda condição de luta em área.",
            "reset de ultimate", "sustentação", "controle"),
        sinergia("Liu Shan", "Marco Polo", 8, ConfiancaDado.ALTA,
            "Controle em cadeia abre a luta e converte vantagem em torres.",
            "controle", "pressão de torre", "iniciação"),

        sinergia("Yaria", "Arli", 10, ConfiancaDado.ALTA,
            "Acompanha a mobilidade extrema de Arli e reduz o risco dos reposicionamentos.",
            "mobilidade compartilhada", "escudo", "duelo"),
        sinergia("Ming", "Arli", 9, ConfiancaDado.MEDIA,
            "Amplifica dano e cura uma atiradora capaz de escolher o momento do combate.",
            "amplificação", "cura", "duelo"),
        sinergia("Guiguzi", "Arli", 8, ConfiancaDado.MEDIA,
            "A iniciação furtiva entrega alvos agrupados para a mobilidade ofensiva de Arli.",
            "camuflagem", "agrupamento", "execução"),

        sinergia("Zhang Fei", "Hou Yi", 10, ConfiancaDado.ALTA,
            "Protege um ADC imóvel e encadeia iniciação com a ultimate global de Hou Yi.",
            "peel", "escudo", "controle em cadeia"),
        sinergia("Ming", "Hou Yi", 9, ConfiancaDado.MEDIA,
            "Maximiza o dano sustentado de um hipercarregador de ataques básicos.",
            "amplificação", "velocidade de ataque", "cura"),
        sinergia("Cai Yan", "Hou Yi", 9, ConfiancaDado.MEDIA,
            "Mantém o ADC imóvel vivo durante lutas front-to-back prolongadas.",
            "cura", "proteção", "luta longa"),

        sinergia("Zhang Fei", "Garo", 10, ConfiancaDado.ALTA,
            "Oferece a linha de frente e o peel necessários para Garo usar seu alcance máximo.",
            "linha de frente", "escudo", "desengage"),
        sinergia("Cai Yan", "Garo", 9, ConfiancaDado.MEDIA,
            "A cura prolonga o tempo de dano de Garo em formações front-to-back.",
            "cura", "hipercarregador", "luta longa"),
        sinergia("Ming", "Garo", 9, ConfiancaDado.MEDIA,
            "Amplifica o alcance efetivo e o DPS de uma carregadora tardia.",
            "amplificação", "dano sustentado", "proteção"),

        sinergia("Zhang Fei", "Huang Zhong", 10, ConfiancaDado.MEDIA,
            "Forma uma fortaleza ao redor da ultimate estacionária de Huang Zhong.",
            "peel", "zona defensiva", "cerco"),
        sinergia("Cai Yan", "Huang Zhong", 9, ConfiancaDado.MEDIA,
            "Cura e controle sustentam o ADC enquanto ele permanece fixo na ultimate.",
            "cura", "controle", "cerco"),
        sinergia("Zhuangzi", "Huang Zhong", 8, ConfiancaDado.MEDIA,
            "A purificação reduz a principal fraqueza do ADC estacionário: controle em cadeia.",
            "anti-controle", "proteção", "cerco"),

        sinergia("Zhang Fei", "Luban No.7", 10, ConfiancaDado.MEDIA,
            "Peel máximo para um ADC imóvel de altíssimo dano sustentado.",
            "escudo", "desengage", "linha de frente"),
        sinergia("Yaria", "Luban No.7", 9, ConfiancaDado.MEDIA,
            "Escudo e visão reduzem a vulnerabilidade de Luban a assassinos.",
            "escudo", "anti-assassino", "visão"),
        sinergia("Liu Shan", "Luban No.7", 8, ConfiancaDado.MEDIA,
            "O controle frontal permite que Luban descarregue passivas e derrube torres rapidamente.",
            "controle", "pressão de torre", "front-to-back"),

        sinergia("Da Qiao", "Di Renjie", 9, ConfiancaDado.MEDIA,
            "Une pressão segura, limpeza e macro para controlar o mapa sem expor o ADC.",
            "macro", "controle", "reposicionamento"),
        sinergia("Sun Bin", "Di Renjie", 8, ConfiancaDado.MEDIA,
            "A aceleração reforça o kite e a autossuficiência do atirador.",
            "velocidade", "kite", "desengage"),
        sinergia("Ming", "Di Renjie", 8, ConfiancaDado.MEDIA,
            "Amplifica dano sustentado e mantém pressão constante de rota.",
            "amplificação", "dano sustentado", "cura"),
        sinergia("Cai Yan", "Di Renjie", 8, ConfiancaDado.MEDIA,
            "A dupla possui muita estabilidade, controle e recuperação em trocas longas.",
            "cura", "controle", "estabilidade"),

        sinergia("Yaria", "Alessio", 9, ConfiancaDado.MEDIA,
            "Acompanha a mobilidade aérea e protege entradas agressivas.",
            "mobilidade compartilhada", "escudo", "explosão"),
        sinergia("Dolia", "Alessio", 8, ConfiancaDado.MEDIA,
            "Sustentação e reset de ultimate aumentam a pressão de finalização.",
            "reset de ultimate", "sustentação", "finalização"),
        sinergia("Annette", "Alessio", 8, ConfiancaDado.MEDIA,
            "O desengage cria espaço para Alessio reposicionar e continuar o poke.",
            "desengage", "poke", "controle de área"),

        sinergia("Ming", "Lady Sun", 9, ConfiancaDado.MEDIA,
            "Amplifica a explosão e o alcance dos ataques após o rolamento.",
            "amplificação", "explosão", "cura"),
        sinergia("Da Qiao", "Lady Sun", 8, ConfiancaDado.MEDIA,
            "Mobilidade e macro permitem pressionar torres e sair antes da resposta.",
            "macro", "mobilidade", "pressão de torre"),
        sinergia("Liu Shan", "Lady Sun", 8, ConfiancaDado.MEDIA,
            "Controle e pressão de torre combinam com a explosão de curto intervalo da atiradora.",
            "controle", "explosão", "torres"),

        sinergia("Dolia", "Loong", 10, ConfiancaDado.MEDIA,
            "Resetar uma ultimate defensiva e ofensiva de alto impacto muda duas lutas seguidas.",
            "reset de ultimate", "artilharia", "sobrevivência"),
        sinergia("Zhang Fei", "Loong", 9, ConfiancaDado.MEDIA,
            "Linha de frente e peel deixam Loong usar toda a artilharia com segurança.",
            "peel", "linha de frente", "artilharia"),
        sinergia("Da Qiao", "Loong", 8, ConfiancaDado.MEDIA,
            "Reposicionamento e controle complementam o alcance e a flexibilidade de Loong.",
            "macro", "reposicionamento", "controle"),

        sinergia("Mozi", "Shouyue", 10, ConfiancaDado.ALTA,
            "Duas ameaças de longa distância encadeiam controle e disparos de precisão.",
            "artilharia", "controle à distância", "poke"),
        sinergia("Kui", "Shouyue", 9, ConfiancaDado.MEDIA,
            "Um gancho bem-sucedido entrega um alvo parado para o tiro de precisão.",
            "pickoff", "gancho", "explosão"),
        sinergia("Da Qiao", "Shouyue", 8, ConfiancaDado.MEDIA,
            "Controle de corredores e macro ampliam a zona de ameaça do sniper.",
            "macro", "zoneamento", "artilharia"),

        sinergia("Ming", "Meng Ya", 9, ConfiancaDado.MEDIA,
            "Amplificação aumenta o volume de dano contínuo e a pressão precoce.",
            "amplificação", "dano sustentado", "pressão"),
        sinergia("Cai Yan", "Meng Ya", 8, ConfiancaDado.MEDIA,
            "Cura permite manter o avanço enquanto Meng Ya descarrega tiros.",
            "cura", "pressão", "luta longa"),
        sinergia("Sun Bin", "Meng Ya", 8, ConfiancaDado.MEDIA,
            "Velocidade ajuda o ADC a manter distância e reposicionar seu alto volume de dano.",
            "velocidade", "kite", "dano sustentado"),

        sinergia("Ming", "Erin", 8, ConfiancaDado.MEDIA,
            "Amplifica um ADC mágico móvel e dificulta a itemização defensiva inimiga.",
            "amplificação", "dano mágico", "mobilidade"),
        sinergia("Dolia", "Erin", 8, ConfiancaDado.MEDIA,
            "Sustentação e utilidade prolongam o dano mágico contínuo.",
            "sustentação", "controle", "dano sustentado"),
        sinergia("Sun Bin", "Erin", 8, ConfiancaDado.MEDIA,
            "Aceleração combina com o kite e a mobilidade natural da atiradora.",
            "velocidade", "kite", "mobilidade"),

        sinergia("Liu Shan", "Fang", 9, ConfiancaDado.MEDIA,
            "Pressão precoce, explosão e dano a torres criam uma rota de snowball.",
            "pressão de torre", "explosão", "início de jogo"),
        sinergia("Guiguzi", "Fang", 9, ConfiancaDado.MEDIA,
            "Agrupa alvos para a marca e o dano explosivo de Fang.",
            "agrupamento", "explosão", "rotação"),
        sinergia("Donghuang", "Fang", 8, ConfiancaDado.MEDIA,
            "Supressão garante tempo suficiente para aplicar e detonar a marca.",
            "supressão", "pickoff", "explosão"),

        sinergia("Yaria", "Luara", 9, ConfiancaDado.MEDIA,
            "Acompanha travessias de terreno e protege uma carregadora móvel.",
            "mobilidade compartilhada", "escudo", "fim de jogo"),
        sinergia("Ming", "Luara", 8, ConfiancaDado.MEDIA,
            "Amplifica o dano contínuo e o escalamento tardio.",
            "amplificação", "dano sustentado", "cura"),
        sinergia("Zhang Fei", "Luara", 8, ConfiancaDado.MEDIA,
            "Peel e linha de frente criam espaço para atravessar terrenos e continuar atacando.",
            "peel", "linha de frente", "mobilidade"),

        sinergia("Mozi", "Chano", 9, ConfiancaDado.MEDIA,
            "Controle e artilharia sobrepostos pressionam alvos antes da luta.",
            "artilharia", "controle", "poke"),
        sinergia("Da Qiao", "Chano", 8, ConfiancaDado.MEDIA,
            "Macro e zoneamento favorecem a iniciação e a finalização de longa distância.",
            "macro", "zoneamento", "finalização"),
        sinergia("Dolia", "Chano", 8, ConfiancaDado.MEDIA,
            "Reset de ultimate amplia a capacidade de iniciar ou finalizar duas vezes.",
            "reset de ultimate", "artilharia", "finalização"),

        sinergia("Ming", "Consorte Yu", 8, ConfiancaDado.MEDIA,
            "Amplificação complementa a proteção física e a pressão de poke.",
            "amplificação", "poke", "proteção"),
        sinergia("Da Qiao", "Consorte Yu", 8, ConfiancaDado.MEDIA,
            "A dupla controla distância, limpa rota e se reposiciona com segurança.",
            "macro", "poke", "reposicionamento"),
        sinergia("Zhang Fei", "Consorte Yu", 8, ConfiancaDado.MEDIA,
            "Proteção mista contra dive físico e controle frontal.",
            "peel", "escudo", "controle"),

        sinergia("Dyadia", "Ser do Fluxo (Atirador)", 9, ConfiancaDado.EXPLORATORIA,
            "Cura e mobilidade combinam com um ADC adaptável e agressivo.",
            "mobilidade", "cura", "escaramuça"),
        sinergia("Zhang Fei", "Ser do Fluxo (Atirador)", 8, ConfiancaDado.EXPLORATORIA,
            "Linha de frente confiável permite ao ADC adaptar sua postura sem perder proteção.",
            "peel", "linha de frente", "adaptação"),
        sinergia("Sakeer", "Ser do Fluxo (Atirador)", 8, ConfiancaDado.EXPLORATORIA,
            "Visão e sustentação favorecem lutas móveis e prolongadas.",
            "visão", "cura", "mobilidade"),

        sinergia("Sakeer", "Agu", 8, ConfiancaDado.EXPLORATORIA,
            "A dupla forma uma linha resistente com cura, visão e utilidade constante.",
            "sustentação", "visão", "utilidade"),
        sinergia("Dyadia", "Agu", 8, ConfiancaDado.EXPLORATORIA,
            "Pressão de rota e cura ampliam o valor utilitário de Agu no início.",
            "cura", "pressão", "início de jogo"),

        sinergia("Zhang Fei", "Chicha", 8, ConfiancaDado.EXPLORATORIA,
            "Peel e controle permitem que a lutadora mantenha ataques contínuos.",
            "peel", "dano sustentado", "linha de frente"),
        sinergia("Dyadia", "Chicha", 8, ConfiancaDado.EXPLORATORIA,
            "Cura e mobilidade sustentam duelos prolongados e perseguições.",
            "cura", "mobilidade", "duelo")
    );

    public List<PerfilSuporte> listarPerfis() {
        return perfis;
    }

    public List<SinergiaBotLane> listarSinergias() {
        return sinergias;
    }

    private PerfilSuporte perfil(
        String heroi,
        List<ArquetipoSuporte> arquetipos,
        List<TipoComposicao> fortalece,
        List<TipoComposicao> quebra,
        List<TipoComposicao> sofreContra,
        int protecaoCarry,
        int iniciacao,
        int desengage,
        int sustentacao,
        int pressaoRota,
        int mobilidadeMapa,
        int dependenciaCoordenacao,
        String... observacoes
    ) {
        return new PerfilSuporte(
            heroi,
            arquetipos,
            fortalece,
            quebra,
            sofreContra,
            protecaoCarry,
            iniciacao,
            desengage,
            sustentacao,
            pressaoRota,
            mobilidadeMapa,
            dependenciaCoordenacao,
            List.of(observacoes)
        );
    }

    private SinergiaBotLane sinergia(
        String suporte,
        String atirador,
        int nota,
        ConfiancaDado confianca,
        String motivo,
        String... gatilhos
    ) {
        return new SinergiaBotLane(
            suporte,
            atirador,
            nota,
            confianca,
            motivo,
            List.of(gatilhos)
        );
    }

    @SafeVarargs
    private final <T> List<T> arq(T... valores) {
        return List.of(valores);
    }

    @SafeVarargs
    private final <T> List<T> comp(T... valores) {
        return List.of(valores);
    }
}
