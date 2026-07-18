package br.com.carlosdaniel.hokdraftcoach.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import br.com.carlosdaniel.hokdraftcoach.dto.CurvaPoderComposicaoResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.DiagnosticoTemporalResponse;
import br.com.carlosdaniel.hokdraftcoach.model.ClasseHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.FaseJogo;
import br.com.carlosdaniel.hokdraftcoach.model.Heroi;
import br.com.carlosdaniel.hokdraftcoach.model.PerfilTemporalHeroi;
import br.com.carlosdaniel.hokdraftcoach.model.SeveridadeDiagnostico;

@Service
public class PerfilTemporalService {

    public PerfilTemporalHeroi perfil(Heroi heroi) {
        int early = 38
            + heroi.getAtributos().controle() * 2
            + heroi.getAtributos().resistencia()
            + heroi.getAtributos().mobilidade()
            + heroi.getAtributos().danoExplosivo() * 2;
        int mid = 42
            + heroi.getAtributos().controle()
            + heroi.getAtributos().mobilidade() * 2
            + heroi.getAtributos().danoExplosivo() * 2
            + heroi.getAtributos().danoSustentado();
        int late = 30
            + heroi.getAtributos().danoSustentado() * 4
            + heroi.getAtributos().alcance() * 2
            + heroi.getAtributos().resistencia()
            + heroi.getAtributos().controle();
        List<String> motivos = new ArrayList<>();

        switch (heroi.getClasse()) {
            case ATIRADOR -> {
                early -= 8;
                mid += 2;
                late += 10;
                motivos.add("Atiradores convertem itens em grande crescimento de DPS.");
            }
            case ASSASSINO -> {
                early += 5;
                mid += 9;
                late -= 3;
                motivos.add("Assassinos concentram força nas janelas de rotação e execução.");
            }
            case MAGO -> {
                mid += 7;
                late += 4;
                motivos.add("Magos normalmente atingem pico após níveis e primeiros itens.");
            }
            case TANQUE -> {
                early += 8;
                mid += 9;
                late += 3;
                motivos.add("Controle e resistência entregam valor antes da economia completa.");
            }
            case SUPORTE -> {
                early += 8;
                mid += 8;
                late += 2;
                motivos.add("Utilidade de suporte funciona mesmo com poucos itens.");
            }
            case LUTADOR -> {
                early += 5;
                mid += 7;
                late += 1;
            }
            case HIBRIDO -> {
                mid += 4;
                late += 2;
            }
        }

        if (possuiTag(
            heroi,
            "início de jogo",
            "inicio de jogo",
            "pressão precoce",
            "pressao precoce",
            "snowball"
        )) {
            early += 20;
            mid += 5;
            late -= 8;
            motivos.add("O kit foi cadastrado como forte no início e orientado a snowball.");
        }
        if (possuiTag(
            heroi,
            "pressão",
            "invasão",
            "gank",
            "duelo",
            "pickoff"
        )) {
            early += 7;
            mid += 5;
        }
        if (possuiTag(
            heroi,
            "meio de jogo",
            "mid game",
            "rotação",
            "escaramuça",
            "objetivos"
        )) {
            mid += 15;
            motivos.add("A mobilidade e as rotações ampliam o pico no meio da partida.");
        }
        if (possuiTag(
            heroi,
            "fim de jogo",
            "late game",
            "escalamento",
            "hipercarregador",
            "carregadora",
            "carregador"
        )) {
            early -= 10;
            mid += 4;
            late += 20;
            motivos.add("O perfil depende de escalamento e alcança o maior valor no late game.");
        }
        if (possuiTag(
            heroi,
            "dano sustentado",
            "velocidade de ataque",
            "lutas longas",
            "dano verdadeiro"
        )) {
            mid += 4;
            late += 8;
        }
        if (possuiTag(
            heroi,
            "controle",
            "iniciação",
            "desengage",
            "proteção",
            "cura",
            "escudo",
            "macro"
        )) {
            early += 3;
            mid += 5;
            late += 4;
        }

        early = limitar(early);
        mid = limitar(mid);
        late = limitar(late);
        FaseJogo pico = fasePico(early, mid, late);

        return new PerfilTemporalHeroi(
            heroi.getNome(),
            early,
            mid,
            late,
            pico,
            motivos.stream().distinct().limit(5).toList()
        );
    }

    public CurvaPoderComposicaoResponse curva(List<Heroi> equipe) {
        if (equipe == null || equipe.isEmpty()) {
            return CurvaPoderComposicaoResponse.vazia();
        }

        List<PerfilTemporalHeroi> perfis = equipe.stream()
            .map(this::perfil)
            .sorted(Comparator.comparing(PerfilTemporalHeroi::heroi))
            .toList();
        int early = media(perfis, FaseJogo.EARLY_GAME);
        int mid = media(perfis, FaseJogo.MID_GAME);
        int late = media(perfis, FaseJogo.LATE_GAME);
        FaseJogo pico = fasePico(early, mid, late);
        List<String> alertas = new ArrayList<>();

        int maiorEarly = perfis.stream()
            .mapToInt(PerfilTemporalHeroi::earlyGame)
            .max()
            .orElse(0);
        if (early < 50 && maiorEarly < 58) {
            alertas.add(
                "Todos os aliados são fracos ou apenas medianos no início da partida."
            );
        }
        if (late - early >= 18) {
            alertas.add(
                "A composição cresce muito com o tempo e deve evitar perdas irreversíveis no início."
            );
        }
        if (early - late >= 15) {
            alertas.add(
                "A composição perde força relativa no late game e precisa converter pressão cedo."
            );
        }
        if (mid + 10 < Math.min(early, late)) {
            alertas.add(
                "Existe um vale de poder no meio da partida; lutas nesse período exigem cautela."
            );
        }

        return new CurvaPoderComposicaoResponse(
            early,
            mid,
            late,
            pico,
            planoTemporal(early, mid, late),
            alertas,
            perfis
        );
    }

    public List<DiagnosticoTemporalResponse> diagnosticarConfronto(
        CurvaPoderComposicaoResponse nossa,
        CurvaPoderComposicaoResponse inimiga
    ) {
        List<DiagnosticoTemporalResponse> resultado = new ArrayList<>();
        int maiorEarlyAliado = nossa.perfis().stream()
            .mapToInt(PerfilTemporalHeroi::earlyGame)
            .max()
            .orElse(0);

        if (nossa.earlyGame() < 50 && maiorEarlyAliado < 58) {
            resultado.add(new DiagnosticoTemporalResponse(
                "TODOS_FRACOS_NO_INICIO",
                SeveridadeDiagnostico.ALTA,
                88,
                "Todos os aliados são fracos no início",
                "A equipe possui pouca pressão de rota, invasão e resposta a objetivos iniciais. A próxima escolha deve estabilizar o early game.",
                FaseJogo.EARLY_GAME
            ));
        }

        if (inimiga == null || inimiga.perfis().isEmpty()) {
            return ordenarOuEquilibrar(resultado);
        }

        int deltaEarly = nossa.earlyGame() - inimiga.earlyGame();
        int deltaMid = nossa.midGame() - inimiga.midGame();
        int deltaLate = nossa.lateGame() - inimiga.lateGame();

        if (deltaEarly >= 8 && deltaLate <= -12) {
            resultado.add(new DiagnosticoTemporalResponse(
                "PRECISA_TERMINAR_CEDO",
                SeveridadeDiagnostico.CRITICA,
                96,
                "Nossa composição precisa terminar cedo",
                "Temos vantagem de " + deltaEarly
                    + " pontos no early game, mas ficamos " + Math.abs(deltaLate)
                    + " pontos atrás no late game. A condição temporal é converter pressão em torres e objetivos antes do escalamento inimigo.",
                FaseJogo.EARLY_GAME
            ));
        } else if (deltaLate <= -15 && deltaEarly < 5) {
            resultado.add(new DiagnosticoTemporalResponse(
                "ESCALA_MENOS_SEM_VANTAGEM_INICIAL",
                SeveridadeDiagnostico.CRITICA,
                94,
                "Escalamos menos e não possuímos vantagem inicial suficiente",
                "A equipe inimiga cresce mais e nossa janela inicial não compensa. A próxima escolha deve criar pressão imediata ou reduzir o déficit de late game.",
                deltaMid >= 5 ? FaseJogo.MID_GAME : FaseJogo.EARLY_GAME
            ));
        }

        if (deltaEarly <= -12) {
            resultado.add(new DiagnosticoTemporalResponse(
                "INIMIGO_MAIS_FORTE_NO_EARLY",
                SeveridadeDiagnostico.ALTA,
                86,
                "O inimigo possui uma janela inicial muito superior",
                "A diferença de early game é de " + Math.abs(deltaEarly)
                    + " pontos. Evitar disputas sem prioridade e considerar uma escolha capaz de contestar o início.",
                FaseJogo.EARLY_GAME
            ));
        }

        if (deltaLate >= 12) {
            resultado.add(new DiagnosticoTemporalResponse(
                "VANTAGEM_DE_ESCALAMENTO",
                SeveridadeDiagnostico.INFO,
                62,
                "Nossa composição escala melhor",
                "O late game aliado supera o inimigo em " + deltaLate
                    + " pontos. O plano deve priorizar estabilidade, economia e proteção aos carregadores.",
                FaseJogo.LATE_GAME
            ));
        }

        if (deltaMid >= 12 && deltaEarly < 8 && deltaLate < 8) {
            resultado.add(new DiagnosticoTemporalResponse(
                "JANELA_FORTE_NO_MEIO",
                SeveridadeDiagnostico.INFO,
                70,
                "Nossa principal janela está no meio da partida",
                "O pico de primeiros itens e rotações deve ser convertido em objetivos antes que a curva volte a se equilibrar.",
                FaseJogo.MID_GAME
            ));
        }

        return ordenarOuEquilibrar(resultado);
    }

    public int ajusteCandidato(
        CurvaPoderComposicaoResponse nossa,
        CurvaPoderComposicaoResponse inimiga,
        PerfilTemporalHeroi candidato
    ) {
        int ajuste = 0;
        int menorAtual = Math.min(
            nossa.earlyGame(),
            Math.min(nossa.midGame(), nossa.lateGame())
        );

        if (nossa.earlyGame() == menorAtual && nossa.earlyGame() < 55) {
            ajuste += faixa(candidato.earlyGame(), 50, 75, 0, 10);
        }
        if (nossa.midGame() == menorAtual && nossa.midGame() < 55) {
            ajuste += faixa(candidato.midGame(), 52, 78, 0, 7);
        }
        if (nossa.lateGame() == menorAtual && nossa.lateGame() < 55) {
            ajuste += faixa(candidato.lateGame(), 55, 82, 0, 7);
        }

        if (inimiga != null && !inimiga.perfis().isEmpty()) {
            int deltaEarly = nossa.earlyGame() - inimiga.earlyGame();
            int deltaLate = nossa.lateGame() - inimiga.lateGame();

            if (deltaEarly <= -10) {
                ajuste += faixa(candidato.earlyGame(), 55, 82, 0, 10);
            }
            if (deltaEarly >= 8 && deltaLate <= -12) {
                int ritmo = Math.round(
                    (candidato.earlyGame() + candidato.midGame()) / 2.0f
                );
                ajuste += faixa(ritmo, 58, 82, 0, 9);
                if (
                    candidato.lateGame() >= 80
                        && candidato.earlyGame() < 48
                ) {
                    ajuste -= 6;
                }
            } else if (deltaLate <= -15 && deltaEarly < 5) {
                ajuste += faixa(candidato.lateGame(), 62, 88, 0, 8);
                ajuste += faixa(candidato.earlyGame(), 55, 80, 0, 4);
            }
        }

        return Math.max(-10, Math.min(15, ajuste));
    }

    private List<DiagnosticoTemporalResponse> ordenarOuEquilibrar(
        List<DiagnosticoTemporalResponse> diagnosticos
    ) {
        if (diagnosticos.isEmpty()) {
            return List.of(new DiagnosticoTemporalResponse(
                "CURVA_TEMPORAL_EQUILIBRADA",
                SeveridadeDiagnostico.INFO,
                20,
                "Curva temporal equilibrada",
                "Nenhuma fase apresenta vantagem ou déficit crítico com as escolhas atuais.",
                FaseJogo.EQUILIBRADA
            ));
        }
        return diagnosticos.stream()
            .sorted(
                Comparator.comparingInt(
                    DiagnosticoTemporalResponse::prioridade
                ).reversed()
            )
            .toList();
    }

    private String planoTemporal(int early, int mid, int late) {
        FaseJogo pico = fasePico(early, mid, late);
        return switch (pico) {
            case EARLY_GAME ->
                "Acelerar o mapa, invadir e converter pressão em objetivos antes da queda relativa de poder.";
            case MID_GAME ->
                "Jogar pelas rotações e pelos primeiros picos de itens no meio da partida.";
            case LATE_GAME ->
                "Estabilizar o início, preservar recursos e alcançar os picos de itens do fim de jogo.";
            case EQUILIBRADA ->
                "A composição não depende de uma única fase e pode adaptar o ritmo ao confronto.";
        };
    }

    private FaseJogo fasePico(int early, int mid, int late) {
        int maior = Math.max(early, Math.max(mid, late));
        int menor = Math.min(early, Math.min(mid, late));
        if (maior - menor <= 6) {
            return FaseJogo.EQUILIBRADA;
        }
        if (early == maior) {
            return FaseJogo.EARLY_GAME;
        }
        if (mid == maior) {
            return FaseJogo.MID_GAME;
        }
        return FaseJogo.LATE_GAME;
    }

    private int media(List<PerfilTemporalHeroi> perfis, FaseJogo fase) {
        return limitar((int) Math.round(
            perfis.stream().mapToInt(perfil -> perfil.valor(fase)).average()
                .orElse(0)
        ));
    }

    private int faixa(
        int valor,
        int inicio,
        int fim,
        int minimo,
        int maximo
    ) {
        if (valor <= inicio) {
            return minimo;
        }
        if (valor >= fim) {
            return maximo;
        }
        double proporcao = (valor - inicio) / (double) (fim - inicio);
        return (int) Math.round(minimo + proporcao * (maximo - minimo));
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

    private int limitar(int valor) {
        return Math.max(0, Math.min(100, valor));
    }
}
