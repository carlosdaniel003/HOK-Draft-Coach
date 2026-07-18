package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.LadoDraft;

public record RecomendacaoProximoPickResponse(
    String versaoMotor,
    String estadoDraft,
    String mensagem,
    boolean ehMinhaVez,
    String meuSlot,
    LadoDraft proximoLado,
    List<String> proximosSlots,
    int hipotesesAliadas,
    int hipotesesInimigas,
    String confiancaFuncoesAliadas,
    String confiancaFuncoesInimigas,
    ContextoDraftResponse contextoDraft,
    DiagnosticoComposicaoResponse diagnosticoComposicao,
    RecomendacaoPickResponse recomendacaoPrincipal,
    List<RecomendacaoPickResponse> alternativas,
    List<OpcaoPickResponse> opcoesEstrategicas,
    List<String> avisos
) {

    public RecomendacaoProximoPickResponse {
        proximosSlots = List.copyOf(proximosSlots);
        alternativas = List.copyOf(alternativas);
        opcoesEstrategicas = List.copyOf(opcoesEstrategicas);
        avisos = List.copyOf(avisos);
    }

    public RecomendacaoProximoPickResponse(
        String versaoMotor,
        String estadoDraft,
        String mensagem,
        boolean ehMinhaVez,
        String meuSlot,
        LadoDraft proximoLado,
        List<String> proximosSlots,
        int hipotesesAliadas,
        int hipotesesInimigas,
        String confiancaFuncoesAliadas,
        String confiancaFuncoesInimigas,
        ContextoDraftResponse contextoDraft,
        DiagnosticoComposicaoResponse diagnosticoComposicao,
        RecomendacaoPickResponse recomendacaoPrincipal,
        List<RecomendacaoPickResponse> alternativas,
        List<String> avisos
    ) {
        this(
            versaoMotor,
            estadoDraft,
            mensagem,
            ehMinhaVez,
            meuSlot,
            proximoLado,
            proximosSlots,
            hipotesesAliadas,
            hipotesesInimigas,
            confiancaFuncoesAliadas,
            confiancaFuncoesInimigas,
            contextoDraft,
            diagnosticoComposicao,
            recomendacaoPrincipal,
            alternativas,
            List.of(),
            avisos
        );
    }

    public RecomendacaoProximoPickResponse(
        String versaoMotor,
        String estadoDraft,
        String mensagem,
        boolean ehMinhaVez,
        String meuSlot,
        LadoDraft proximoLado,
        List<String> proximosSlots,
        int hipotesesAliadas,
        int hipotesesInimigas,
        String confiancaFuncoesAliadas,
        String confiancaFuncoesInimigas,
        DiagnosticoComposicaoResponse diagnosticoComposicao,
        RecomendacaoPickResponse recomendacaoPrincipal,
        List<RecomendacaoPickResponse> alternativas,
        List<String> avisos
    ) {
        this(
            versaoMotor,
            estadoDraft,
            mensagem,
            ehMinhaVez,
            meuSlot,
            proximoLado,
            proximosSlots,
            hipotesesAliadas,
            hipotesesInimigas,
            confiancaFuncoesAliadas,
            confiancaFuncoesInimigas,
            null,
            diagnosticoComposicao,
            recomendacaoPrincipal,
            alternativas,
            List.of(),
            avisos
        );
    }

    public RecomendacaoProximoPickResponse(
        String versaoMotor,
        String estadoDraft,
        String mensagem,
        boolean ehMinhaVez,
        String meuSlot,
        LadoDraft proximoLado,
        List<String> proximosSlots,
        int hipotesesAliadas,
        int hipotesesInimigas,
        String confiancaFuncoesAliadas,
        String confiancaFuncoesInimigas,
        RecomendacaoPickResponse recomendacaoPrincipal,
        List<RecomendacaoPickResponse> alternativas,
        List<String> avisos
    ) {
        this(
            versaoMotor,
            estadoDraft,
            mensagem,
            ehMinhaVez,
            meuSlot,
            proximoLado,
            proximosSlots,
            hipotesesAliadas,
            hipotesesInimigas,
            confiancaFuncoesAliadas,
            confiancaFuncoesInimigas,
            null,
            null,
            recomendacaoPrincipal,
            alternativas,
            List.of(),
            avisos
        );
    }
}
