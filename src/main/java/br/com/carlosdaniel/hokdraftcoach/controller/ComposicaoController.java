package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseAmeacasResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.CurvaPoderComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilTemporalHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.service.AnaliseAmeacaComposicaoService;

@RestController
@RequestMapping("/api/composicoes")
public class ComposicaoController {

    private final AnaliseAmeacaComposicaoService dnaComposicaoService;

    public ComposicaoController(
        AnaliseAmeacaComposicaoService dnaComposicaoService
    ) {
        this.dnaComposicaoService = dnaComposicaoService;
    }

    @GetMapping("/dna")
    public DnaComposicao gerarDna(
        @RequestParam List<String> herois
    ) {
        return dnaComposicaoService.gerarDnaPorNomes(herois);
    }

    @GetMapping("/perfil-temporal")
    public PerfilTemporalHeroi perfilTemporal(
        @RequestParam String heroi
    ) {
        return dnaComposicaoService.perfilTemporal(heroi);
    }

    @GetMapping("/curva-poder")
    public CurvaPoderComposicaoResponse curvaPoder(
        @RequestParam List<String> herois
    ) {
        return dnaComposicaoService.curvaPoder(herois);
    }

    @GetMapping("/ameacas")
    public AnaliseAmeacasResponse analisarAmeacas(
        @RequestParam List<String> inimigos
    ) {
        return dnaComposicaoService.analisarAmeacas(inimigos);
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
