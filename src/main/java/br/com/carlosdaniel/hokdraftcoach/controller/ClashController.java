package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.ClashDetalhadoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoClashResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoEncaixeClashResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoClashLane;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaClashEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.service.ConhecimentoClashService;

@RestController
@RequestMapping("/api/tops")
public class ClashController {

    private final ConhecimentoClashService conhecimentoClashService;

    public ClashController(ConhecimentoClashService conhecimentoClashService) {
        this.conhecimentoClashService = conhecimentoClashService;
    }

    @GetMapping
    public List<ClashDetalhadoResponse> listar() {
        return conhecimentoClashService.listarTops();
    }

    @GetMapping("/counters")
    public List<ConfrontoClashLane> recomendarCounters(
        @RequestParam String inimigo,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoClashService.melhoresRespostasParaTop(
            inimigo,
            limite
        );
    }

    @GetMapping("/confronto")
    public ConfrontoClashLane analisarConfronto(
        @RequestParam String candidato,
        @RequestParam String inimigo
    ) {
        return conhecimentoClashService.analisarConfronto(
            candidato,
            inimigo
        );
    }

    @GetMapping("/combos")
    public List<SinergiaClashEquipe> recomendarTopsParaAliado(
        @RequestParam String aliado,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoClashService.melhoresTopsParaAliado(
            aliado,
            limite
        );
    }

    @GetMapping("/combo")
    public SinergiaClashEquipe analisarCombo(
        @RequestParam String top,
        @RequestParam String aliado
    ) {
        return conhecimentoClashService.analisarCombo(top, aliado);
    }

    @GetMapping("/contra-composicao")
    public List<RecomendacaoComposicaoClashResponse>
        recomendarContraComposicao(
            @RequestParam List<TipoComposicao> tipos,
            @RequestParam(defaultValue = "10") int limite
        ) {
        return conhecimentoClashService.recomendarContraComposicao(
            tipos,
            limite
        );
    }

    @GetMapping("/fortalecer-composicao")
    public List<RecomendacaoEncaixeClashResponse>
        recomendarParaComposicaoAliada(
            @RequestParam List<TipoComposicao> tipos,
            @RequestParam(defaultValue = "10") int limite
        ) {
        return conhecimentoClashService.recomendarParaComposicaoAliada(
            tipos,
            limite
        );
    }

    @GetMapping("/{nome}/combos")
    public List<SinergiaClashEquipe> recomendarCombosDoTop(
        @PathVariable String nome,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoClashService.melhoresCombosDoTop(nome, limite);
    }

    @GetMapping("/{nome}")
    public ClashDetalhadoResponse buscar(@PathVariable String nome) {
        return conhecimentoClashService.buscarTop(nome);
    }
}
