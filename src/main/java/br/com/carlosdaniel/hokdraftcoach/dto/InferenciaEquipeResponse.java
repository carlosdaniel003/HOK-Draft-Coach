package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record InferenciaEquipeResponse(
    String lado,
    int totalPicks,
    int totalHipoteses,
    boolean composicaoCompativel,
    String confiancaMelhorHipotese,
    List<AmbiguidadeFuncaoResponse> ambiguidades,
    List<HipoteseFuncaoResponse> hipoteses,
    List<String> avisos
) {
}
