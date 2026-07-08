package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.RecomendacaoResponse;
import br.com.carlosdaniel.hokdraftcoach.model.Confronto;

@Service
public class RecomendacaoService {

    private final Map<String, List<Confronto>> confrontos = Map.of(
        "arli",
        List.of(
            new Confronto(
                "Arli",
                "Consort Yu",
                85,
                "Confronto provisório para validar o motor de recomendação."
            ),
            new Confronto(
                "Arli",
                "Garo",
                72,
                "Confronto provisório para testar uma recomendação de alcance."
            ),
            new Confronto(
                "Arli",
                "Lady Sun",
                61,
                "Confronto provisório para testar uma escolha situacional."
            )
        ),

        "hou yi",
        List.of(
            new Confronto(
                "Hou Yi",
                "Arli",
                82,
                "Confronto provisório para testar vantagem de mobilidade."
            ),
            new Confronto(
                "Hou Yi",
                "Marco Polo",
                74,
                "Confronto provisório para testar reposicionamento."
            ),
            new Confronto(
                "Hou Yi",
                "Alessio",
                63,
                "Confronto provisório para testar mobilidade aérea."
            )
        )
    );

    public List<RecomendacaoResponse> recomendarContra(String nomeInimigo) {
        String nomeNormalizado = normalizar(nomeInimigo);

        return confrontos
            .getOrDefault(nomeNormalizado, List.of())
            .stream()
            .sorted(
                Comparator.comparingInt(Confronto::pontuacao)
                    .reversed()
            )
            .map(this::converterParaResponse)
            .toList();
    }

    private RecomendacaoResponse converterParaResponse(
        Confronto confronto
    ) {
        return new RecomendacaoResponse(
            confronto.inimigo(),
            confronto.recomendado(),
            confronto.pontuacao(),
            confronto.calcularNivel(),
            confronto.motivo(),
            false
        );
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        return Normalizer
            .normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}