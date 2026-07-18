package br.com.carlosdaniel.hokdraftcoach.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DadosMetaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;

@Repository
public class CatalogoMidRepository {

    public static final String VERSAO_META = "S15-HOK-PLUS-2.0";
    public static final LocalDate DATA_META = LocalDate.of(2026, 7, 2);
    public static final String FONTE_META = "HOKSTATS_TIER_LIST";

    private final List<Heroi> mids = List.of(
        mid(
            53L,
            "Ser do Fluxo (Mago)",
            List.of("Flowborn (Mage)", "Flowborn Mage"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago adaptável que alterna utilidade, controle e dano conforme a partida",
            2,
            TipoDano.MAGICO,
            new AtributosHeroi(6, 4, 6, 7, 6, 7),
            TierMeta.C,
            "adaptação", "controle", "poke", "utilidade", "flexibilidade"
        ),
        mid(
            54L,
            "Garuda",
            List.of("Garuda Khageswara"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Maga de poke móvel que acelera a equipe e controla distância",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 3, 7, 9, 6, 7),
            TierMeta.B,
            "poke", "velocidade", "alcance", "rotação", "desgaste"
        ),
        mid(
            55L,
            "Lorion",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago de posicionamento de orbe, explosão e controle em área",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(9, 4, 8, 8, 7, 9),
            TierMeta.S,
            "orbe", "controle em área", "explosão", "objetivos", "mobilidade"
        ),
        mid(
            14L,
            "Angela",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Maga de artilharia e explosão capaz de eliminar alvos imóveis",
            2,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 3, 3, 9, 5, 10),
            TierMeta.S,
            "explosão", "artilharia", "escudo", "controle", "alvos imóveis"
        ),
        mid(
            37L,
            "Da Qiao",
            List.of(),
            ClasseHeroi.HIBRIDO,
            List.of(Rota.ROAMING, Rota.MID_LANE),
            "Maga-suporte de macro, teleporte, wave control e reposicionamento",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(8, 3, 8, 9, 4, 5),
            TierMeta.B,
            "macro", "teleporte", "wave clear", "rotação", "split push", "flex"
        ),
        mid(
            56L,
            "Daji",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Maga de pickoff com controle garantido e explosão em alvo único",
            1,
            TipoDano.MAGICO,
            new AtributosHeroi(8, 2, 4, 7, 3, 10),
            TierMeta.S,
            "pickoff", "explosão", "controle garantido", "alvo único", "rotação"
        ),
        mid(
            57L,
            "Diaochan",
            List.of("Diao Chan"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE, Rota.CLASH_LANE),
            "Maga de batalha móvel para lutas prolongadas dentro da própria zona",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(6, 5, 9, 5, 10, 7),
            TierMeta.B,
            "mago de batalha", "mobilidade", "dano sustentado", "cura", "anti-tanque", "flex"
        ),
        mid(
            58L,
            "Dr Bian Qe",
            List.of("Dr Bian", "Bian Que", "Bian Qe"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago de veneno e cura que vence trocas e lutas longas por acúmulo",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(4, 5, 3, 7, 9, 5),
            TierMeta.C,
            "veneno", "cura", "dano sustentado", "lutas longas", "anti-sustain"
        ),
        mid(
            59L,
            "Gan & Mo",
            List.of("Gan and Mo", "Gan Mo"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago de artilharia e precisão com explosão extrema à distância",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(3, 2, 3, 10, 5, 10),
            TierMeta.C,
            "artilharia", "precisão", "explosão", "longo alcance", "poke"
        ),
        mid(
            60L,
            "Gao Jianli",
            List.of("Gao"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago de batalha que mergulha na formação inimiga com dano em área",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 6, 7, 4, 9, 8),
            TierMeta.C,
            "mago de batalha", "dano em área", "dive", "lutas agrupadas", "sustentação"
        ),
        mid(
            61L,
            "Haya",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Maga móvel de dano consistente, poke e rotações rápidas",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 3, 8, 8, 8, 7),
            TierMeta.A,
            "mobilidade", "poke", "rotação", "dano consistente", "desgaste"
        ),
        mid(
            62L,
            "Heino",
            List.of(),
            ClasseHeroi.HIBRIDO,
            List.of(Rota.MID_LANE, Rota.CLASH_LANE),
            "Mago flexível de alcance, sustentação e manipulação temporal",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(6, 6, 7, 9, 8, 7),
            TierMeta.B,
            "flex", "alcance", "sustentação", "tempo", "poke", "escalamento"
        ),
        mid(
            20L,
            "Kongming",
            List.of("Zhuge Liang", "Kong Ming"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE, Rota.JUNGLE),
            "Mago-assassino móvel de execução e resets",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(4, 3, 9, 6, 7, 10),
            TierMeta.A,
            "flex", "mobilidade", "execução", "reset", "explosão", "snowball"
        ),
        mid(
            15L,
            "Lady Zhen",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Controladora de área com congelamento e grande impacto em lutas agrupadas",
            2,
            TipoDano.MAGICO,
            new AtributosHeroi(10, 3, 3, 8, 7, 7),
            TierMeta.B,
            "controle", "congelamento", "zoneamento", "dano em área", "anti-dive"
        ),
        mid(
            63L,
            "Liang",
            List.of("Zhang Liang"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago utilitário de supressão e controle contra ameaças móveis",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(10, 3, 3, 8, 5, 6),
            TierMeta.S,
            "supressão", "anti-mobilidade", "controle", "pickoff", "utilidade"
        ),
        mid(
            16L,
            "Mai Shiranui",
            List.of(),
            ClasseHeroi.HIBRIDO,
            List.of(Rota.MID_LANE),
            "Maga-assassina de mobilidade extrema, poke e explosão",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(8, 2, 10, 7, 5, 10),
            TierMeta.B,
            "assassino mágico", "mobilidade", "explosão", "poke", "controle"
        ),
        mid(
            64L,
            "Milady",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Maga de pressão de torre, invocações e controle de ondas",
            2,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 3, 3, 8, 8, 7),
            TierMeta.S,
            "pressão de torre", "invocações", "wave clear", "cerco", "controle de mapa"
        ),
        mid(
            45L,
            "Mozi",
            List.of(),
            ClasseHeroi.HIBRIDO,
            List.of(Rota.MID_LANE, Rota.ROAMING),
            "Mago utilitário de poke, escudo e controle de longa distância",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(10, 5, 5, 10, 5, 8),
            TierMeta.A,
            "flex", "poke", "controle", "escudo", "artilharia", "anti-dive"
        ),
        mid(
            65L,
            "Nuwa",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Maga de alcance global, criação de terreno e controle de corredores",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(8, 2, 5, 10, 6, 9),
            TierMeta.B,
            "alcance global", "zoneamento", "terreno", "poke", "wave clear", "cerco"
        ),
        mid(
            66L,
            "Shangguan",
            List.of("Shangguan Wan'er", "Shangguan Waner"),
            ClasseHeroi.HIBRIDO,
            List.of(Rota.MID_LANE),
            "Maga-assassina de mergulho e explosão contra a retaguarda",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(4, 2, 10, 6, 5, 10),
            TierMeta.C,
            "assassino mágico", "dive", "mobilidade", "explosão", "backline"
        ),
        mid(
            67L,
            "Shi",
            List.of("Xi Shi"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Controladora de pickoff que reposiciona inimigos e cria eliminações",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(10, 2, 5, 9, 4, 6),
            TierMeta.C,
            "pickoff", "reposicionamento inimigo", "controle", "setup", "alcance"
        ),
        mid(
            68L,
            "Sima Yi",
            List.of(),
            ClasseHeroi.HIBRIDO,
            List.of(Rota.MID_LANE, Rota.JUNGLE),
            "Assassino mágico de silêncio, acesso global e explosão",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 3, 10, 5, 6, 10),
            TierMeta.B,
            "flex", "assassino mágico", "silêncio", "acesso global", "explosão", "anti-mago"
        ),
        mid(
            69L,
            "Wang Zhaojun",
            List.of("Princesa Gélida", "Princess Frost"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Controladora de zona com congelamento e domínio de objetivos",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(10, 3, 3, 9, 7, 8),
            TierMeta.A,
            "congelamento", "zoneamento", "objetivos", "controle em área", "anti-engage"
        ),
        mid(
            70L,
            "Xiao Qiao",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Maga de poke, controle e explosão em área com bom alcance",
            2,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 2, 5, 9, 6, 9),
            TierMeta.A,
            "poke", "explosão", "dano em área", "controle", "alcance"
        ),
        mid(
            71L,
            "Yixing",
            List.of("Yi Xing"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago estratégico de zoneamento, tabuleiro e controle de área",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(10, 4, 4, 9, 7, 7),
            TierMeta.B,
            "zoneamento", "controle de área", "objetivos", "armadilha", "lutas agrupadas"
        ),
        mid(
            72L,
            "Zhou Yu",
            List.of("Zhou You"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE),
            "Mago de fogo, pressão de rota e controle persistente de território",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 3, 4, 8, 9, 6),
            TierMeta.C,
            "zoneamento", "dano sustentado", "wave clear", "pressão de torre", "objetivos"
        ),
        mid(
            52L,
            "Ziya",
            List.of("Jiang Ziya"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE, Rota.ROAMING),
            "Mago de poke, zoneamento e escalamento da equipe",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 2, 3, 10, 5, 8),
            TierMeta.B,
            "flex", "poke", "zoneamento", "alcance", "escalamento", "wave clear"
        ),
        mid(
            50L,
            "Yuhuan",
            List.of("Yang Yuhuan"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE, Rota.ROAMING),
            "Maga flex de sustentação, poke e controle de ritmo",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 4, 7, 8, 6, 6),
            TierMeta.C,
            "flex", "cura", "poke", "mobilidade", "tempo", "lutas longas"
        )
    );

    public List<Heroi> listarTodos() {
        return mids;
    }

    public Optional<Heroi> buscarPorNome(String nome) {
        return mids.stream()
            .filter(heroi -> heroi.correspondeAoNome(nome))
            .findFirst();
    }

    public Optional<Heroi> buscarPorId(Long id) {
        return mids.stream()
            .filter(heroi -> heroi.getId().equals(id))
            .findFirst();
    }

    private Heroi mid(
        Long id,
        String nome,
        List<String> aliases,
        ClasseHeroi classe,
        List<Rota> rotasPossiveis,
        String estilo,
        int dificuldade,
        TipoDano tipoDano,
        AtributosHeroi atributos,
        TierMeta tier,
        String... caracteristicas
    ) {
        return new Heroi(
            id,
            nome,
            aliases,
            classe,
            rotasPossiveis.getFirst(),
            rotasPossiveis,
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            List.of(caracteristicas),
            new DadosMetaHeroi(
                tier,
                VERSAO_META,
                DATA_META,
                FONTE_META,
                false
            )
        );
    }
}
