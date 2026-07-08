package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InferenciaFuncoesRequest(
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksAzul,
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksVermelho
) {
}
