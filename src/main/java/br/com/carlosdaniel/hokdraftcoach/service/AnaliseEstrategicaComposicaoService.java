package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.CondicaoVitoriaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.EconomiaComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.NecessidadeComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PenalidadeComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilEconomicoHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoDnaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.RespostaCondicaoVitoriaResponse;
import br.com.carlosdaniel.hokdraftcoach.exception.RegraNegocioException;
import br.com.carlosdaniel.hokdraftcoach.model.CondicaoVitoriaTipo;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
@Primary
public class AnaliseEstrategicaComposicaoService
    extends DnaComposicaoService {

    private final HeroiService heroiService;
    private final DnaHeroiService dnaHeroiService;
    private final CondicaoVitoriaService condicaoVitoriaService;
    private final EconomiaRecursosService economiaRecursosService;
    private final NecessidadePenalidadeService necessidadePenalidadeService;

    public AnaliseEstrategicaComposicaoService(
        HeroiService heroiService,
        DnaHeroiService dnaHeroiService,
        CondicaoVitoriaService condicaoVitoriaService,
        EconomiaRecursosService economiaRecursosService,
        NecessidadePenalidadeService necessidadePenalidadeService
    ) {
        super(heroiService, dnaHeroiService);
        this.heroiService = heroiService;
        this.dnaHeroiService = dnaHeroiService;
        this.condicaoVitoriaService = condicaoVitoriaService;
        this.economiaRecursosService = economiaRecursosService;
        this.necessidadePenalidadeService = necessidadePenalidadeService;
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
        EconomiaComposicaoResponse nossaEconomia =
            economiaRecursosService.analisar(nossaEquipe);
        EconomiaComposicaoResponse economiaInimiga =
            economiaRecursosService.analisar(equipeInimiga);
        List<CondicaoVitoriaResponse> nossasCondicoes =
            condicaoVitoriaService.descobrir(
                nossaEquipe,
                base.nossaComposicao()
            );
        List<CondicaoVitoriaResponse> condicoesInimigas =
            condicaoVitoriaService.descobrir(
                equipeInimiga,
                base.composicaoInimiga()
            );
        List<NecessidadeComposicaoResponse> necessidades =
            necessidadePenalidadeService.necessidades(
                base.nossaComposicao(),
                base.prioridades(),
                nossaEconomia
            );
        List<PenalidadeComposicaoResponse> penalidades =
            necessidadePenalidadeService.penalidades(
                nossaEquipe,
                base.nossaComposicao(),
                nossaEconomia
            );

        return new DiagnosticoComposicaoResponse(
            base.nossaComposicao(),
            base.composicaoInimiga(),
            base.diagnosticos(),
            base.prioridades(),
            nossasCondicoes,
            condicoesInimigas,
            necessidades,
            penalidades,
            nossaEconomia,
            economiaInimiga,
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
        int penalidadeAtual = necessidadePenalidadeService.totalPenalidades(
            diagnostico.penalidades()
        );
        int conflitoAtual = diagnostico.economiaNossaComposicao()
            .conflitoDeRecursos();

        return base.stream()
            .map(recomendacao -> ajustarCandidato(
                recomendacao,
                nossaEquipe,
                diagnostico,
                penalidadeAtual,
                conflitoAtual
            ))
            .sorted(
                Comparator.comparingInt(RecomendacaoDnaResponse::pontuacao)
                    .reversed()
                    .thenComparing(RecomendacaoDnaResponse::heroi)
            )
            .limit(limite)
            .toList();
    }

    private RecomendacaoDnaResponse ajustarCandidato(
        RecomendacaoDnaResponse base,
        List<Heroi> nossaEquipe,
        DiagnosticoComposicaoResponse diagnostico,
        int penalidadeAtual,
        int conflitoAtual
    ) {
        Heroi candidato = heroiService.buscarPorNome(base.heroi())
            .orElseThrow(() -> new RegraNegocioException(
                "Herói não encontrado: " + base.heroi() + "."
            ));
        PerfilEconomicoHeroiResponse perfil = economiaRecursosService.perfil(
            candidato
        );
        List<Heroi> projetada = new ArrayList<>(nossaEquipe);
        projetada.add(candidato);
        DnaComposicao dnaProjetado = gerarDna(projetada);
        EconomiaComposicaoResponse economiaProjetada =
            economiaRecursosService.analisar(projetada);
        List<PenalidadeComposicaoResponse> penalidadesProjetadas =
            necessidadePenalidadeService.penalidades(
                projetada,
                dnaProjetado,
                economiaProjetada
            );
        int penalidadeProjetada =
            necessidadePenalidadeService.totalPenalidades(
                penalidadesProjetadas
            );
        int novaRedundancia = Math.max(
            0,
            penalidadeProjetada - penalidadeAtual
        );
        int ajusteEconomico = calcularAjusteEconomico(
            perfil,
            diagnostico.economiaNossaComposicao(),
            economiaProjetada,
            conflitoAtual
        );
        int bonusCondicao = bonusCondicoes(
            candidato,
            diagnostico.nossasCondicoesVitoria(),
            diagnostico.condicoesVitoriaInimigas()
        );
        int pontuacao = limitar(
            base.pontuacao()
                + ajusteEconomico
                + bonusCondicao
                - novaRedundancia,
            0,
            100
        );

        List<String> motivos = new ArrayList<>(base.motivos());
        List<String> alertas = new ArrayList<>();
        if (ajusteEconomico > 0) {
            motivos.add(
                candidato.getNome()
                    + " funciona com poucos recursos e preserva ouro para os carregadores atuais."
            );
        } else if (ajusteEconomico < 0) {
            alertas.add(
                candidato.getNome()
                    + " aumenta a disputa por ouro entre os carregadores."
            );
        }
        if (bonusCondicao > 0) {
            motivos.add(
                "A escolha fortalece nossa condição de vitória ou responde diretamente ao plano inimigo."
            );
        }
        if (novaRedundancia > 0) {
            alertas.add(
                "A escolha adiciona " + novaRedundancia
                    + " pontos de penalidade por redundância estratégica."
            );
        }
        penalidadesProjetadas.stream()
            .filter(penalidade -> diagnostico.penalidades().stream()
                .noneMatch(atual -> atual.codigo().equals(penalidade.codigo())))
            .map(PenalidadeComposicaoResponse::titulo)
            .limit(3)
            .forEach(alertas::add);

        return new RecomendacaoDnaResponse(
            base.heroi(),
            base.rota(),
            pontuacao,
            base.corrige(),
            base.explora(),
            perfil.dependenciaRecursos(),
            ajusteEconomico,
            novaRedundancia,
            motivos.stream().distinct().limit(8).toList(),
            alertas.stream().distinct().limit(5).toList()
        );
    }

    private int calcularAjusteEconomico(
        PerfilEconomicoHeroiResponse candidato,
        EconomiaComposicaoResponse atual,
        EconomiaComposicaoResponse projetada,
        int conflitoAtual
    ) {
        int ajuste = 0;
        if (
            atual.carregadoresDependentes() >= 2
                && candidato.consegueCederRecursos()
        ) {
            ajuste += 10;
        }
        if (
            atual.carregadoresDependentes() >= 2
                && candidato.dependenciaRecursos() >= 70
        ) {
            ajuste -= 12;
        }
        int aumentoConflito = Math.max(
            0,
            projetada.conflitoDeRecursos() - conflitoAtual
        );
        ajuste -= Math.min(15, aumentoConflito / 3);
        if (
            atual.conflitoDeRecursos() >= 45
                && candidato.dependenciaRecursos() <= 40
        ) {
            ajuste += 6;
        }
        return limitar(ajuste, -20, 16);
    }

    private int bonusCondicoes(
        Heroi candidato,
        List<CondicaoVitoriaResponse> nossas,
        List<CondicaoVitoriaResponse> inimigas
    ) {
        DnaHeroi dna = dnaHeroiService.calcular(candidato);
        int bonus = 0;

        CondicaoVitoriaResponse principalNossa = nossas.stream()
            .filter(CondicaoVitoriaResponse::principal)
            .findFirst()
            .orElse(null);
        if (principalNossa != null && fortalece(principalNossa.tipo(), dna)) {
            bonus += 6;
        }

        CondicaoVitoriaResponse principalInimiga = inimigas.stream()
            .filter(CondicaoVitoriaResponse::principal)
            .findFirst()
            .orElse(null);
        if (principalInimiga != null) {
            boolean responde = principalInimiga.respostasAdversarias().stream()
                .anyMatch(resposta -> atendeResposta(dna, resposta));
            if (responde) {
                bonus += 8;
            }
        }
        return limitar(bonus, 0, 14);
    }

    private boolean fortalece(
        CondicaoVitoriaTipo tipo,
        DnaHeroi dna
    ) {
        return switch (tipo) {
            case PROTEGER_HIPERCARREGADOR -> maximo(
                dna,
                DimensaoEstrategica.PEEL,
                DimensaoEstrategica.PROTECAO,
                DimensaoEstrategica.LINHA_DE_FRENTE
            ) >= 62;
            case SPLIT_PUSH ->
                dna.valor(DimensaoEstrategica.PRESSAO_LATERAL) >= 65;
            case CERCO_DE_TORRES -> maximo(
                dna,
                DimensaoEstrategica.POKE,
                DimensaoEstrategica.WAVE_CLEAR,
                DimensaoEstrategica.ALCANCE
            ) >= 68;
            case LUTAS_LONGAS -> maximo(
                dna,
                DimensaoEstrategica.DPS,
                DimensaoEstrategica.SUSTAIN,
                DimensaoEstrategica.LINHA_DE_FRENTE
            ) >= 65;
            case PICKOFF ->
                dna.valor(DimensaoEstrategica.CONTROLE) >= 65
                    && dna.valor(DimensaoEstrategica.EXPLOSAO) >= 55;
            case DIVE_NA_RETAGUARDA ->
                dna.valor(DimensaoEstrategica.DIVE) >= 65;
            case WOMBO_COMBO ->
                dna.valor(DimensaoEstrategica.ENGAGE) >= 65
                    || dna.valor(DimensaoEstrategica.EXPLOSAO) >= 70;
            case CONTROLE_DE_OBJETIVOS ->
                dna.valor(DimensaoEstrategica.OBJETIVOS) >= 65;
            case ESCALAMENTO_TARDIO ->
                dna.valor(DimensaoEstrategica.ESCALAMENTO) >= 68;
        };
    }

    private boolean atendeResposta(
        DnaHeroi dna,
        RespostaCondicaoVitoriaResponse resposta
    ) {
        if (resposta.capacidadesNecessarias().isEmpty()) {
            return false;
        }
        double media = resposta.capacidadesNecessarias().stream()
            .mapToInt(dna::valor)
            .average()
            .orElse(0);
        int maior = resposta.capacidadesNecessarias().stream()
            .mapToInt(dna::valor)
            .max()
            .orElse(0);
        return media >= 58 || maior >= 72;
    }

    private int maximo(
        DnaHeroi dna,
        DimensaoEstrategica... dimensoes
    ) {
        int maior = 0;
        for (DimensaoEstrategica dimensao : dimensoes) {
            maior = Math.max(maior, dna.valor(dimensao));
        }
        return maior;
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
