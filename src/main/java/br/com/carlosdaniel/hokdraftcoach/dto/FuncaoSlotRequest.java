package br.com.carlosdaniel.hokdraftcoach.dto;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FuncaoSlotRequest(
    @NotNull @Min(1) @Max(5) Integer ordem,
    @NotNull Rota funcao
) {
}
