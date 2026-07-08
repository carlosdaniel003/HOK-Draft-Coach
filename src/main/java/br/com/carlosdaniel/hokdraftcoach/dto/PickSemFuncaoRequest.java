package br.com.carlosdaniel.hokdraftcoach.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PickSemFuncaoRequest(
    @NotNull @Min(1) @Max(5) Integer ordem,
    @NotNull @Positive Long heroiId
) {
}
