package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public Map<String, Object> consultarStatus() {
        return Map.of(
            "aplicacao", "HOK Draft Coach",
            "status", "online",
            "versaoJava", 21
        );
    }
}