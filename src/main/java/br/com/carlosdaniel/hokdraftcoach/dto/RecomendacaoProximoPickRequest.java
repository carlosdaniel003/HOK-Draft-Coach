package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecomendacaoProximoPickRequest(
    LadoDraft meuLado,
    @Min(1) @Max(5) Integer minhaOrdem,
    @NotNull @Size(max = 3) List<Long> bansAzul,
    @NotNull @Size(max = 3) List<Long> bansVermelho,
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksAzul,
    @Valid @NotNull @Size(max = 5) List<PickSemFuncaoRequest> picksVermelho,
    @NotNull @Size(max = 5) List<Rota> funcoesPreferidas
) {
}
