package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InferenciaFuncoesRequest(
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksAzul,
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksVermelho,
    @Valid @NotNull @Size(max = 5) List<FuncaoSlotRequest> funcoesAzul,
    @Valid @NotNull @Size(max = 5) List<FuncaoSlotRequest> funcoesVermelho
) {

    public InferenciaFuncoesRequest {
        funcoesAzul = funcoesAzul == null ? List.of() : List.copyOf(funcoesAzul);
        funcoesVermelho = funcoesVermelho == null
            ? List.of()
            : List.copyOf(funcoesVermelho);
    }

    public InferenciaFuncoesRequest(
        List<PickSemFuncaoRequest> picksAzul,
        List<PickSemFuncaoRequest> picksVermelho
    ) {
        this(picksAzul, picksVermelho, List.of(), List.of());
    }
}
