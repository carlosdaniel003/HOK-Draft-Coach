package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DraftRequest(
    @NotNull Rota rotaAlvo,
    @Valid @NotNull @Size(max = 5) List<EscolhaDraftRequest> aliados,
    @Valid @NotNull @Size(max = 5) List<EscolhaDraftRequest> inimigos
) {
}
