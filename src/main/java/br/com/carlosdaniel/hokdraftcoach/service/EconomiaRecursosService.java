package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.EconomiaComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilEconomicoHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.NivelDependenciaRecursos;
import br.com.carlosdaniel.hokdraftcoach.model.Rota;

@Service
public class EconomiaRecursosService {

    public EconomiaComposicaoResponse analisar(List<Heroi> equipe) {
        if (equipe == null || equipe.isEmpty()) {
            return EconomiaComposicaoResponse.vazia();
        }

        List<PerfilEconomicoHeroiResponse> perfis = equipe.stream()
            .map(this::perfil)
            .sorted(
                Comparator.comparingInt(
                    PerfilEconomicoHeroiResponse::dependenciaRecursos
                ).reversed()
            )
            .toList();
        int dependentes = (int) perfis.stream()
            .filter(perfil -> perfil.dependenciaRecursos() >= 70)
            .count();
        double mediaTresMaiores = perfis.stream()
            .limit(3)
            .mapToInt(PerfilEconomicoHeroiResponse::dependenciaRecursos)
            .average()
            .orElse(0);
        int carga = limitar((int) Math.round(mediaTresMaiores), 0, 100);
        int conflito = 0;

        if (dependentes >= 3) {
            conflito += 35 + (dependentes - 3) * 15;
        }
        if (carga >= 78 && equipe.size() >= 3) {
            conflito += 20;
        } else if (carga >= 68 && equipe.size() >= 3) {
            conflito += 10;
        }
        long cedemRecursos = perfis.stream()
            .filter(PerfilEconomicoHeroiResponse::consegueCederRecursos)
            .count();
        if (equipe.size() >= 4 && cedemRecursos == 0) {
            conflito += 20;
        }
        conflito = limitar(conflito, 0, 100);

        boolean viavel = conflito < 55;
        String diagnostico;
        if (conflito >= 75) {
            diagnostico = "Conflito crítico: vários heróis precisam ser a prioridade de ouro ao mesmo tempo.";
        } else if (conflito >= 55) {
            diagnostico = "Economia instável: a composição possui mais carregadores dependentes do que fontes seguras de recursos.";
        } else if (conflito >= 30) {
            diagnostico = "Economia exigente: é necessário definir claramente quem receberá farm prioritário.";
        } else {
            diagnostico = "Economia viável: existem heróis capazes de funcionar cedendo recursos aos carregadores.";
        }

        return new EconomiaComposicaoResponse(
            carga,
            conflito,
            dependentes,
            viavel,
            diagnostico,
            perfis
        );
    }

    public PerfilEconomicoHeroiResponse perfil(Heroi heroi) {
        int dependencia = 12;
        List<String> motivos = new ArrayList<>();

        dependencia += heroi.getAtributos().danoSustentado() * 4;
        dependencia += heroi.getAtributos().danoExplosivo() * 2;
        dependencia += Math.max(0, heroi.getAtributos().mobilidade() - 5);

        if (heroi.getClasse() == ClasseHeroi.ATIRADOR) {
            dependencia += 15;
            motivos.add("Atiradores normalmente convertem ouro diretamente em DPS.");
        }
        if (heroi.getClasse() == ClasseHeroi.ASSASSINO) {
            dependencia += 9;
            motivos.add("Assassinos precisam manter vantagem de itens para executar alvos.");
        }
        if (
            heroi.getRota() == Rota.JUNGLE
                && Math.max(
                    heroi.getAtributos().danoSustentado(),
                    heroi.getAtributos().danoExplosivo()
                ) >= 8
        ) {
            dependencia += 10;
            motivos.add("Jungler carregador disputa campos e eliminações como fonte principal de ouro.");
        }
        if (possuiTag(
            heroi,
            "fim de jogo",
            "escalamento",
            "hipercarregador",
            "carregadora",
            "carregador"
        )) {
            dependencia += 18;
            motivos.add("O pico de força depende de níveis e itens avançados.");
        }
        if (possuiTag(
            heroi,
            "duelo",
            "split push",
            "dano verdadeiro",
            "velocidade de ataque"
        )) {
            dependencia += 7;
        }

        if (heroi.getClasse() == ClasseHeroi.SUPORTE) {
            dependencia -= 30;
            motivos.add("O kit entrega utilidade mesmo com baixa prioridade de farm.");
        }
        if (heroi.getClasse() == ClasseHeroi.TANQUE) {
            dependencia -= 22;
            motivos.add("Controle e resistência continuam úteis com poucos recursos.");
        }
        if (possuiTag(
            heroi,
            "proteção",
            "controle",
            "iniciação",
            "desengage",
            "cura",
            "escudo",
            "utilidade",
            "visão",
            "macro"
        )) {
            dependencia -= 12;
            motivos.add("Parte relevante do valor está no kit, não apenas nos itens.");
        }
        if (possuiTag(heroi, "início de jogo", "pressão")) {
            dependencia -= 6;
        }

        dependencia = limitar(dependencia, 5, 100);
        int utilidadeSemOuro = 100 - dependencia;
        utilidadeSemOuro += heroi.getAtributos().controle() * 3;
        utilidadeSemOuro += heroi.getAtributos().resistencia() * 2;
        if (possuiTag(
            heroi,
            "proteção",
            "cura",
            "escudo",
            "desengage",
            "visão",
            "macro",
            "teleporte"
        )) {
            utilidadeSemOuro += 15;
        }
        utilidadeSemOuro = limitar(utilidadeSemOuro, 0, 100);

        return new PerfilEconomicoHeroiResponse(
            heroi.getNome(),
            dependencia,
            NivelDependenciaRecursos.classificar(dependencia),
            utilidadeSemOuro,
            dependencia <= 52 || utilidadeSemOuro >= 70,
            motivos.stream().distinct().limit(5).toList()
        );
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

    private int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
