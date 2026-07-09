package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteRespostaAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseAmeacasResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class AnaliseAmeacaComposicaoService
    extends AnaliseTemporalSinergiaService {

    private final HeroiService heroiService;
    private final AnaliseAmeacaService analiseAmeacaService;

    public AnaliseAmeacaComposicaoService(
        HeroiService heroiService,
        DnaHeroiService dnaHeroiService,
        CondicaoVitoriaService condicaoVitoriaService,
        EconomiaRecursosService economiaRecursosService,
        NecessidadePenalidadeService necessidadePenalidadeService,
        PerfilTemporalService perfilTemporalService,
        SinergiaGrupoService sinergiaGrupoService,
        AntiSinergiaService antiSinergiaService,
        AnaliseAmeacaService analiseAmeacaService
    ) {
        super(
            heroiService,
            dnaHeroiService,
            condicaoVitoriaService,
            economiaRecursosService,
            necessidadePenalidadeService,
            perfilTemporalService,
            sinergiaGrupoService,
            antiSinergiaService
        );
        this.heroiService = heroiService;
        this.analiseAmeacaService = analiseAmeacaService;
    }

    @Override
    public DiagnosticoComposicaoResponse diagnosticar(
        List<String> aliados,
        List<String> inimigos
    ) {
        DiagnosticoComposicaoResponse base = super.diagnosticar(
            aliados,
            inimigos
        );
        List<Heroi> equipeInimiga = resolverEquipe(inimigos, true);
        AnaliseAmeacasResponse ameacas = analiseAmeacaService.analisar(
            equipeInimiga,
            base.composicaoInimiga(),
            base.condicoesVitoriaInimigas(),
            base.sinergiasGrupoComposicaoInimiga(),
            base.antiSinergiasComposicaoInimiga()
        );

        return new DiagnosticoComposicaoResponse(
            base.nossaComposicao(),
            base.composicaoInimiga(),
            base.diagnosticos(),
            base.prioridades(),
            base.nossasCondicoesVitoria(),
            base.condicoesVitoriaInimigas(),
            base.necessidades(),
            base.penalidades(),
            base.economiaNossaComposicao(),
            base.economiaComposicaoInimiga(),
            base.curvaPoderNossaComposicao(),
            base.curvaPoderComposicaoInimiga(),
            base.diagnosticosTemporais(),
            base.sinergiasGrupoNossaComposicao(),
            base.sinergiasGrupoComposicaoInimiga(),
            base.antiSinergiasNossaComposicao(),
            base.antiSinergiasComposicaoInimiga(),
            ameacas,
            base.diagnosticoConcluido()
        );
    }

    @Override
    public List<RecomendacaoDnaResponse> recomendar(
        List<String> aliados,
        List<String> inimigos,
        Rota rota,
        int limite
    ) {
        List<RecomendacaoDnaResponse> base = super.recomendar(
            aliados,
            inimigos,
            rota,
            50
        );
        AnaliseAmeacasResponse ameacas = diagnosticar(
            aliados,
            inimigos
        ).analiseAmeacasInimigas();

        return base.stream()
            .map(recomendacao -> ajustarRespostaAmeaca(
                recomendacao,
                ameacas
            ))
            .sorted(
                Comparator.comparingInt(RecomendacaoDnaResponse::pontuacao)
                    .reversed()
                    .thenComparing(RecomendacaoDnaResponse::heroi)
            )
            .limit(limite)
            .toList();
    }

    public AnaliseAmeacasResponse analisarAmeacas(
        List<String> inimigos
    ) {
        List<Heroi> equipe = resolverEquipe(inimigos, false);
        DiagnosticoComposicaoResponse base = super.diagnosticar(
            inimigos,
            List.of()
        );
        return analiseAmeacaService.analisar(
            equipe,
            base.nossaComposicao(),
            base.nossasCondicoesVitoria(),
            base.sinergiasGrupoNossaComposicao(),
            base.antiSinergiasNossaComposicao()
        );
    }

    private RecomendacaoDnaResponse ajustarRespostaAmeaca(
        RecomendacaoDnaResponse base,
        AnaliseAmeacasResponse ameacas
    ) {
        Heroi candidato = heroiService.buscarPorNome(base.heroi())
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + base.heroi() + "."
            ));
        AjusteRespostaAmeacaResponse ajuste =
            analiseAmeacaService.avaliarCandidato(candidato, ameacas);
        int pontuacao = limitar(
            base.pontuacao() + ajuste.bonus(),
            0,
            100
        );
        List<String> motivos = new ArrayList<>(base.motivos());
        motivos.addAll(ajuste.motivos());

        return new RecomendacaoDnaResponse(
            base.heroi(),
            base.rota(),
            pontuacao,
            base.corrige(),
            base.explora(),
            base.dependenciaRecursos(),
            base.ajusteEconomico(),
            base.penalidadeRedundancia(),
            base.ajusteTemporal(),
            base.bonusSinergiaGrupo(),
            base.penalidadeAntiSinergia(),
            ajuste.bonus(),
            ajuste.alvosRespondidos(),
            motivos.stream().distinct().limit(12).toList(),
            base.alertas()
        );
    }

    private List<Heroi> resolverEquipe(
        List<String> nomes,
        boolean opcional
    ) {
        if (nomes == null || nomes.isEmpty()) {
            if (opcional) {
                return List.of();
            }
            throw new RegraNegocioException(
                "Informe ao menos um herói para a composição."
            );
        }
        if (nomes.size() > 5) {
            throw new RegraNegocioException(
                "A composição pode ter no máximo 5 heróis."
            );
        }
        List<Heroi> equipe = nomes.stream()
            .map(String::trim)
            .filter(nome -> !nome.isBlank())
            .map(nome -> heroiService.buscarPorNome(nome)
                .orElseThrow(() -> new RegraNegocioException(
                    "Herói não encontrado: " + nome + "."
                )))
            .toList();
        Set<String> unicos = new LinkedHashSet<>();
        equipe.forEach(heroi -> unicos.add(normalizar(heroi.getNome())));
        if (unicos.size() != equipe.size()) {
            throw new RegraNegocioException(
                "A composição possui heróis repetidos."
            );
        }
        return equipe;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase(Locale.ROOT);
    }

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
