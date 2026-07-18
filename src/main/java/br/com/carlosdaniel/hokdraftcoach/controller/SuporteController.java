package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoSuporteResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.SuporteDetalhadoResponse;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaBotLane;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.service.ConhecimentoSuporteService;

@RestController
@RequestMapping("/api/suportes")
public class SuporteController {

    private final ConhecimentoSuporteService conhecimentoSuporteService;

    public SuporteController(
        ConhecimentoSuporteService conhecimentoSuporteService
    ) {
        this.conhecimentoSuporteService = conhecimentoSuporteService;
    }

    @GetMapping
    public List<SuporteDetalhadoResponse> listar() {
        return conhecimentoSuporteService.listarSuportes();
    }

    @GetMapping("/recomendados")
    public List<SinergiaBotLane> recomendarParaAtirador(
        @RequestParam String atirador,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoSuporteService.melhoresSuportesParaAtirador(
            atirador,
            limite
        );
    }

    @GetMapping("/sinergia")
    public SinergiaBotLane analisarDupla(
        @RequestParam String suporte,
        @RequestParam String atirador
    ) {
        return conhecimentoSuporteService.analisarDupla(suporte, atirador);
    }

    @GetMapping("/contra-composicao")
    public List<RecomendacaoComposicaoSuporteResponse> recomendarContraComposicao(
        @RequestParam List<TipoComposicao> tipos,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoSuporteService.recomendarContraComposicao(
            tipos,
            limite
        );
    }

    @GetMapping("/{nome}/atiradores")
    public List<SinergiaBotLane> recomendarAtiradores(
        @PathVariable String nome,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoSuporteService.melhoresAtiradoresParaSuporte(
            nome,
            limite
        );
    }

    @GetMapping("/{nome}")
    public SuporteDetalhadoResponse buscar(@PathVariable String nome) {
        return conhecimentoSuporteService.buscarSuporte(nome);
    }
}
