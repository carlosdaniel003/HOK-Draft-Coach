package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.EnumMap;
import java.util.Locale;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.model.AtributosHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.DimensaoEstrategica;
import br.com.carlosdaniel.hokdraftcoach.model.DnaHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;

@Service
public class DnaHeroiService {

    public DnaHeroi calcular(Heroi heroi) {
        AtributosHeroi a = heroi.getAtributos();
        EnumMap<DimensaoEstrategica, Integer> vetor =
            new EnumMap<>(DimensaoEstrategica.class);

        vetor.put(DimensaoEstrategica.CONTROLE, limitar(
            a.controle() * 10 + bonus(heroi, 8, "controle pesado", "supressao")
        ));
        vetor.put(DimensaoEstrategica.MOBILIDADE, limitar(
            a.mobilidade() * 10 + bonus(heroi, 8, "global", "teleporte")
        ));
        vetor.put(DimensaoEstrategica.ALCANCE, limitar(
            a.alcance() * 10 + bonus(heroi, 8, "artilharia", "longo alcance")
        ));
        vetor.put(DimensaoEstrategica.DPS, limitar(
            a.danoSustentado() * 10
                + bonus(heroi, 8, "velocidade de ataque", "dano verdadeiro")
        ));
        vetor.put(DimensaoEstrategica.EXPLOSAO, limitar(
            a.danoExplosivo() * 10 + bonus(heroi, 8, "execucao", "burst")
        ));

        vetor.put(DimensaoEstrategica.ENGAGE, pontuar(
            a.controle() * 4.0
                + a.mobilidade() * 2.0
                + a.resistencia() * 2.0
                + a.danoExplosivo(),
            heroi,
            18,
            "iniciacao", "engage", "provocacao", "gancho", "global"
        ));
        vetor.put(DimensaoEstrategica.DESENGAGE, pontuar(
            a.controle() * 3.5
                + a.mobilidade() * 2.0
                + a.alcance() * 2.0
                + a.resistencia() * 1.5,
            heroi,
            20,
            "desengage", "purificacao", "anti-controle", "deslocamento"
        ));
        vetor.put(DimensaoEstrategica.PEEL, pontuar(
            a.controle() * 4.0
                + a.resistencia() * 3.0
                + a.alcance() * 1.5,
            heroi,
            20,
            "peel", "protecao", "escudo", "anti-dive", "guardiao"
        ));
        vetor.put(DimensaoEstrategica.POKE, pontuar(
            a.alcance() * 4.0
                + a.danoExplosivo() * 3.0
                + a.controle() * 1.5,
            heroi,
            18,
            "poke", "artilharia", "cerco", "longo alcance"
        ));
        vetor.put(DimensaoEstrategica.LINHA_DE_FRENTE, pontuar(
            a.resistencia() * 7.0
                + a.controle() * 1.5
                + a.danoSustentado() * 0.5,
            heroi,
            heroi.getClasse() == ClasseHeroi.TANQUE ? 18 : 12,
            "tanque", "linha de frente", "frontline", "ressurreicao"
        ));
        vetor.put(DimensaoEstrategica.SUSTAIN, pontuar(
            a.resistencia() * 3.5
                + a.danoSustentado() * 2.5
                + a.mobilidade() * 1.5,
            heroi,
            25,
            "sustentacao", "cura", "roubo de vida", "recuperacao",
            "escudo", "ressurreicao"
        ));
        vetor.put(DimensaoEstrategica.WAVE_CLEAR, pontuar(
            a.danoSustentado() * 3.5
                + a.danoExplosivo() * 3.0
                + a.alcance() * 2.0,
            heroi,
            18,
            "limpeza", "wave clear", "dano em area", "artilharia", "cerco"
        ));
        vetor.put(DimensaoEstrategica.OBJETIVOS, pontuar(
            a.danoSustentado() * 4.0
                + a.resistencia() * 2.5
                + a.controle() * 1.5
                + a.danoExplosivo(),
            heroi,
            18,
            "objetivos", "controle de objetivos", "invasao", "torres"
        ));
        vetor.put(DimensaoEstrategica.ANTI_TANQUE, pontuar(
            a.danoSustentado() * 4.5
                + a.danoExplosivo() * 2.0
                + a.mobilidade() * 1.5,
            heroi,
            28,
            "anti-tanque", "dano verdadeiro", "dano percentual", "execucao"
        ));
        vetor.put(DimensaoEstrategica.ANTI_CURA, antiCura(heroi));
        vetor.put(DimensaoEstrategica.PRESSAO_LATERAL, pontuar(
            a.danoSustentado() * 3.0
                + a.mobilidade() * 2.5
                + a.resistencia() * 1.5
                + a.danoExplosivo() * 1.5,
            heroi,
            24,
            "split push", "pressao lateral", "torres", "macro", "global"
        ));
        vetor.put(DimensaoEstrategica.DIVE, pontuar(
            a.mobilidade() * 3.5
                + a.danoExplosivo() * 3.0
                + a.resistencia() * 1.5
                + a.controle(),
            heroi,
            18,
            "dive", "mergulho", "assassino", "flanco", "acesso global"
        ));
        vetor.put(DimensaoEstrategica.PROTECAO, pontuar(
            a.controle() * 3.0
                + a.resistencia() * 2.5
                + a.alcance() * 1.5,
            heroi,
            28,
            "protecao", "escudo", "cura", "amplificacao", "guardiao"
        ));
        vetor.put(DimensaoEstrategica.ESCALAMENTO, pontuar(
            a.danoSustentado() * 4.0
                + a.alcance() * 2.0
                + a.mobilidade() * 1.5
                + a.danoExplosivo(),
            heroi,
            20,
            "escalamento", "fim de jogo", "hipercarregador", "carregadora"
        ));

        return new DnaHeroi(heroi.getNome(), vetor);
    }

    private int antiCura(Heroi heroi) {
        if (temTag(heroi, "anti-cura", "reducao de cura")) {
            return 92;
        }
        if (temTag(heroi, "execucao")) {
            return 38;
        }
        return 8;
    }

    private int pontuar(
        double base,
        Heroi heroi,
        int valorBonus,
        String... tags
    ) {
        return limitar((int) Math.round(base) + bonus(heroi, valorBonus, tags));
    }

    private int bonus(Heroi heroi, int valor, String... tags) {
        return temTag(heroi, tags) ? valor : 0;
    }

    private boolean temTag(Heroi heroi, String... tags) {
        return heroi.getCaracteristicas()
            .stream()
            .map(this::normalizar)
            .anyMatch(caracteristica -> {
                for (String tag : tags) {
                    String procurada = normalizar(tag);
                    if (
                        caracteristica.equals(procurada)
                            || caracteristica.contains(procurada)
                    ) {
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

    private int limitar(int valor) {
        return Math.max(0, Math.min(100, valor));
    }
}
