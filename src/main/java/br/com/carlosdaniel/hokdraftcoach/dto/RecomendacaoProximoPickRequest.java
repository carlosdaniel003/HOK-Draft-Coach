package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    @NotNull @Size(max = 5) List<Rota> funcoesPreferidas,
    @Valid @NotNull @Size(max = 5) List<FuncaoSlotRequest> funcoesAliadas
) {

    public RecomendacaoProximoPickRequest {
        funcoesPreferidas = funcoesPreferidas == null
            ? List.of()
            : List.copyOf(funcoesPreferidas);
        funcoesAliadas = funcoesAliadas == null
            ? List.of()
            : List.copyOf(funcoesAliadas);
    }

    public RecomendacaoProximoPickRequest(
        LadoDraft meuLado,
        Integer minhaOrdem,
        List<Long> bansAzul,
        List<Long> bansVermelho,
        List<PickSemFuncaoRequest> picksAzul,
        List<PickSemFuncaoRequest> picksVermelho,
        List<Rota> funcoesPreferidas
    ) {
        this(
            meuLado,
            minhaOrdem,
            bansAzul,
            bansVermelho,
            picksAzul,
            picksVermelho,
            funcoesPreferidas,
            List.of()
        );
    }

    public List<Rota> funcoesDaOrdem(Integer ordem) {
        if (ordem == null) {
            return List.of();
        }
        if (ordem.equals(minhaOrdem) && !funcoesPreferidas.isEmpty()) {
            return funcoesPreferidas;
        }
        return funcoesAliadas.stream()
            .filter(item -> ordem.equals(item.ordem()))
            .map(FuncaoSlotRequest::funcao)
            .distinct()
            .toList();
    }

    public List<FuncaoSlotRequest> funcoesDaEquipe() {
        Map<Integer, Rota> porOrdem = new TreeMap<>();
        funcoesAliadas.stream()
            .filter(item -> item != null && item.ordem() != null && item.funcao() != null)
            .forEach(item -> porOrdem.put(item.ordem(), item.funcao()));
        if (
            minhaOrdem != null
                && funcoesPreferidas.size() == 1
        ) {
            porOrdem.put(minhaOrdem, funcoesPreferidas.getFirst());
        }
        return porOrdem.entrySet().stream()
            .map(item -> new FuncaoSlotRequest(item.getKey(), item.getValue()))
            .toList();
    }
}
