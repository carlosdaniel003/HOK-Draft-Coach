package br.com.carlosdaniel.hokdraftcoach.dto;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EscolhaDraftRequest(
    @NotNull Rota rota,
    @NotNull @Positive Long heroiId
) {
}
