package br.com.carlosdaniel.hokdraftcoach.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DadosMetaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TierMeta;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;

@Service
public class HeroiService {

    private static final String VERSAO_META_FARM = "S15-HOK-PLUS-2.0";
    private static final LocalDate DATA_META_FARM = LocalDate.of(2026, 7, 2);
    private static final String FONTE_META_FARM = "HOKSTATS_TIER_LIST";

    private final List<Heroi> herois = List.of(
        criarHeroiFarm(
            1L,
            "Arli",
            List.of("Gongsun Li"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atiradora de altíssima mobilidade e reposicionamento",
            5,
            TipoDano.FISICO,
            new AtributosHeroi(2, 3, 10, 6, 8, 7),
            TierMeta.B,
            "mobilidade", "duelo", "reposicionamento", "limpeza"
        ),
        criarHeroiFarm(
            2L,
            "Hou Yi",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador de dano contínuo com controle global",
            1,
            TipoDano.FISICO,
            new AtributosHeroi(6, 2, 2, 8, 10, 5),
            TierMeta.A,
            "dano sustentado", "controle", "iniciação", "fim de jogo"
        ),
        criarHeroiFarm(
            3L,
            "Alessio",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador móvel de pressão à distância e dano em área",
            4,
            TipoDano.FISICO,
            new AtributosHeroi(3, 3, 7, 8, 7, 8),
            TierMeta.B,
            "mobilidade aérea", "poke", "dano em área", "cerco"
        ),
        criarHeroiFarm(
            4L,
            "Lady Sun",
            List.of("Sun Shangxiang"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atiradora de explosão, mobilidade e alcance ampliado",
            4,
            TipoDano.FISICO,
            new AtributosHeroi(2, 3, 7, 8, 6, 9),
            TierMeta.A,
            "explosão", "mobilidade", "alcance", "limpeza"
        ),
        criarHeroiFarm(
            5L,
            "Marco Polo",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador móvel de dano contínuo e dano verdadeiro",
            4,
            TipoDano.MISTO,
            new AtributosHeroi(2, 3, 9, 6, 9, 7),
            TierMeta.A,
            "mobilidade", "dano sustentado", "dano em área", "anti-tanque"
        ),
        criarHeroiFarm(
            6L,
            "Consorte Yu",
            List.of("Consort Yu", "Yu Ji"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atiradora de pressão à distância e proteção física",
            4,
            TipoDano.FISICO,
            new AtributosHeroi(5, 5, 5, 8, 7, 7),
            TierMeta.A,
            "proteção física", "poke", "controle", "alcance"
        ),
        criarHeroiFarm(
            7L,
            "Garo",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atiradora de alcance extremo e escalamento tardio",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(3, 2, 2, 10, 10, 5),
            TierMeta.A,
            "longo alcance", "dano sustentado", "poke", "fim de jogo"
        ),
        criarHeroiFarm(
            21L,
            "Luara",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atiradora de dano contínuo capaz de atravessar terreno",
            4,
            TipoDano.FISICO,
            new AtributosHeroi(3, 3, 7, 7, 9, 6),
            TierMeta.A,
            "dano sustentado", "travessia de terreno", "carregadora", "fim de jogo"
        ),
        criarHeroiFarm(
            22L,
            "Agu",
            List.of("Agudo"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.JUNGLE, Rota.FARM_LANE),
            "Atiradora resistente de utilidade e pressão no início",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(6, 8, 5, 7, 6, 4),
            TierMeta.C,
            "flex", "resistência", "poke", "buffs de equipe", "início de jogo"
        ),
        criarHeroiFarm(
            23L,
            "Loong",
            List.of("Ao'yin", "Aoyin", "Ao Yin"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador de artilharia com controle, recuperação e escalamento",
            4,
            TipoDano.FISICO,
            new AtributosHeroi(7, 4, 7, 8, 8, 8),
            TierMeta.S,
            "controle", "recuperação", "artilharia", "fim de jogo"
        ),
        criarHeroiFarm(
            24L,
            "Chano",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE, Rota.JUNGLE),
            "Atirador de artilharia com iniciação e finalização",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(5, 3, 5, 9, 8, 7),
            TierMeta.B,
            "flex", "artilharia", "iniciação", "limpeza", "fim de jogo"
        ),
        criarHeroiFarm(
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
        ),
        criarHeroiFarm(
            26L,
            "Di Renjie",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador acessível de poke, controle e dano contínuo",
            1,
            TipoDano.FISICO,
            new AtributosHeroi(6, 4, 4, 8, 9, 5),
            TierMeta.B,
            "dano sustentado", "poke", "controle", "purificação"
        ),
        criarHeroiFarm(
            27L,
            "Erin",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atiradora mágica móvel de dano sustentado",
            3,
            TipoDano.MAGICO,
            new AtributosHeroi(3, 3, 7, 7, 9, 6),
            TierMeta.A,
            "dano mágico", "mobilidade", "dano sustentado", "meio de jogo"
        ),
        criarHeroiFarm(
            28L,
            "Fang",
            List.of("Li Yuanfang"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE, Rota.JUNGLE),
            "Atirador flex de explosão e pressão no início",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(3, 3, 8, 7, 7, 9),
            TierMeta.B,
            "flex", "explosão", "mobilidade", "limpeza", "início de jogo"
        ),
        criarHeroiFarm(
            29L,
            "Ser do Fluxo (Atirador)",
            List.of("Flowborn (Marksman)", "Flowborn Atirador"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador adaptável de mobilidade e dano contínuo",
            1,
            TipoDano.FISICO,
            new AtributosHeroi(3, 3, 8, 7, 9, 6),
            TierMeta.B,
            "mobilidade", "dano sustentado", "adaptação", "fim de jogo"
        ),
        criarHeroiFarm(
            30L,
            "Huang Zhong",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador de cerco com alcance extremo e dano tardio",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(4, 4, 3, 10, 10, 7),
            TierMeta.C,
            "cerco", "longo alcance", "dano sustentado", "fim de jogo"
        ),
        criarHeroiFarm(
            31L,
            "Luban No.7",
            List.of("Luban", "Luban Nº 7"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador de dano contínuo e pressão à distância",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(4, 2, 2, 9, 10, 7),
            TierMeta.A,
            "dano sustentado", "poke", "dano em área", "fim de jogo"
        ),
        criarHeroiFarm(
            32L,
            "Meng Ya",
            List.of(),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador de pressão precoce e alto volume de disparos",
            3,
            TipoDano.FISICO,
            new AtributosHeroi(3, 3, 5, 8, 10, 7),
            TierMeta.C,
            "dano sustentado", "poke", "pressão", "início de jogo"
        ),
        criarHeroiFarm(
            33L,
            "Shouyue",
            List.of("Baili Shouyue"),
            ClasseHeroi.ATIRADOR,
            List.of(Rota.FARM_LANE),
            "Atirador-assassino de artilharia e precisão extrema",
            4,
            TipoDano.FISICO,
            new AtributosHeroi(3, 2, 6, 10, 5, 10),
            TierMeta.B,
            "artilharia", "poke", "explosão", "visão", "assassino"
        ),

        criarHeroi(
            8L, "Arthur", ClasseHeroi.LUTADOR, Rota.CLASH_LANE,
            "Linha de frente simples e resistente", 1, TipoDano.MISTO,
            new AtributosHeroi(6, 9, 5, 2, 5, 5),
            "linha de frente", "silêncio", "iniciação"
        ),
        criarHeroi(
            9L, "Allain", ClasseHeroi.LUTADOR, Rota.CLASH_LANE,
            "Duelista de dano contínuo", 3, TipoDano.MISTO,
            new AtributosHeroi(3, 6, 6, 2, 9, 6),
            "duelo", "dano sustentado", "sustentação"
        ),
        criarHeroi(
            10L, "Dun", ClasseHeroi.TANQUE, Rota.CLASH_LANE,
            "Tanque com controle e sustentação", 2, TipoDano.MISTO,
            new AtributosHeroi(8, 9, 4, 3, 5, 4),
            "tanque", "controle", "sustentação"
        ),
        criarHeroi(
            11L, "Lam", ClasseHeroi.ASSASSINO, Rota.JUNGLE,
            "Assassino móvel para eliminar alvos frágeis", 4,
            TipoDano.FISICO,
            new AtributosHeroi(3, 3, 10, 2, 6, 10),
            "assassino", "mobilidade", "execução"
        ),
        criarHeroi(
            12L, "Butterfly", ClasseHeroi.ASSASSINO, Rota.JUNGLE,
            "Assassina de resets e execução", 2, TipoDano.FISICO,
            new AtributosHeroi(2, 4, 7, 2, 6, 9),
            "assassino", "reset", "execução"
        ),
        criarHeroi(
            13L, "Nakoruru", ClasseHeroi.ASSASSINO, Rota.JUNGLE,
            "Explosão de dano e entrada rápida", 3, TipoDano.FISICO,
            new AtributosHeroi(2, 3, 9, 2, 5, 10),
            "assassino", "explosão", "mobilidade"
        ),
        criarHeroi(
            14L, "Angela", ClasseHeroi.MAGO, Rota.MID_LANE,
            "Maga de explosão e alcance", 2, TipoDano.MAGICO,
            new AtributosHeroi(6, 2, 2, 9, 5, 10),
            "explosão", "controle", "alcance"
        ),
        criarHeroi(
            15L, "Lady Zhen", ClasseHeroi.MAGO, Rota.MID_LANE,
            "Controle em área e zoneamento", 2, TipoDano.MAGICO,
            new AtributosHeroi(10, 3, 3, 8, 6, 7),
            "controle", "zoneamento", "dano em área"
        ),
        criarHeroi(
            16L, "Mai Shiranui", ClasseHeroi.MAGO, Rota.MID_LANE,
            "Maga-assassina de alta mobilidade", 5, TipoDano.MAGICO,
            new AtributosHeroi(7, 2, 10, 5, 5, 10),
            "mobilidade", "explosão", "controle"
        ),
        criarHeroiFlex(
            20L, "Kongming", List.of(), ClasseHeroi.MAGO,
            Rota.MID_LANE, List.of(Rota.MID_LANE, Rota.JUNGLE),
            "Mago de mobilidade e execução", 4, TipoDano.MAGICO,
            new AtributosHeroi(4, 3, 8, 6, 7, 9),
            "flex", "mobilidade", "execução", "escalamento"
        ),
        criarHeroi(
            17L, "Zhang Fei", ClasseHeroi.TANQUE, Rota.ROAMING,
            "Proteção, iniciação e linha de frente", 2, TipoDano.FISICO,
            new AtributosHeroi(8, 10, 4, 2, 3, 3),
            "tanque", "proteção", "iniciação"
        ),
        criarHeroi(
            18L, "Dolia", ClasseHeroi.SUPORTE, Rota.ROAMING,
            "Suporte utilitário para lutas prolongadas", 3,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 4, 5, 7, 3, 3),
            "utilidade", "sustentação", "controle"
        ),
        criarHeroi(
            19L, "Ming", ClasseHeroi.SUPORTE, Rota.ROAMING,
            "Amplificação de um aliado carregador", 3, TipoDano.MAGICO,
            new AtributosHeroi(4, 3, 5, 6, 3, 4),
            "amplificação", "proteção", "dupla"
        )
    );

    public List<Heroi> listarTodos() {
        return herois;
    }

    public List<Heroi> listarPorRota(Rota rota) {
        return herois.stream()
            .filter(heroi -> heroi.podeJogarNaRota(rota))
            .toList();
    }

    public Optional<Heroi> buscarPorId(Long id) {
        return herois.stream()
            .filter(heroi -> heroi.getId().equals(id))
            .findFirst();
    }

    public Optional<Heroi> buscarPorNome(String nome) {
        return herois.stream()
            .filter(heroi -> heroi.correspondeAoNome(nome))
            .findFirst();
    }

    private Heroi criarHeroiFarm(
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
                VERSAO_META_FARM,
                DATA_META_FARM,
                FONTE_META_FARM,
                false
            )
        );
    }

    private Heroi criarHeroi(
        Long id,
        String nome,
        ClasseHeroi classe,
        Rota rota,
        String estilo,
        int dificuldade,
        TipoDano tipoDano,
        AtributosHeroi atributos,
        String... caracteristicas
    ) {
        return new Heroi(
            id,
            nome,
            List.of(),
            classe,
            rota,
            List.of(rota),
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            List.of(caracteristicas),
            DadosMetaHeroi.naoClassificado()
        );
    }

    private Heroi criarHeroiFlex(
        Long id,
        String nome,
        List<String> aliases,
        ClasseHeroi classe,
        Rota rotaPrincipal,
        List<Rota> rotasPossiveis,
        String estilo,
        int dificuldade,
        TipoDano tipoDano,
        AtributosHeroi atributos,
        String... caracteristicas
    ) {
        return new Heroi(
            id,
            nome,
            aliases,
            classe,
            rotaPrincipal,
            rotasPossiveis,
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            List.of(caracteristicas),
            DadosMetaHeroi.naoClassificado()
        );
    }
}
