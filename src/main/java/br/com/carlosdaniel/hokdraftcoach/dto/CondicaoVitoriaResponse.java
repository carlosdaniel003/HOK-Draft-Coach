package br.com.carlosdaniel.hokdraftcoach.dto;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.model.CondicaoVitoriaTipo;

public record CondicaoVitoriaResponse(
    CondicaoVitoriaTipo tipo,
    int forca,
    boolean principal,
    String titulo,
    String descricao,
    List<String> executores,
    List<String> dependencias,
    List<String> vulnerabilidades,
    List<RespostaCondicaoVitoriaResponse> respostasAdversarias
) {

    public CondicaoVitoriaResponse {
        executores = List.copyOf(executores);
        dependencias = List.copyOf(dependencias);
        vulnerabilidades = List.copyOf(vulnerabilidades);
        respostasAdversarias = List.copyOf(respostasAdversarias);
        if (forca < 0 || forca > 100) {
            throw new IllegalArgumentException(
                "forca deve estar entre 0 e 100."
            );
        }
    }

    public CondicaoVitoriaResponse comoPrincipal(boolean valor) {
        return new CondicaoVitoriaResponse(
            tipo,
            forca,
            valor,
            titulo,
            descricao,
            executores,
            dependencias,
            vulnerabilidades,
            respostasAdversarias
        );
    }
}
