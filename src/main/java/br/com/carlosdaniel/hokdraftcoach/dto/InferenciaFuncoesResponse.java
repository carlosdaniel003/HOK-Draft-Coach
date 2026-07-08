package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

public record InferenciaFuncoesResponse(
    String versaoMotor,
    InferenciaEquipeResponse equipeAzul,
    InferenciaEquipeResponse equipeVermelha,
    List<String> avisos
) {
}
