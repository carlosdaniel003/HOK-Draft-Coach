package br.com.carlosdaniel.hokdraftcoach.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DraftRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesRequest;
import br.com.carlosdaniel.hokdraftcoach.dto.InferenciaFuncoesResponse;
import br.com.carlosdaniel.hokdraftcoach.service.DraftService;
import br.com.carlosdaniel.hokdraftcoach.service.InferenciaFuncoesService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/draft")
public class DraftController {

    private final DraftService draftService;
    private final InferenciaFuncoesService inferenciaFuncoesService;

    public DraftController(
        DraftService draftService,
        InferenciaFuncoesService inferenciaFuncoesService
    ) {
        this.draftService = draftService;
        this.inferenciaFuncoesService = inferenciaFuncoesService;
    }

    @PostMapping("/recomendar")
    public AnaliseDraftResponse recomendar(
        @Valid @RequestBody DraftRequest request
    ) {
        return draftService.recomendar(request);
    }

    @PostMapping("/inferir-funcoes")
    public InferenciaFuncoesResponse inferirFuncoes(
        @Valid @RequestBody InferenciaFuncoesRequest request
    ) {
        return inferenciaFuncoesService.inferir(request);
    }
}
