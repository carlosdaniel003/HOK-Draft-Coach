package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoMidLane;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilMidLane;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaMidEquipe;

public record MidDetalhadoResponse(
    Heroi heroi,
    PerfilMidLane perfil,
    List<ConfrontoMidLane> vantagens,
    List<ConfrontoMidLane> desvantagens,
    List<SinergiaMidEquipe> melhoresCombos
) {
}
