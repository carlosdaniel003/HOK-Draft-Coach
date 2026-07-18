package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record ExplicacaoRecomendacaoResponse(
    String resumo,
    List<String> leituraInimiga,
    List<String> leituraAliada,
    List<String> porQueFunciona,
    List<String> riscos,
    String planoDeJogo
) {

    public ExplicacaoRecomendacaoResponse {
        leituraInimiga = List.copyOf(leituraInimiga);
        leituraAliada = List.copyOf(leituraAliada);
        porQueFunciona = List.copyOf(porQueFunciona);
        riscos = List.copyOf(riscos);
    }
}
