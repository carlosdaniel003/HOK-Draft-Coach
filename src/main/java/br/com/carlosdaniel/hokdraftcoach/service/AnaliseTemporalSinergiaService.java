package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AntiSinergiaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.CurvaPoderComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoTemporalResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.SinergiaGrupoResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilTemporalHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class AnaliseTemporalSinergiaService
    extends AnaliseEstrategicaComposicaoService {

    private final HeroiService heroiService;
    private final PerfilTemporalService perfilTemporalService;
    private final SinergiaGrupoService sinergiaGrupoService;
    private final AntiSinergiaService antiSinergiaService;

    public AnaliseTemporalSinergiaService(
        HeroiService heroiService,
        DnaHeroiService dnaHeroiService,
        CondicaoVitoriaService condicaoVitoriaService,
        EconomiaRecursosService economiaRecursosService,
        NecessidadePenalidadeService necessidadePenalidadeService,
        PerfilTemporalService perfilTemporalService,
        SinergiaGrupoService sinergiaGrupoService,
        AntiSinergiaService antiSinergiaService
    ) {
        super(
            heroiService,
            dnaHeroiService,
            condicaoVitoriaService,
            economiaRecursosService,
            necessidadePenalidadeService
        );
        this.heroiService = heroiService;
        this.perfilTemporalService = perfilTemporalService;
        this.sinergiaGrupoService = sinergiaGrupoService;
        this.antiSinergiaService = antiSinergiaService;
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
        List<Heroi> nossaEquipe = resolverEquipe(aliados, false);
        List<Heroi> equipeInimiga = resolverEquipe(inimigos, true);
        CurvaPoderComposicaoResponse curvaNossa =
            perfilTemporalService.curva(nossaEquipe);
        CurvaPoderComposicaoResponse curvaInimiga =
            perfilTemporalService.curva(equipeInimiga);
        List<DiagnosticoTemporalResponse> diagnosticosTemporais =
            perfilTemporalService.diagnosticarConfronto(
                curvaNossa,
                curvaInimiga
            );
        List<SinergiaGrupoResponse> sinergiasNossa =
            sinergiaGrupoService.avaliar(nossaEquipe);
        List<SinergiaGrupoResponse> sinergiasInimigas =
            sinergiaGrupoService.avaliar(equipeInimiga);
        List<AntiSinergiaResponse> antiSinergiasNossa =
            antiSinergiaService.analisar(
                nossaEquipe,
                base.nossaComposicao()
            );
        List<AntiSinergiaResponse> antiSinergiasInimigas =
            antiSinergiaService.analisar(
                equipeInimiga,
                base.composicaoInimiga()
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
            curvaNossa,
            curvaInimiga,
            diagnosticosTemporais,
            sinergiasNossa,
            sinergiasInimigas,
            antiSinergiasNossa,
            antiSinergiasInimigas,
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
        List<Heroi> nossaEquipe = resolverEquipe(aliados, false);
        DiagnosticoComposicaoResponse diagnostico = diagnosticar(
            aliados,
            inimigos
        );
        List<SinergiaGrupoResponse> sinergiasAntes =
            diagnostico.sinergiasGrupoNossaComposicao();
        List<AntiSinergiaResponse> antiAntes =
            diagnostico.antiSinergiasNossaComposicao();

        return base.stream()
            .map(recomendacao -> ajustar(
                recomendacao,
                nossaEquipe,
                diagnostico,
                sinergiasAntes,
                antiAntes
            ))
            .sorted(
                Comparator.comparingInt(RecomendacaoDnaResponse::pontuacao)
                    .reversed()
                    .thenComparing(RecomendacaoDnaResponse::heroi)
            )
            .limit(limite)
            .toList();
    }

    public PerfilTemporalHeroi perfilTemporal(String nome) {
        Heroi heroi = heroiService.buscarPorNome(nome)
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + nome + "."
            ));
        return perfilTemporalService.perfil(heroi);
    }

    public CurvaPoderComposicaoResponse curvaPoder(List<String> nomes) {
        return perfilTemporalService.curva(resolverEquipe(nomes, false));
    }

    private RecomendacaoDnaResponse ajustar(
        RecomendacaoDnaResponse base,
        List<Heroi> nossaEquipe,
        DiagnosticoComposicaoResponse diagnostico,
        List<SinergiaGrupoResponse> sinergiasAntes,
        List<AntiSinergiaResponse> antiAntes
    ) {
        Heroi candidato = heroiService.buscarPorNome(base.heroi())
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + base.heroi() + "."
            ));
        PerfilTemporalHeroi perfilCandidato =
            perfilTemporalService.perfil(candidato);
        int ajusteTemporal = perfilTemporalService.ajusteCandidato(
            diagnostico.curvaPoderNossaComposicao(),
            diagnostico.curvaPoderComposicaoInimiga(),
            perfilCandidato
        );

        List<Heroi> projetada = new ArrayList<>(nossaEquipe);
        projetada.add(candidato);
        List<SinergiaGrupoResponse> sinergiasDepois =
            sinergiaGrupoService.avaliar(projetada);
        int bonusSinergia = sinergiaGrupoService.bonusAoAdicionar(
            sinergiasAntes,
            sinergiasDepois
        );
        DnaComposicao dnaProjetado = gerarDna(projetada);
        List<AntiSinergiaResponse> antiDepois =
            antiSinergiaService.analisar(projetada, dnaProjetado);
        int penalidadeAnti = antiSinergiaService.penalidadeAoAdicionar(
            antiAntes,
            antiDepois
        );
        int pontuacao = limitar(
            base.pontuacao()
                + ajusteTemporal
                + bonusSinergia
                - penalidadeAnti,
            0,
            100
        );

        List<String> motivos = new ArrayList<>(base.motivos());
        List<String> alertas = new ArrayList<>(base.alertas());
        if (ajusteTemporal > 0) {
            motivos.add(
                candidato.getNome() + " corrige a curva temporal com "
                    + perfilCandidato.earlyGame() + "/"
                    + perfilCandidato.midGame() + "/"
                    + perfilCandidato.lateGame()
                    + " em early, mid e late game."
            );
        } else if (ajusteTemporal < 0) {
            alertas.add(
                candidato.getNome()
                    + " reforça uma fase que já não corresponde à janela necessária da composição."
            );
        }

        List<SinergiaGrupoResponse> novasSinergias = novasSinergias(
            sinergiasAntes,
            sinergiasDepois
        );
        novasSinergias.stream()
            .limit(3)
            .forEach(sinergia -> motivos.add(
                "Completa a sinergia de grupo " + sinergia.codigo()
                    + ": " + sinergia.descricao()
            ));

        List<AntiSinergiaResponse> novasAntiSinergias = novasAntiSinergias(
            antiAntes,
            antiDepois
        );
        novasAntiSinergias.stream()
            .limit(3)
            .forEach(anti -> alertas.add(
                anti.descricao() + " Mitigação: " + anti.mitigacao()
            ));

        return new RecomendacaoDnaResponse(
            base.heroi(),
            base.rota(),
            pontuacao,
            base.corrige(),
            base.explora(),
            base.dependenciaRecursos(),
            base.ajusteEconomico(),
            base.penalidadeRedundancia(),
            ajusteTemporal,
            bonusSinergia,
            penalidadeAnti,
            motivos.stream().distinct().limit(10).toList(),
            alertas.stream().distinct().limit(7).toList()
        );
    }

    private List<SinergiaGrupoResponse> novasSinergias(
        List<SinergiaGrupoResponse> antes,
        List<SinergiaGrupoResponse> depois
    ) {
        Set<String> codigosAntes = new LinkedHashSet<>();
        antes.stream()
            .filter(SinergiaGrupoResponse::ativa)
            .map(SinergiaGrupoResponse::codigo)
            .forEach(codigosAntes::add);
        return depois.stream()
            .filter(SinergiaGrupoResponse::ativa)
            .filter(resposta -> !codigosAntes.contains(resposta.codigo()))
            .toList();
    }

    private List<AntiSinergiaResponse> novasAntiSinergias(
        List<AntiSinergiaResponse> antes,
        List<AntiSinergiaResponse> depois
    ) {
        Set<String> codigosAntes = new LinkedHashSet<>();
        antes.stream().map(AntiSinergiaResponse::codigo).forEach(codigosAntes::add);
        return depois.stream()
            .filter(resposta -> !codigosAntes.contains(resposta.codigo()))
            .toList();
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
                "Informe ao menos um herói para a composição aliada."
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
