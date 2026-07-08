package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoResponse;
import br.com.carlosdaniel.hokdraftcoach.service.RecomendacaoService;

@RestController
@RequestMapping("/api/recomendacoes")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(
        RecomendacaoService recomendacaoService
    ) {
        this.recomendacaoService = recomendacaoService;
    }

    @GetMapping("/counter")
    public List<RecomendacaoResponse> recomendarCounter(
        @RequestParam String inimigo
    ) {
        return recomendacaoService.recomendarContra(inimigo);
    }
}