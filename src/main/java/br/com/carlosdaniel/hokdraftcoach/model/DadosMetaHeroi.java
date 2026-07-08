package br.com.carlosdaniel.hokdraftcoach.model;

import java.time.LocalDate;

public record DadosMetaHeroi(
    TierMeta tier,
    String versao,
    LocalDate atualizadoEm,
    String fonte,
    boolean oficial
) {

    public static DadosMetaHeroi naoClassificado() {
        return new DadosMetaHeroi(
            TierMeta.NAO_CLASSIFICADO,
            "SEM_VERSAO",
            null,
            "INTERNO",
            false
        );
    }
}
