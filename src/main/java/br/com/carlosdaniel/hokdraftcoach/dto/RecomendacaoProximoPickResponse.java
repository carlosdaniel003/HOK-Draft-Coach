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
    DiagnosticoComposicaoResponse diagnosticoComposicao,
    RecomendacaoPickResponse recomendacaoPrincipal,
    List<RecomendacaoPickResponse> alternativas,
    List<String> avisos
) {

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
            recomendacaoPrincipal,
            alternativas,
            avisos
        );
    }
}
