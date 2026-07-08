package br.com.carlosdaniel.hokdraftcoach.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PickSemFuncaoRequest(
    @NotNull @Positive Long heroiId
) {
}
