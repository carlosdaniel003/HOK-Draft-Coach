package br.com.carlosdaniel.hokdraftcoach.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseDraftResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DraftRequest;
import br.com.carlosdaniel.hokdraftcoach.service.DraftService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/draft")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping("/recomendar")
    public AnaliseDraftResponse recomendar(
        @Valid @RequestBody DraftRequest request
    ) {
        return draftService.recomendar(request);
    }
}
