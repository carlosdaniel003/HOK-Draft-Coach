package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.SinergiaGrupoResponse;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.RegraSinergiaGrupo;
import br.com.carlosdaniel.hokdraftcoach.model.RequisitoSinergiaGrupo;
import br.com.carlosdaniel.hokdraftcoach.repository.SinergiaGrupoRepository;

@Service
public class SinergiaGrupoService {

    private final SinergiaGrupoRepository repository;
    private final DnaHeroiService dnaHeroiService;

    public SinergiaGrupoService(
        SinergiaGrupoRepository repository,
        DnaHeroiService dnaHeroiService
    ) {
        this.repository = repository;
        this.dnaHeroiService = dnaHeroiService;
    }

    public List<SinergiaGrupoResponse> avaliar(List<Heroi> equipe) {
        if (equipe == null || equipe.isEmpty()) {
            return List.of();
        }

        return repository.listar().stream()
            .map(regra -> avaliarRegra(regra, equipe))
            .filter(resposta -> resposta.ativa() || resposta.faltando().size() <= 1)
            .sorted(
                Comparator.comparing(SinergiaGrupoResponse::ativa)
                    .reversed()
                    .thenComparing(
                        Comparator.comparingInt(SinergiaGrupoResponse::nota)
                            .reversed()
                    )
                    .thenComparing(SinergiaGrupoResponse::codigo)
            )
            .toList();
    }

    public List<SinergiaGrupoResponse> ativas(List<Heroi> equipe) {
        return avaliar(equipe).stream()
            .filter(SinergiaGrupoResponse::ativa)
            .toList();
    }

    public int bonusAoAdicionar(
        List<SinergiaGrupoResponse> antes,
        List<SinergiaGrupoResponse> depois
    ) {
        Set<String> ativasAntes = new LinkedHashSet<>();
        antes.stream()
            .filter(SinergiaGrupoResponse::ativa)
            .map(SinergiaGrupoResponse::codigo)
            .forEach(ativasAntes::add);

        int bonus = depois.stream()
            .filter(SinergiaGrupoResponse::ativa)
            .filter(resposta -> !ativasAntes.contains(resposta.codigo()))
            .mapToInt(resposta -> {
                if (resposta.nota() >= 95) {
                    return 15;
                }
                if (resposta.nota() >= 88) {
                    return 12;
                }
                return 9;
            })
            .sum();

        if (bonus == 0) {
            Set<String> potenciaisAntes = new LinkedHashSet<>();
            antes.stream()
                .filter(resposta -> !resposta.ativa())
                .map(SinergiaGrupoResponse::codigo)
                .forEach(potenciaisAntes::add);
            boolean criouPotencial = depois.stream()
                .anyMatch(resposta ->
                    !resposta.ativa()
                        && resposta.faltando().size() == 1
                        && !potenciaisAntes.contains(resposta.codigo())
                );
            if (criouPotencial) {
                bonus = 3;
            }
        }

        return Math.min(18, bonus);
    }

    private SinergiaGrupoResponse avaliarRegra(
        RegraSinergiaGrupo regra,
        List<Heroi> equipe
    ) {
        List<String> membros = new ArrayList<>();
        List<String> faltando = new ArrayList<>();
        Set<String> utilizados = new LinkedHashSet<>();

        for (String obrigatorio : regra.heroisObrigatorios()) {
            Heroi encontrado = equipe.stream()
                .filter(heroi -> heroi.correspondeAoNome(obrigatorio))
                .findFirst()
                .orElse(null);
            if (encontrado == null) {
                faltando.add(obrigatorio);
            } else {
                membros.add(encontrado.getNome());
                utilizados.add(normalizar(encontrado.getNome()));
            }
        }

        for (RequisitoSinergiaGrupo requisito : regra.requisitos()) {
            List<Heroi> candidatos = equipe.stream()
                .filter(heroi -> !utilizados.contains(normalizar(heroi.getNome())))
                .filter(heroi -> atende(heroi, requisito))
                .sorted(
                    Comparator.comparingInt(
                        (Heroi heroi) -> notaCorrespondencia(heroi, requisito)
                    ).reversed()
                )
                .toList();

            int encontrados = Math.min(requisito.quantidade(), candidatos.size());
            for (int indice = 0; indice < encontrados; indice++) {
                Heroi heroi = candidatos.get(indice);
                membros.add(heroi.getNome());
                utilizados.add(normalizar(heroi.getNome()));
            }
            for (int indice = encontrados; indice < requisito.quantidade(); indice++) {
                faltando.add(requisito.descricao());
            }
        }

        boolean ativa = faltando.isEmpty();
        int totalNecessario = regra.heroisObrigatorios().size()
            + regra.requisitos().stream()
                .mapToInt(RequisitoSinergiaGrupo::quantidade)
                .sum();
        int progresso = totalNecessario == 0
            ? 0
            : (int) Math.round(membros.size() * 100.0 / totalNecessario);
        int nota = ativa
            ? regra.nota()
            : Math.max(0, Math.min(regra.nota() - 1,
                (int) Math.round(regra.nota() * progresso / 100.0)));

        return new SinergiaGrupoResponse(
            regra.codigo(),
            regra.tipo(),
            nota,
            ativa,
            membros.stream().distinct().toList(),
            faltando,
            regra.descricao(),
            regra.sequencia(),
            regra.beneficios()
        );
    }

    private boolean atende(
        Heroi heroi,
        RequisitoSinergiaGrupo requisito
    ) {
        if (
            !requisito.classesAceitas().isEmpty()
                && !requisito.classesAceitas().contains(heroi.getClasse())
        ) {
            return false;
        }
        if (
            !requisito.tagsAceitas().isEmpty()
                && !possuiAlgumaTag(heroi, requisito.tagsAceitas())
        ) {
            return false;
        }
        if (requisito.dimensao() != null) {
            DnaHeroi dna = dnaHeroiService.calcular(heroi);
            if (dna.valor(requisito.dimensao()) < requisito.valorMinimo()) {
                return false;
            }
        }
        return true;
    }

    private int notaCorrespondencia(
        Heroi heroi,
        RequisitoSinergiaGrupo requisito
    ) {
        int nota = 0;
        if (
            !requisito.classesAceitas().isEmpty()
                && requisito.classesAceitas().contains(heroi.getClasse())
        ) {
            nota += 20;
        }
        if (!requisito.tagsAceitas().isEmpty()) {
            nota += (int) requisito.tagsAceitas().stream()
                .filter(tag -> possuiAlgumaTag(heroi, List.of(tag)))
                .count() * 10;
        }
        if (requisito.dimensao() != null) {
            nota += dnaHeroiService.calcular(heroi).valor(requisito.dimensao());
        }
        return nota;
    }

    private boolean possuiAlgumaTag(Heroi heroi, List<String> tags) {
        return heroi.getCaracteristicas().stream()
            .map(this::normalizar)
            .anyMatch(caracteristica -> tags.stream()
                .map(this::normalizar)
                .anyMatch(caracteristica::contains));
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
