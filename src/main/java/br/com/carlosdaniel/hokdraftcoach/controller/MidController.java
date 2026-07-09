package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.MidDetalhadoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoMidResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoMidLane;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaMidEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.service.ConhecimentoMidService;

@RestController
@RequestMapping("/api/mids")
public class MidController {

    private final ConhecimentoMidService conhecimentoMidService;

    public MidController(ConhecimentoMidService conhecimentoMidService) {
        this.conhecimentoMidService = conhecimentoMidService;
    }

    @GetMapping
    public List<MidDetalhadoResponse> listar() {
        return conhecimentoMidService.listarMids();
    }

    @GetMapping("/counters")
    public List<ConfrontoMidLane> recomendarCounters(
        @RequestParam String inimigo,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoMidService.melhoresRespostasParaMid(
            inimigo,
            limite
        );
    }

    @GetMapping("/confronto")
    public ConfrontoMidLane analisarConfronto(
        @RequestParam String candidato,
        @RequestParam String inimigo
    ) {
        return conhecimentoMidService.analisarConfronto(
            candidato,
            inimigo
        );
    }

    @GetMapping("/combos")
    public List<SinergiaMidEquipe> recomendarMidsParaAliado(
        @RequestParam String aliado,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoMidService.melhoresMidsParaAliado(
            aliado,
            limite
        );
    }

    @GetMapping("/combo")
    public SinergiaMidEquipe analisarCombo(
        @RequestParam String mid,
        @RequestParam String aliado
    ) {
        return conhecimentoMidService.analisarCombo(mid, aliado);
    }

    @GetMapping("/contra-composicao")
    public List<RecomendacaoComposicaoMidResponse> recomendarContraComposicao(
        @RequestParam List<TipoComposicao> tipos,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoMidService.recomendarContraComposicao(
            tipos,
            limite
        );
    }

    @GetMapping("/{nome}/combos")
    public List<SinergiaMidEquipe> recomendarCombosDoMid(
        @PathVariable String nome,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoMidService.melhoresCombosDoMid(nome, limite);
    }

    @GetMapping("/{nome}")
    public MidDetalhadoResponse buscar(@PathVariable String nome) {
        return conhecimentoMidService.buscarMid(nome);
    }
}
