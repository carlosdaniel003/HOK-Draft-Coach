package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.model.TipoDano;

@Service
public class HeroiService {

    private final List<Heroi> herois = List.of(
        criarHeroi(1L, "Arli", Rota.FARM_LANE,
            "Alta mobilidade e reposicionamento", 5, TipoDano.FISICO,
            new AtributosHeroi(2, 3, 10, 6, 8, 7),
            "mobilidade", "duelo", "reposicionamento"),
        criarHeroi(2L, "Hou Yi", Rota.FARM_LANE,
            "Dano contínuo e ataques básicos", 2, TipoDano.FISICO,
            new AtributosHeroi(4, 2, 2, 8, 10, 5),
            "dano sustentado", "luta em equipe", "alcance"),
        criarHeroi(3L, "Alessio", Rota.FARM_LANE,
            "Mobilidade aérea e dano em área", 3, TipoDano.FISICO,
            new AtributosHeroi(3, 3, 7, 7, 7, 7),
            "mobilidade", "dano em área", "execução"),
        criarHeroi(4L, "Lady Sun", Rota.FARM_LANE,
            "Explosão de dano e mobilidade", 3, TipoDano.FISICO,
            new AtributosHeroi(2, 3, 7, 8, 6, 9),
            "explosão", "mobilidade", "alcance"),
        criarHeroi(5L, "Marco Polo", Rota.FARM_LANE,
            "Mobilidade e dano contínuo", 4, TipoDano.MISTO,
            new AtributosHeroi(2, 3, 9, 6, 9, 6),
            "mobilidade", "dano sustentado", "dano em área"),
        criarHeroi(6L, "Consort Yu", Rota.FARM_LANE,
            "Proteção contra dano físico e pressão de rota", 3,
            TipoDano.FISICO,
            new AtributosHeroi(5, 5, 5, 8, 7, 7),
            "proteção", "controle", "alcance"),
        criarHeroi(7L, "Garo", Rota.FARM_LANE,
            "Longo alcance e pressão no fim da partida", 3,
            TipoDano.FISICO,
            new AtributosHeroi(3, 2, 2, 10, 10, 5),
            "longo alcance", "dano sustentado", "escalamento"),

        criarHeroi(8L, "Arthur", Rota.CLASH_LANE,
            "Linha de frente simples e resistente", 1, TipoDano.MISTO,
            new AtributosHeroi(6, 9, 5, 2, 5, 5),
            "linha de frente", "silêncio", "iniciação"),
        criarHeroi(9L, "Allain", Rota.CLASH_LANE,
            "Duelista de dano contínuo", 3, TipoDano.MISTO,
            new AtributosHeroi(3, 6, 6, 2, 9, 6),
            "duelo", "dano sustentado", "sustentação"),
        criarHeroi(10L, "Dun", Rota.CLASH_LANE,
            "Tanque com controle e sustentação", 2, TipoDano.MISTO,
            new AtributosHeroi(8, 9, 4, 3, 5, 4),
            "tanque", "controle", "sustentação"),

        criarHeroi(11L, "Lam", Rota.JUNGLE,
            "Assassino móvel para eliminar alvos frágeis", 4,
            TipoDano.FISICO,
            new AtributosHeroi(3, 3, 10, 2, 6, 10),
            "assassino", "mobilidade", "execução"),
        criarHeroi(12L, "Butterfly", Rota.JUNGLE,
            "Assassina de resets e execução", 2, TipoDano.FISICO,
            new AtributosHeroi(2, 4, 7, 2, 6, 9),
            "assassino", "reset", "execução"),
        criarHeroi(13L, "Nakoruru", Rota.JUNGLE,
            "Explosão de dano e entrada rápida", 3, TipoDano.FISICO,
            new AtributosHeroi(2, 3, 9, 2, 5, 10),
            "assassino", "explosão", "mobilidade"),

        criarHeroi(14L, "Angela", Rota.MID_LANE,
            "Maga de explosão e alcance", 2, TipoDano.MAGICO,
            new AtributosHeroi(6, 2, 2, 9, 5, 10),
            "explosão", "controle", "alcance"),
        criarHeroi(15L, "Lady Zhen", Rota.MID_LANE,
            "Controle em área e zoneamento", 2, TipoDano.MAGICO,
            new AtributosHeroi(10, 3, 3, 8, 6, 7),
            "controle", "zoneamento", "dano em área"),
        criarHeroi(16L, "Mai Shiranui", Rota.MID_LANE,
            "Maga-assassina de alta mobilidade", 5, TipoDano.MAGICO,
            new AtributosHeroi(7, 2, 10, 5, 5, 10),
            "mobilidade", "explosão", "controle"),
        criarHeroiFlex(20L, "Kongming", Rota.MID_LANE,
            List.of(Rota.MID_LANE, Rota.JUNGLE),
            "Mago de mobilidade e execução", 4, TipoDano.MAGICO,
            new AtributosHeroi(4, 3, 8, 6, 7, 9),
            "flex", "mobilidade", "execução", "escalamento"),

        criarHeroi(17L, "Zhang Fei", Rota.ROAMING,
            "Proteção, iniciação e linha de frente", 2, TipoDano.FISICO,
            new AtributosHeroi(8, 10, 4, 2, 3, 3),
            "tanque", "proteção", "iniciação"),
        criarHeroi(18L, "Dolia", Rota.ROAMING,
            "Suporte utilitário para lutas prolongadas", 3,
            TipoDano.MAGICO,
            new AtributosHeroi(7, 4, 5, 7, 3, 3),
            "utilidade", "sustentação", "controle"),
        criarHeroi(19L, "Ming", Rota.ROAMING,
            "Amplificação de um aliado carregador", 3, TipoDano.MAGICO,
            new AtributosHeroi(4, 3, 5, 6, 3, 4),
            "amplificação", "proteção", "dupla")
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

    private Heroi criarHeroi(
        Long id,
        String nome,
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
            rota,
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            List.of(caracteristicas)
        );
    }

    private Heroi criarHeroiFlex(
        Long id,
        String nome,
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
            rotaPrincipal,
            rotasPossiveis,
            estilo,
            dificuldade,
            tipoDano,
            atributos,
            List.of(caracteristicas)
        );
    }
}
