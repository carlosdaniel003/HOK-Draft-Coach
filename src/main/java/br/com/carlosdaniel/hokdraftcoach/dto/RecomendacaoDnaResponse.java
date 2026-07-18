package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record RecomendacaoDnaResponse(
    String heroi,
    Rota rota,
    int pontuacao,
    List<DimensaoEstrategica> corrige,
    List<DimensaoEstrategica> explora,
    int dependenciaRecursos,
    int ajusteEconomico,
    int penalidadeRedundancia,
    int ajusteTemporal,
    int bonusSinergiaGrupo,
    int penalidadeAntiSinergia,
    int bonusRespostaAmeaca,
    List<String> alvosAmeacaRespondidos,
    List<String> motivos,
    List<String> alertas
) {

    public RecomendacaoDnaResponse {
        corrige = List.copyOf(corrige);
        explora = List.copyOf(explora);
        alvosAmeacaRespondidos = List.copyOf(alvosAmeacaRespondidos);
        motivos = List.copyOf(motivos);
        alertas = List.copyOf(alertas);
    }

    public RecomendacaoDnaResponse(
        String heroi,
        Rota rota,
        int pontuacao,
        List<DimensaoEstrategica> corrige,
        List<DimensaoEstrategica> explora,
        int dependenciaRecursos,
        int ajusteEconomico,
        int penalidadeRedundancia,
        int ajusteTemporal,
        int bonusSinergiaGrupo,
        int penalidadeAntiSinergia,
        List<String> motivos,
        List<String> alertas
    ) {
        this(
            heroi,
            rota,
            pontuacao,
            corrige,
            explora,
            dependenciaRecursos,
            ajusteEconomico,
            penalidadeRedundancia,
            ajusteTemporal,
            bonusSinergiaGrupo,
            penalidadeAntiSinergia,
            0,
            List.of(),
            motivos,
            alertas
        );
    }

    public RecomendacaoDnaResponse(
        String heroi,
        Rota rota,
        int pontuacao,
        List<DimensaoEstrategica> corrige,
        List<DimensaoEstrategica> explora,
        int dependenciaRecursos,
        int ajusteEconomico,
        int penalidadeRedundancia,
        List<String> motivos,
        List<String> alertas
    ) {
        this(
            heroi,
            rota,
            pontuacao,
            corrige,
            explora,
            dependenciaRecursos,
            ajusteEconomico,
            penalidadeRedundancia,
            0,
            0,
            0,
            0,
            List.of(),
            motivos,
            alertas
        );
    }

    public RecomendacaoDnaResponse(
        String heroi,
        Rota rota,
        int pontuacao,
        List<DimensaoEstrategica> corrige,
        List<DimensaoEstrategica> explora,
        List<String> motivos
    ) {
        this(
            heroi,
            rota,
            pontuacao,
            corrige,
            explora,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            List.of(),
            motivos,
            List.of()
        );
    }
}
