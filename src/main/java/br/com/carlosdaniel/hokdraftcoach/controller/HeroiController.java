package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;
import br.com.carlosdaniel.hokdraftcoach.service.HeroiService;

@RestController
@RequestMapping("/api/herois")
public class HeroiController {

    private final HeroiService heroiService;

    public HeroiController(HeroiService heroiService) {
        this.heroiService = heroiService;
    }

    @GetMapping
    public List<Heroi> listarHerois(
        @RequestParam(required = false) Rota rota
    ) {
        if (rota == null) {
            return heroiService.listarTodos();
        }

        return heroiService.listarPorRota(rota);
    }
}
