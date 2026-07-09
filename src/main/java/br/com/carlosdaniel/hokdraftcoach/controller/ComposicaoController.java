package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.service.DnaComposicaoService;

@RestController
@RequestMapping("/api/composicoes")
public class ComposicaoController {

    private final DnaComposicaoService dnaComposicaoService;

    public ComposicaoController(DnaComposicaoService dnaComposicaoService) {
        this.dnaComposicaoService = dnaComposicaoService;
    }

    @GetMapping("/dna")
    public DnaComposicao gerarDna(
        @RequestParam List<String> herois
    ) {
        return dnaComposicaoService.gerarDnaPorNomes(herois);
    }

    @GetMapping("/diagnostico")
    public DiagnosticoComposicaoResponse diagnosticar(
        @RequestParam List<String> aliados,
        @RequestParam(required = false) List<String> inimigos
    ) {
        return dnaComposicaoService.diagnosticar(aliados, inimigos);
    }

    @GetMapping("/recomendacoes")
    public List<RecomendacaoDnaResponse> recomendar(
        @RequestParam List<String> aliados,
        @RequestParam(required = false) List<String> inimigos,
        @RequestParam Rota rota,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return dnaComposicaoService.recomendar(
            aliados,
            inimigos,
            rota,
            limite
        );
    }
}
