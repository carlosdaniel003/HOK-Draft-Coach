package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilSuporte;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaBotLane;

public record SuporteDetalhadoResponse(
    Heroi heroi,
    PerfilSuporte perfil,
    List<SinergiaBotLane> melhoresDuplas
) {
}
