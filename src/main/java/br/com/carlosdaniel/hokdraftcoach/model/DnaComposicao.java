package br.com.carlosdaniel.hokdraftcoach.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record DnaComposicao(
    List<String> herois,
    Map<DimensaoEstrategica, Integer> vetor,
    Map<DimensaoEstrategica, NivelDna> niveis,
    DistribuicaoDano distribuicaoDano,
    int quantidadeTanques,
    int quantidadeCarregadoresFrageis,
    List<TipoComposicao> arquetipos
) {

    public DnaComposicao {
        herois = List.copyOf(herois);
        arquetipos = List.copyOf(arquetipos);

        EnumMap<DimensaoEstrategica, Integer> vetorCopia =
            new EnumMap<>(DimensaoEstrategica.class);
        vetorCopia.putAll(vetor);
        vetor = Map.copyOf(vetorCopia);

        EnumMap<DimensaoEstrategica, NivelDna> niveisCopia =
            new EnumMap<>(DimensaoEstrategica.class);
        niveisCopia.putAll(niveis);
        niveis = Map.copyOf(niveisCopia);
    }

    public int valor(DimensaoEstrategica dimensao) {
        return vetor.getOrDefault(dimensao, 0);
    }
}
