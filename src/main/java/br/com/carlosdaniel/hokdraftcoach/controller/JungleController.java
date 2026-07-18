package br.com.carlosdaniel.hokdraftcoach.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.carlosdaniel.hokdraftcoach.dto.JungleDetalhadoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoComposicaoJungleResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoEncaixeJungleResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ConfrontoJungle;
import br.com.carlosdaniel.hokdraftcoach.model.SinergiaJungleEquipe;
import br.com.carlosdaniel.hokdraftcoach.model.TipoComposicao;
import br.com.carlosdaniel.hokdraftcoach.service.ConhecimentoJungleService;

@RestController
@RequestMapping("/api/junglers")
public class JungleController {

    private final ConhecimentoJungleService conhecimentoJungleService;

    public JungleController(
        ConhecimentoJungleService conhecimentoJungleService
    ) {
        this.conhecimentoJungleService = conhecimentoJungleService;
    }

    @GetMapping
    public List<JungleDetalhadoResponse> listar() {
        return conhecimentoJungleService.listarJunglers();
    }

    @GetMapping("/counters")
    public List<ConfrontoJungle> recomendarCounters(
        @RequestParam String inimigo,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoJungleService.melhoresRespostasParaJungler(
            inimigo,
            limite
        );
    }

    @GetMapping("/confronto")
    public ConfrontoJungle analisarConfronto(
        @RequestParam String candidato,
        @RequestParam String inimigo
    ) {
        return conhecimentoJungleService.analisarConfronto(
            candidato,
            inimigo
        );
    }

    @GetMapping("/combos")
    public List<SinergiaJungleEquipe> recomendarJunglersParaAliado(
        @RequestParam String aliado,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoJungleService.melhoresJunglersParaAliado(
            aliado,
            limite
        );
    }

    @GetMapping("/combo")
    public SinergiaJungleEquipe analisarCombo(
        @RequestParam String jungler,
        @RequestParam String aliado
    ) {
        return conhecimentoJungleService.analisarCombo(jungler, aliado);
    }

    @GetMapping("/contra-composicao")
    public List<RecomendacaoComposicaoJungleResponse>
        recomendarContraComposicao(
            @RequestParam List<TipoComposicao> tipos,
            @RequestParam(defaultValue = "10") int limite
        ) {
        return conhecimentoJungleService.recomendarContraComposicao(
            tipos,
            limite
        );
    }

    @GetMapping("/fortalecer-composicao")
    public List<RecomendacaoEncaixeJungleResponse>
        recomendarParaComposicaoAliada(
            @RequestParam List<TipoComposicao> tipos,
            @RequestParam(defaultValue = "10") int limite
        ) {
        return conhecimentoJungleService.recomendarParaComposicaoAliada(
            tipos,
            limite
        );
    }

    @GetMapping("/{nome}/combos")
    public List<SinergiaJungleEquipe> recomendarCombosDoJungler(
        @PathVariable String nome,
        @RequestParam(defaultValue = "10") int limite
    ) {
        return conhecimentoJungleService.melhoresCombosDoJungler(
            nome,
            limite
        );
    }

    @GetMapping("/{nome}")
    public JungleDetalhadoResponse buscar(@PathVariable String nome) {
        return conhecimentoJungleService.buscarJungler(nome);
    }
}
