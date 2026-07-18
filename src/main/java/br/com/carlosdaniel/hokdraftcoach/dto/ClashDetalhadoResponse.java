package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoClashLane;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilClashLane;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaClashEquipe;

public record ClashDetalhadoResponse(
    Heroi heroi,
    PerfilClashLane perfil,
    List<ConfrontoClashLane> vantagens,
    List<ConfrontoClashLane> desvantagens,
    List<SinergiaClashEquipe> melhoresCombos
) {
}
