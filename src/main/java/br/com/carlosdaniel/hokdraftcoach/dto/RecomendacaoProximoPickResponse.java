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
    RecomendacaoPickResponse recomendacaoPrincipal,
    List<RecomendacaoPickResponse> alternativas,
    List<String> avisos
) {
}
