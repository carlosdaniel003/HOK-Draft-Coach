package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoJungle;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilJungle;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaJungleEquipe;

public record JungleDetalhadoResponse(
    Heroi heroi,
    PerfilJungle perfil,
    List<ConfrontoJungle> vantagens,
    List<ConfrontoJungle> desvantagens,
    List<SinergiaJungleEquipe> melhoresCombos
) {
}
