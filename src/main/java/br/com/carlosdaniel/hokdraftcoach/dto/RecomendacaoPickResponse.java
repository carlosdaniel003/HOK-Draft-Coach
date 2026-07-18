package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;
import java.util.Map;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record RecomendacaoPickResponse(
    Long heroiId,
    String heroi,
    List<Rota> rotasRecomendadas,
    int pontuacaoFinal,
    int mediaCenarios,
    int piorCenario,
    int coberturaHipoteses,
    int cenariosAvaliados,
    String seguranca,
    int dificuldade,
    Map<String, Integer> componentes,
    List<String> motivos,
    List<String> riscos,
    PerfilSegurancaBlindPickResponse perfilBlindPick,
    boolean dadosValidados
) {

    public RecomendacaoPickResponse(
        Long heroiId,
        String heroi,
        List<Rota> rotasRecomendadas,
        int pontuacaoFinal,
        int mediaCenarios,
        int piorCenario,
        int coberturaHipoteses,
        int cenariosAvaliados,
        String seguranca,
        int dificuldade,
        Map<String, Integer> componentes,
        List<String> motivos,
        List<String> riscos,
        boolean dadosValidados
    ) {
        this(
            heroiId,
            heroi,
            rotasRecomendadas,
            pontuacaoFinal,
            mediaCenarios,
            piorCenario,
            coberturaHipoteses,
            cenariosAvaliados,
            seguranca,
            dificuldade,
            componentes,
            motivos,
            riscos,
            null,
            dadosValidados
        );
    }
}
