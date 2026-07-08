package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;
import java.util.Map;

import br.com.carlosdaniel.hokdraftcoach.model.Rota;

public record RecomendacaoDraftResponse(
    Long heroiId,
    String nome,
    Rota rota,
    int pontuacaoFinal,
    String nivel,
    Map<String, Integer> componentes,
    List<String> motivos,
    boolean dadosValidados
) {
}
