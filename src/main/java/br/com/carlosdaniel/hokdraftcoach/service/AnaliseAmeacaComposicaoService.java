package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.AjusteRespostaAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AlvoPrioritarioAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.AnaliseAmeacasResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilAmeacaHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.SinergiaGrupoResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PapelAmeaca;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class AnaliseAmeacaComposicaoService
    extends AnaliseTemporalSinergiaService {

    private static final int LIMITE_CACHE_ANALISE = 256;

    private final HeroiService heroiService;
    private final AnaliseAmeacaService analiseAmeacaService;
    private final ConcurrentMap<ChaveDiagnostico, DiagnosticoComposicaoResponse> cacheDiagnosticos =
        new ConcurrentHashMap<>();
    private final ConcurrentMap<ChaveRecomendacao, List<RecomendacaoDnaResponse>> cacheRecomendacoes =
        new ConcurrentHashMap<>();

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
        ChaveDiagnostico chave = new ChaveDiagnostico(
            copiarNomes(aliados),
            copiarNomes(inimigos)
        );
        if (
            cacheDiagnosticos.size() >= LIMITE_CACHE_ANALISE
                && !cacheDiagnosticos.containsKey(chave)
        ) {
            cacheDiagnosticos.clear();
        }
        return cacheDiagnosticos.computeIfAbsent(
            chave,
            ignorada -> calcularDiagnostico(aliados, inimigos)
        );
    }

    private DiagnosticoComposicaoResponse calcularDiagnostico(
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
        ameacas = refinarDependencias(
            ameacas,
            base.sinergiasGrupoComposicaoInimiga()
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
        ChaveRecomendacao chave = new ChaveRecomendacao(
            copiarNomes(aliados),
            copiarNomes(inimigos),
            rota,
            limite
        );
        if (
            cacheRecomendacoes.size() >= LIMITE_CACHE_ANALISE
                && !cacheRecomendacoes.containsKey(chave)
        ) {
            cacheRecomendacoes.clear();
        }
        return cacheRecomendacoes.computeIfAbsent(
            chave,
            ignorada -> calcularRecomendacao(aliados, inimigos, rota, limite)
        );
    }

    private List<RecomendacaoDnaResponse> calcularRecomendacao(
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
        AnaliseAmeacasResponse ameacas = analiseAmeacaService.analisar(
            equipe,
            base.nossaComposicao(),
            base.nossasCondicoesVitoria(),
            base.sinergiasGrupoNossaComposicao(),
            base.antiSinergiasNossaComposicao()
        );
        return refinarDependencias(
            ameacas,
            base.sinergiasGrupoNossaComposicao()
        );
    }

    private AnaliseAmeacasResponse refinarDependencias(
        AnaliseAmeacasResponse original,
        List<SinergiaGrupoResponse> sinergias
    ) {
        if (
            original == null
                || original.maiorAmeaca() == null
                || original.perfis().isEmpty()
        ) {
            return original == null
                ? AnaliseAmeacasResponse.vazia()
                : original;
        }

        PerfilAmeacaHeroiResponse ameacaOriginal = original.maiorAmeaca();
        PerfilAmeacaHeroiResponse iniciadorOriginal =
            original.iniciadorPrincipal();
        PerfilAmeacaHeroiResponse habilitadorOriginal =
            original.habilitadorCritico();
        PerfilAmeacaHeroiResponse habilitadorRefinado =
            habilitadorOriginal;

        if (
            iniciadorOriginal != null
                && habilitadorOriginal != null
                && mesmoHeroi(
                    iniciadorOriginal.heroi(),
                    habilitadorOriginal.heroi()
                )
                && original.perfis().size() > 2
        ) {
            String nomeAmeaca = ameacaOriginal.heroi();
            String nomeIniciador = iniciadorOriginal.heroi();
            PerfilAmeacaHeroiResponse alternativo = original.perfis().stream()
                .filter(perfil -> !mesmoHeroi(
                    perfil.heroi(),
                    nomeAmeaca
                ))
                .filter(perfil -> !mesmoHeroi(
                    perfil.heroi(),
                    nomeIniciador
                ))
                .max(Comparator.comparingInt(perfil ->
                    perfil.habilitacao()
                        + (conectado(
                            sinergias,
                            perfil.heroi(),
                            nomeAmeaca
                        ) ? 20 : 0)
                ))
                .orElse(null);
            if (alternativo != null) {
                habilitadorRefinado = alternativo;
            }
        }

        List<PerfilAmeacaHeroiResponse> perfis = reclassificarPapeis(
            original.perfis(),
            habilitadorOriginal,
            habilitadorRefinado
        );
        PerfilAmeacaHeroiResponse ameaca = buscarPerfil(
            perfis,
            ameacaOriginal.heroi()
        );
        PerfilAmeacaHeroiResponse iniciador = iniciadorOriginal == null
            ? null
            : buscarPerfil(perfis, iniciadorOriginal.heroi());
        PerfilAmeacaHeroiResponse habilitador = habilitadorRefinado == null
            ? null
            : buscarPerfil(perfis, habilitadorRefinado.heroi());
        PerfilAmeacaHeroiResponse protetor = original.protetorPrincipal() == null
            ? null
            : buscarPerfil(
                perfis,
                original.protetorPrincipal().heroi()
            );
        PerfilAmeacaHeroiResponse eloFraco = original.eloFraco() == null
            ? null
            : buscarPerfil(perfis, original.eloFraco().heroi());

        List<AlvoPrioritarioAmeacaResponse> alvos = reconstruirAlvos(
            ameaca,
            protetor,
            iniciador,
            habilitador,
            eloFraco,
            sinergias
        );

        return new AnaliseAmeacasResponse(
            ameaca,
            protetor,
            iniciador,
            habilitador,
            eloFraco,
            perfis,
            alvos,
            planoResposta(ameaca, alvos)
        );
    }

    private List<PerfilAmeacaHeroiResponse> reclassificarPapeis(
        List<PerfilAmeacaHeroiResponse> perfis,
        PerfilAmeacaHeroiResponse habilitadorAnterior,
        PerfilAmeacaHeroiResponse novoHabilitador
    ) {
        if (
            habilitadorAnterior == null
                || novoHabilitador == null
                || mesmoHeroi(
                    habilitadorAnterior.heroi(),
                    novoHabilitador.heroi()
                )
        ) {
            return perfis;
        }

        return perfis.stream()
            .map(perfil -> {
                LinkedHashSet<PapelAmeaca> papeis = new LinkedHashSet<>(
                    perfil.papeis()
                );
                if (mesmoHeroi(
                    perfil.heroi(),
                    habilitadorAnterior.heroi()
                )) {
                    papeis.remove(PapelAmeaca.HABILITADOR);
                }
                if (mesmoHeroi(
                    perfil.heroi(),
                    novoHabilitador.heroi()
                )) {
                    papeis.add(PapelAmeaca.HABILITADOR);
                }
                return new PerfilAmeacaHeroiResponse(
                    perfil.heroi(),
                    perfil.potencialVitoria(),
                    perfil.protecao(),
                    perfil.iniciacao(),
                    perfil.habilitacao(),
                    perfil.vulnerabilidade(),
                    List.copyOf(papeis),
                    perfil.motivos()
                );
            })
            .toList();
    }

    private List<AlvoPrioritarioAmeacaResponse> reconstruirAlvos(
        PerfilAmeacaHeroiResponse ameaca,
        PerfilAmeacaHeroiResponse protetor,
        PerfilAmeacaHeroiResponse iniciador,
        PerfilAmeacaHeroiResponse habilitador,
        PerfilAmeacaHeroiResponse eloFraco,
        List<SinergiaGrupoResponse> sinergias
    ) {
        List<AlvoPrioritarioAmeacaResponse> candidatos = new ArrayList<>();
        boolean ameacaProtegida = conectado(
            sinergias,
            ameaca.heroi(),
            iniciador == null ? null : iniciador.heroi()
        ) || conectado(
            sinergias,
            ameaca.heroi(),
            habilitador == null ? null : habilitador.heroi()
        );
        candidatos.add(alvo(
            ameaca,
            PapelAmeaca.AMEACA_PRINCIPAL,
            ameaca.potencialVitoria() - (ameacaProtegida ? 8 : 0)
        ));
        if (iniciador != null) {
            candidatos.add(alvo(
                iniciador,
                PapelAmeaca.INICIADOR,
                iniciador.iniciacao()
                    + (conectado(
                        sinergias,
                        iniciador.heroi(),
                        ameaca.heroi()
                    ) ? 30 : 8)
            ));
        }
        if (habilitador != null) {
            candidatos.add(alvo(
                habilitador,
                PapelAmeaca.HABILITADOR,
                habilitador.habilitacao()
                    + (conectado(
                        sinergias,
                        habilitador.heroi(),
                        ameaca.heroi()
                    ) ? 22 : 5)
            ));
        }
        if (protetor != null) {
            candidatos.add(alvo(
                protetor,
                PapelAmeaca.PROTETOR,
                protetor.protecao()
                    + (conectado(
                        sinergias,
                        protetor.heroi(),
                        ameaca.heroi()
                    ) ? 10 : 0)
            ));
        }
        if (eloFraco != null) {
            candidatos.add(alvo(
                eloFraco,
                PapelAmeaca.ELO_FRACO,
                eloFraco.vulnerabilidade()
            ));
        }

        Map<String, AlvoPrioritarioAmeacaResponse> unicos =
            new LinkedHashMap<>();
        candidatos.forEach(candidato -> unicos.merge(
            normalizar(candidato.heroi()),
            candidato,
            (atual, novo) -> atual.prioridade() >= novo.prioridade()
                ? atual
                : novo
        ));
        return unicos.values().stream()
            .sorted(
                Comparator.comparingInt(
                    AlvoPrioritarioAmeacaResponse::prioridade
                ).reversed()
            )
            .limit(4)
            .toList();
    }

    private AlvoPrioritarioAmeacaResponse alvo(
        PerfilAmeacaHeroiResponse perfil,
        PapelAmeaca papel,
        int prioridade
    ) {
        String justificativa = switch (papel) {
            case AMEACA_PRINCIPAL ->
                perfil.heroi()
                    + " possui o maior potencial individual de vencer a partida.";
            case INICIADOR ->
                perfil.heroi()
                    + " cria a janela de entrada que permite ao dano principal funcionar.";
            case HABILITADOR ->
                perfil.heroi()
                    + " amplifica, conecta ou permite repetir a condição de vitória inimiga.";
            case PROTETOR ->
                perfil.heroi()
                    + " mantém o carregador vivo e preserva sua janela de dano.";
            case ELO_FRACO ->
                perfil.heroi()
                    + " apresenta a maior exposição ou dependência explorável.";
        };
        return new AlvoPrioritarioAmeacaResponse(
            perfil.heroi(),
            papel,
            limitar(prioridade, 0, 100),
            respostas(papel),
            justificativa
        );
    }

    private List<DimensaoEstrategica> respostas(PapelAmeaca papel) {
        return switch (papel) {
            case AMEACA_PRINCIPAL -> List.of(
                DimensaoEstrategica.CONTROLE,
                DimensaoEstrategica.DIVE,
                DimensaoEstrategica.EXPLOSAO,
                DimensaoEstrategica.MOBILIDADE
            );
            case INICIADOR -> List.of(
                DimensaoEstrategica.DESENGAGE,
                DimensaoEstrategica.PEEL,
                DimensaoEstrategica.CONTROLE,
                DimensaoEstrategica.MOBILIDADE
            );
            case HABILITADOR -> List.of(
                DimensaoEstrategica.CONTROLE,
                DimensaoEstrategica.EXPLOSAO,
                DimensaoEstrategica.MOBILIDADE,
                DimensaoEstrategica.ALCANCE
            );
            case PROTETOR -> List.of(
                DimensaoEstrategica.ANTI_CURA,
                DimensaoEstrategica.DIVE,
                DimensaoEstrategica.CONTROLE,
                DimensaoEstrategica.EXPLOSAO
            );
            case ELO_FRACO -> List.of(
                DimensaoEstrategica.EXPLOSAO,
                DimensaoEstrategica.DIVE,
                DimensaoEstrategica.ALCANCE
            );
        };
    }

    private String planoResposta(
        PerfilAmeacaHeroiResponse ameaca,
        List<AlvoPrioritarioAmeacaResponse> alvos
    ) {
        if (alvos.isEmpty()) {
            return "Não foi possível estabelecer uma prioridade de resposta.";
        }
        AlvoPrioritarioAmeacaResponse primeiro = alvos.getFirst();
        if (!mesmoHeroi(primeiro.heroi(), ameaca.heroi())) {
            return "Embora " + ameaca.heroi()
                + " seja a maior ameaça, a resposta mais eficiente começa por "
                + primeiro.heroi() + ". " + primeiro.justificativa()
                + " Neutralizar essa peça reduz a janela do carregador antes do confronto direto.";
        }
        return "A prioridade é limitar " + ameaca.heroi()
            + " diretamente antes que alcance sua janela de dano e escalamento.";
    }

    private PerfilAmeacaHeroiResponse buscarPerfil(
        List<PerfilAmeacaHeroiResponse> perfis,
        String nome
    ) {
        return perfis.stream()
            .filter(perfil -> mesmoHeroi(perfil.heroi(), nome))
            .findFirst()
            .orElseThrow();
    }

    private boolean conectado(
        List<SinergiaGrupoResponse> sinergias,
        String primeiro,
        String segundo
    ) {
        if (primeiro == null || segundo == null) {
            return false;
        }
        return sinergias.stream()
            .filter(SinergiaGrupoResponse::ativa)
            .anyMatch(sinergia ->
                contemNome(sinergia.membros(), primeiro)
                    && contemNome(sinergia.membros(), segundo)
            );
    }

    private boolean contemNome(List<String> nomes, String procurado) {
        return nomes.stream().anyMatch(nome -> mesmoHeroi(nome, procurado));
    }

    private boolean mesmoHeroi(String primeiro, String segundo) {
        return normalizar(primeiro).equals(normalizar(segundo));
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

    private List<String> copiarNomes(List<String> nomes) {
        return nomes == null ? List.of() : List.copyOf(nomes);
    }

    private record ChaveDiagnostico(
        List<String> aliados,
        List<String> inimigos
    ) {
    }

    private record ChaveRecomendacao(
        List<String> aliados,
        List<String> inimigos,
        Rota rota,
        int limite
    ) {
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
