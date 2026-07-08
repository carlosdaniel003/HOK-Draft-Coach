package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class HeroiService {

    private final List<Heroi> herois = List.of(
        new Heroi(
            1L,
            "Arli",
            Rota.FARM_LANE,
            "Alta mobilidade e reposicionamento",
            5
        ),
        new Heroi(
            2L,
            "Hou Yi",
            Rota.FARM_LANE,
            "Dano contínuo e ataques básicos",
            2
        ),
        new Heroi(
            3L,
            "Alessio",
            Rota.FARM_LANE,
            "Mobilidade aérea e dano em área",
            3
        ),
        new Heroi(
            4L,
            "Lady Sun",
            Rota.FARM_LANE,
            "Explosão de dano e mobilidade",
            3
        ),
        new Heroi(
            5L,
            "Marco Polo",
            Rota.FARM_LANE,
            "Mobilidade e dano verdadeiro",
            4
        )
    );

    public List<Heroi> listarTodos() {
        return herois;
    }
}