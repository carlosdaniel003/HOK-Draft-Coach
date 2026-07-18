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
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaComposicao;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.RegraAntiSinergia;
import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;
import br.com.carlosdaniel.hokdraftcoach.model.TipoAntiSinergia;
import br.com.carlosdaniel.hokdraftcoach.repository.AntiSinergiaRepository;

@Service
public class AntiSinergiaService {

    private static final Set<String> EMPURRADORES = Set.of(
        "xiangyu",
        "guanyu"
    );
    private static final Set<String> ULTIMATES_DE_ZONA = Set.of(
        "marcopolo",
        "wangzhaojun",
        "gaojianli",
        "huangzhong",
        "ladyzhen",
        "yixing"
    );

    private final AntiSinergiaRepository repository;
    private final DnaHeroiService dnaHeroiService;

    public AntiSinergiaService(
        AntiSinergiaRepository repository,
        DnaHeroiService dnaHeroiService
    ) {
        this.repository = repository;
        this.dnaHeroiService = dnaHeroiService;
    }

    public List<AntiSinergiaResponse> analisar(
        List<Heroi> equipe,
        DnaComposicao dna
    ) {
        if (equipe == null || equipe.size() < 2) {
            return List.of();
        }

        List<AntiSinergiaResponse> resultado = new ArrayList<>();
        adicionarExplicitas(resultado, equipe);
        adicionarMobilidadeIncompativel(resultado, equipe);
        adicionarRitmosIncompativeis(resultado, equipe, dna);
        adicionarDeslocamentosGenericos(resultado, equipe);

        return resultado.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    AntiSinergiaResponse::codigo,
                    resposta -> resposta,
                    (primeira, ignorada) -> primeira,
                    java.util.LinkedHashMap::new
                )
            )
            .values()
            .stream()
            .sorted(
                Comparator.comparingInt(AntiSinergiaResponse::penalidade)
                    .reversed()
            )
            .toList();
    }

    public int totalPenalidade(List<AntiSinergiaResponse> respostas) {
        return Math.min(
            30,
            respostas.stream().mapToInt(AntiSinergiaResponse::penalidade).sum()
        );
    }

    public int penalidadeAoAdicionar(
        List<AntiSinergiaResponse> antes,
        List<AntiSinergiaResponse> depois
    ) {
        Set<String> codigosAntes = new LinkedHashSet<>();
        antes.stream().map(AntiSinergiaResponse::codigo).forEach(codigosAntes::add);
        return Math.min(
            20,
            depois.stream()
                .filter(resposta -> !codigosAntes.contains(resposta.codigo()))
                .mapToInt(AntiSinergiaResponse::penalidade)
                .sum()
        );
    }

    private void adicionarExplicitas(
        List<AntiSinergiaResponse> resultado,
        List<Heroi> equipe
    ) {
        for (RegraAntiSinergia regra : repository.listar()) {
            List<String> encontrados = regra.heroisObrigatorios().stream()
                .map(nome -> equipe.stream()
                    .filter(heroi -> heroi.correspondeAoNome(nome))
                    .findFirst()
                    .map(Heroi::getNome)
                    .orElse(null))
                .filter(nome -> nome != null)
                .toList();
            if (encontrados.size() == regra.heroisObrigatorios().size()) {
                resultado.add(new AntiSinergiaResponse(
                    regra.codigo(),
                    regra.tipo(),
                    severidade(regra.penalidade()),
                    regra.penalidade(),
                    encontrados,
                    regra.descricao(),
                    regra.mitigacao()
                ));
            }
        }
    }

    private void adicionarMobilidadeIncompativel(
        List<AntiSinergiaResponse> resultado,
        List<Heroi> equipe
    ) {
        List<Heroi> adcsMoveis = equipe.stream()
            .filter(heroi -> heroi.getClasse() == ClasseHeroi.ATIRADOR)
            .filter(heroi -> heroi.getAtributos().mobilidade() >= 8)
            .toList();
        List<Heroi> suportesEstaticos = equipe.stream()
            .filter(heroi -> heroi.getClasse() == ClasseHeroi.SUPORTE)
            .filter(heroi -> heroi.getAtributos().mobilidade() <= 4)
            .filter(heroi -> !possuiTag(
                heroi,
                "mobilidade compartilhada",
                "global",
                "teleporte",
                "aceleração",
                "aceleracao",
                "anexar"
            ))
            .toList();

        for (Heroi adc : adcsMoveis) {
            for (Heroi suporte : suportesEstaticos) {
                String codigo = "MOBILIDADE_INCOMPATIVEL_"
                    + normalizar(suporte.getNome()).toUpperCase(Locale.ROOT)
                    + "_"
                    + normalizar(adc.getNome()).toUpperCase(Locale.ROOT);
                resultado.add(new AntiSinergiaResponse(
                    codigo,
                    TipoAntiSinergia.MOBILIDADE_INCOMPATIVEL,
                    SeveridadeDiagnostico.ATENCAO,
                    8,
                    List.of(suporte.getNome(), adc.getNome()),
                    adc.getNome()
                        + " se reposiciona além do alcance de "
                        + suporte.getNome()
                        + ", reduzindo a consistência de cura, escudo ou proteção.",
                    "Encurtar as entradas do ADC, guardar recursos para a saída ou escolher um suporte capaz de acompanhar deslocamentos."
                ));
            }
        }
    }

    private void adicionarRitmosIncompativeis(
        List<AntiSinergiaResponse> resultado,
        List<Heroi> equipe,
        DnaComposicao dna
    ) {
        List<String> poke = equipe.stream()
            .filter(heroi ->
                dnaHeroiService.calcular(heroi).valor(DimensaoEstrategica.POKE)
                    >= 75
            )
            .map(Heroi::getNome)
            .toList();
        List<String> allIn = equipe.stream()
            .filter(heroi -> {
                DnaHeroi dnaHeroi = dnaHeroiService.calcular(heroi);
                return dnaHeroi.valor(DimensaoEstrategica.DIVE) >= 72
                    || dnaHeroi.valor(DimensaoEstrategica.ENGAGE) >= 75;
            })
            .map(Heroi::getNome)
            .toList();

        if (
            equipe.size() >= 4
                && poke.size() >= 2
                && allIn.size() >= 2
                && dna.valor(DimensaoEstrategica.SUSTAIN) < 48
                && dna.valor(DimensaoEstrategica.PROTECAO) < 50
        ) {
            List<String> envolvidos = new ArrayList<>(poke);
            allIn.stream().filter(nome -> !envolvidos.contains(nome))
                .forEach(envolvidos::add);
            resultado.add(new AntiSinergiaResponse(
                "RITMO_POKE_VERSUS_ALL_IN",
                TipoAntiSinergia.RITMOS_INCOMPATIVEIS,
                SeveridadeDiagnostico.ATENCAO,
                9,
                envolvidos,
                "Parte da equipe precisa desgastar antes da luta, enquanto outra parte ganha valor entrando imediatamente. Sem proteção ou sustain, um plano tende a abandonar o outro.",
                "Definir quem inicia apenas após o poke criar vantagem ou adicionar uma peça que conecte os planos com controle, frontline ou sustain."
            ));
        }
    }

    private void adicionarDeslocamentosGenericos(
        List<AntiSinergiaResponse> resultado,
        List<Heroi> equipe
    ) {
        List<Heroi> empurradores = equipe.stream()
            .filter(heroi -> EMPURRADORES.contains(normalizar(heroi.getNome())))
            .toList();
        List<Heroi> zonas = equipe.stream()
            .filter(heroi -> ULTIMATES_DE_ZONA.contains(normalizar(heroi.getNome())))
            .toList();

        for (Heroi empurrador : empurradores) {
            for (Heroi zona : zonas) {
                boolean jaCadastrada = repository.listar().stream().anyMatch(regra ->
                    regra.heroisObrigatorios().stream()
                        .allMatch(nome ->
                            empurrador.correspondeAoNome(nome)
                                || zona.correspondeAoNome(nome)
                        )
                );
                if (jaCadastrada) {
                    continue;
                }
                resultado.add(new AntiSinergiaResponse(
                    "DESLOCAMENTO_ZONA_"
                        + normalizar(empurrador.getNome()).toUpperCase(Locale.ROOT)
                        + "_"
                        + normalizar(zona.getNome()).toUpperCase(Locale.ROOT),
                    TipoAntiSinergia.DESLOCAMENTO_QUE_QUEBRA_COMBO,
                    SeveridadeDiagnostico.ATENCAO,
                    7,
                    List.of(empurrador.getNome(), zona.getNome()),
                    "O deslocamento de " + empurrador.getNome()
                        + " pode retirar inimigos da zona de maior dano de "
                        + zona.getNome() + ".",
                    "Usar o deslocamento para devolver os inimigos à área ou somente depois que a habilidade de zona terminar."
                ));
            }
        }
    }

    private SeveridadeDiagnostico severidade(int penalidade) {
        if (penalidade >= 16) {
            return SeveridadeDiagnostico.ALTA;
        }
        if (penalidade >= 8) {
            return SeveridadeDiagnostico.ATENCAO;
        }
        return SeveridadeDiagnostico.INFO;
    }

    private boolean possuiTag(Heroi heroi, String... tags) {
        return heroi.getCaracteristicas().stream()
            .map(this::normalizar)
            .anyMatch(caracteristica -> {
                for (String tag : tags) {
                    if (caracteristica.contains(normalizar(tag))) {
                        return true;
                    }
                }
                return false;
            });
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
}
