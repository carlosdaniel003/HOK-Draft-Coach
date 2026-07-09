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
public class CatalogoSuporteRepository {

    public static final String VERSAO_META = "S15-HOK-PLUS-2.0";
    public static final LocalDate DATA_META = LocalDate.of(2026, 7, 2);
    public static final String FONTE_META =
        "POCKET_GAMER_11.4.1.1+HOKSTATS+COMUNIDADE_HIGH_ELO";

    private final List<Heroi> suportes = List.of(
        suporte(
            34L,
            "Annette",
            List.of(),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Protetora de zona com forte desengage e controle à distância",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(8, 4, 6, 8, 4, 4),
            TierMeta.A,
            "desengage", "proteção", "controle de área", "poke", "anti-dive"
        ),
        suporte(
            35L,
            "Lapulapu",
            List.of("Lapu-Lapu", "Lapu Lapu"),
            ClasseHeroi.TANQUE,
            List.of(Rota.ROAMING, Rota.CLASH_LANE),
            "Tanque de iniciação e proteção com controle em área",
            2,
            TipoDano.FISICO,
            new AtributosHeroi(8, 9, 5, 3, 4, 5),
            TierMeta.B,
            "tanque", "iniciação", "proteção", "controle em área", "flex"
        ),
        suporte(
            36L,
            "Cai Yan",
            List.of("Cai Wenji"),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Curadora de rota e lutas prolongadas focada em manter carregadores vivos",
            1,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 3, 4, 6, 2, 2),
            TierMeta.A,
            "cura", "sustentação", "proteção", "hipercarregador", "lutas longas"
        ),
        suporte(
            37L,
            "Da Qiao",
            List.of(),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Suporte macro de teleporte, reposicionamento e controle de mapa",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(8, 3, 8, 9, 3, 4),
            TierMeta.S,
            "macro", "teleporte", "desengage", "controle de área", "split push"
        ),
        suporte(
            18L,
            "Dolia",
            List.of(),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Encantadora que sustenta a equipe e reinicia ultimates decisivas",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 4, 6, 7, 3, 3),
            TierMeta.S,
            "reset de ultimate", "sustentação", "amplificação", "controle", "utilidade"
        ),
        suporte(
            38L,
            "Donghuang",
            List.of("Donghuang Taiyi"),
            ClasseHeroi.TANQUE,
            List.of(Rota.ROAMING),
            "Tanque de supressão capaz de travar carregadores e mergulhadores",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(10, 9, 4, 3, 4, 5),
            TierMeta.A,
            "supressão", "pickoff", "tanque", "anti-dive", "controle inevitável"
        ),
        suporte(
            10L,
            "Dun",
            List.of("Xiahou Dun"),
            ClasseHeroi.TANQUE,
            List.of(Rota.CLASH_LANE, Rota.ROAMING),
            "Tanque flexível de controle, escudo e sustentação",
            2,
            TipoDano.MISTO,
            new AtributosHeroi(8, 9, 5, 3, 5, 4),
            TierMeta.C,
            "tanque", "controle", "sustentação", "proteção", "flex"
        ),
        suporte(
            39L,
            "Dyadia",
            List.of(),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Encantadora móvel de cura, pressão e escaramuças",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(6, 4, 8, 7, 4, 5),
            TierMeta.S,
            "cura", "mobilidade", "poke", "escaramuça", "lutas longas"
        ),
        suporte(
            40L,
            "Guiguzi",
            List.of("Gui Guzi"),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Iniciador furtivo de agrupamento e emboscada",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(9, 5, 8, 4, 3, 5),
            TierMeta.B,
            "iniciação", "camuflagem", "pickoff", "agrupamento", "rotação"
        ),
        suporte(
            41L,
            "Kui",
            List.of("Zhong Kui"),
            ClasseHeroi.TANQUE,
            List.of(Rota.ROAMING),
            "Tanque de gancho e pickoff que pune posicionamento ruim",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(9, 8, 3, 8, 3, 7),
            TierMeta.B,
            "gancho", "pickoff", "tanque", "visão", "punição de posicionamento"
        ),
        suporte(
            42L,
            "Lian Po",
            List.of(),
            ClasseHeroi.TANQUE,
            List.of(Rota.ROAMING, Rota.CLASH_LANE),
            "Tanque resistente a controle com múltiplas entradas e deslocamentos",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(10, 10, 6, 3, 4, 6),
            TierMeta.S,
            "tanque", "iniciação", "controle em área", "anti-controle", "flex"
        ),
        suporte(
            43L,
            "Liu Bang",
            List.of(),
            ClasseHeroi.TANQUE,
            List.of(Rota.CLASH_LANE, Rota.ROAMING),
            "Protetor global que transforma pressão lateral em superioridade numérica",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(6, 9, 7, 3, 4, 5),
            TierMeta.C,
            "proteção global", "macro", "split push", "escudo", "flex"
        ),
        suporte(
            44L,
            "Liu Shan",
            List.of(),
            ClasseHeroi.TANQUE,
            List.of(Rota.ROAMING),
            "Tanque de iniciação e pressão de torre com controle em cadeia",
            2,
            TipoDano.FISICO,
            new AtributosHeroi(9, 9, 6, 3, 4, 5),
            TierMeta.A,
            "iniciação", "controle", "pressão de torre", "proteção", "rotação"
        ),
        suporte(
            19L,
            "Ming",
            List.of("Ming Shiyin"),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Amplificador dedicado a potencializar um único carregador",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(4, 3, 5, 6, 3, 4),
            TierMeta.A,
            "amplificação", "proteção", "dupla", "hipercarregador", "cura"
        ),
        suporte(
            45L,
            "Mozi",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.ROAMING, Rota.MID_LANE),
            "Mago de poke, escudo e controle de longa distância",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(9, 5, 5, 9, 4, 7),
            TierMeta.S,
            "poke", "controle", "escudo", "desengage", "flex"
        ),
        suporte(
            46L,
            "Sakeer",
            List.of("Sakker"),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Suporte de visão, cura e controle para lutas prolongadas",
            4,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 5, 7, 7, 3, 4),
            TierMeta.A,
            "cura", "visão", "controle", "mobilidade", "lutas longas"
        ),
        suporte(
            47L,
            "Sun Bin",
            List.of(),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Suporte de aceleração, desaceleração e controle de ritmo",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 3, 9, 8, 4, 4),
            TierMeta.A,
            "velocidade", "desengage", "controle de ritmo", "poke", "rotação"
        ),
        suporte(
            48L,
            "Xiang Yu",
            List.of(),
            ClasseHeroi.TANQUE,
            List.of(Rota.CLASH_LANE, Rota.ROAMING),
            "Tanque de deslocamento e proteção contra entradas corpo a corpo",
            2,
            TipoDano.FISICO,
            new AtributosHeroi(8, 10, 5, 3, 4, 6),
            TierMeta.C,
            "tanque", "deslocamento", "proteção", "anti-dive", "flex"
        ),
        suporte(
            49L,
            "Yaria",
            List.of("Yao"),
            ClasseHeroi.SUPORTE,
            List.of(Rota.ROAMING),
            "Encantadora de escudo que acompanha carregadores móveis",
            2,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 3, 8, 6, 2, 3),
            TierMeta.S,
            "escudo", "proteção", "visão", "dupla", "anti-assassino"
        ),
        suporte(
            50L,
            "Yuhuan",
            List.of(),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE, Rota.ROAMING),
            "Maga de sustentação, poke e controle de ritmo",
            5,
            TipoDano.MAGICO,
            new AtributosHeroi(5, 4, 7, 8, 5, 6),
            TierMeta.C,
            "cura", "poke", "mobilidade", "tempo", "flex"
        ),
        suporte(
            17L,
            "Zhang Fei",
            List.of(),
            ClasseHeroi.TANQUE,
            List.of(Rota.ROAMING),
            "Guardião de carregador com escudos, linha de frente e contra-iniciação",
            2,
            TipoDano.FISICO,
            new AtributosHeroi(9, 10, 5, 3, 3, 4),
            TierMeta.S,
            "tanque", "proteção", "escudo", "iniciação", "desengage"
        ),
        suporte(
            51L,
            "Zhuangzi",
            List.of("Zhuang Zhou"),
            ClasseHeroi.TANQUE,
            List.of(Rota.ROAMING),
            "Suporte anti-controle que limpa efeitos e protege a formação",
            2,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 9, 6, 4, 4, 4),
            TierMeta.A,
            "anti-controle", "purificação", "desengage", "tanque", "proteção"
        ),
        suporte(
            52L,
            "Ziya",
            List.of("Jiang Ziya"),
            ClasseHeroi.MAGO,
            List.of(Rota.MID_LANE, Rota.ROAMING),
            "Mago de poke, zoneamento e escalamento de equipe",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 2, 3, 10, 4, 8),
            TierMeta.C,
            "poke", "zoneamento", "alcance", "escalamento", "flex"
        )
    );

    public List<Heroi> listarTodos() {
        return suportes;
    }

    public Optional<Heroi> buscarPorNome(String nome) {
        return suportes.stream()
            .filter(heroi -> heroi.correspondeAoNome(nome))
            .findFirst();
    }

    public Optional<Heroi> buscarPorId(Long id) {
        return suportes.stream()
            .filter(heroi -> heroi.getId().equals(id))
            .findFirst();
    }

    private Heroi suporte(
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
