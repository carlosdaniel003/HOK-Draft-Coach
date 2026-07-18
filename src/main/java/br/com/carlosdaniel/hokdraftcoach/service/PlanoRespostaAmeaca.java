package br.com.carlosdaniel.hokdraftcoach.service;

import java.util.List;

import br.com.carlosdaniel.hokdraftcoach.dto.AlvoPrioritarioAmeacaResponse;
import br.com.carlosdaniel.hokdraftcoach.dto.PerfilAmeacaHeroiResponse;
import br.com.carlosdaniel.hokdraftcoach.model.PapelAmeaca;

final class PlanoRespostaAmeaca {

    private static final int MARGEM_TROCA_FOCO = 10;

    private PlanoRespostaAmeaca() {
    }

    static int prioridade(
        PapelAmeaca papel,
        int valorPapel,
        int vulnerabilidade,
        boolean conectado,
        boolean protegido
    ) {
        int calculada = switch (papel) {
            case AMEACA_PRINCIPAL -> valorPapel
                + Math.min(8, vulnerabilidade / 12)
                - (protegido ? 3 : 0);
            case INICIADOR -> (int) Math.round(valorPapel * 0.72)
                + (conectado ? 10 : 3)
                + Math.min(7, vulnerabilidade / 15);
            case HABILITADOR -> (int) Math.round(valorPapel * 0.68)
                + (conectado ? 9 : 2)
                + Math.min(8, vulnerabilidade / 12);
            case PROTETOR -> (int) Math.round(valorPapel * 0.64)
                + (conectado ? 7 : 1)
                + Math.min(8, vulnerabilidade / 12);
            case ELO_FRACO -> (int) Math.round(vulnerabilidade * 0.60);
        };
        return Math.max(0, Math.min(100, calculada));
    }

    static String criar(
        PerfilAmeacaHeroiResponse ameaca,
        List<AlvoPrioritarioAmeacaResponse> alvos
    ) {
        if (ameaca == null || alvos == null || alvos.isEmpty()) {
            return "Não foi possível estabelecer uma prioridade de resposta.";
        }

        AlvoPrioritarioAmeacaResponse alvoAmeaca = alvos.stream()
            .filter(alvo -> alvo.papel() == PapelAmeaca.AMEACA_PRINCIPAL)
            .findFirst()
            .orElseGet(() -> alvos.stream()
                .filter(alvo -> mesmoHeroi(alvo.heroi(), ameaca.heroi()))
                .findFirst()
                .orElse(alvos.getFirst()));
        AlvoPrioritarioAmeacaResponse primeiro = alvos.getFirst();
        AlvoPrioritarioAmeacaResponse foco = primeiro;

        if (
            primeiro.papel() != PapelAmeaca.AMEACA_PRINCIPAL
                && primeiro.prioridade() - alvoAmeaca.prioridade()
                    < MARGEM_TROCA_FOCO
        ) {
            foco = alvoAmeaca;
        }

        return switch (foco.papel()) {
            case AMEACA_PRINCIPAL -> planoContraAmeaca(
                ameaca,
                buscar(alvos, PapelAmeaca.INICIADOR)
            );
            case INICIADOR -> planoContraIniciador(foco, ameaca);
            case HABILITADOR -> planoContraHabilitador(foco, ameaca);
            case PROTETOR -> planoContraProtetor(foco, ameaca);
            case ELO_FRACO -> planoContraEloFraco(foco, ameaca);
        };
    }

    private static String planoContraAmeaca(
        PerfilAmeacaHeroiResponse ameaca,
        AlvoPrioritarioAmeacaResponse iniciador
    ) {
        String origemJanela = iniciador == null
            || mesmoHeroi(iniciador.heroi(), ameaca.heroi())
                ? "quando tentar ocupar uma posição segura"
                : "depois da entrada de " + iniciador.heroi();
        return "Janela a negar: não permita que " + ameaca.heroi()
            + " cause dano livre " + origemJanela + ". "
            + "Recurso a guardar: preserve controle duro, peel ou mobilidade para a primeira ativação do carregador, em vez de descarregar tudo na linha de frente. "
            + "Conversão: depois de forçar recuo ou habilidade defensiva de "
            + ameaca.heroi() + ", transforme a vantagem em objetivo.";
    }

    private static String planoContraIniciador(
        AlvoPrioritarioAmeacaResponse iniciador,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: absorva ou desengaje a primeira entrada de "
            + iniciador.heroi() + "; não é necessário eliminá-lo. "
            + "Recurso a guardar: mantenha peel, controle e reposicionamento para interromper o combo inicial. "
            + "Conversão: com as habilidades de entrada em recarga, avance sobre "
            + ameaca.heroi() + " ou force o objetivo antes da próxima iniciação.";
    }

    private static String planoContraHabilitador(
        AlvoPrioritarioAmeacaResponse habilitador,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: separe " + habilitador.heroi() + " de "
            + ameaca.heroi()
            + " e force sua utilidade principal antes do confronto decisivo. "
            + "Recurso a guardar: use controle ou zoneamento para negar cura, amplificação, conexão ou reset; não persiga o suporte se isso liberar o carregador. "
            + "Conversão: quando a utilidade estiver indisponível, troque o foco para "
            + ameaca.heroi() + ".";
    }

    private static String planoContraProtetor(
        AlvoPrioritarioAmeacaResponse protetor,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: force " + protetor.heroi()
            + " a gastar escudo, cura ou peel em um alvo secundário. "
            + "Recurso a guardar: preserve anti-cura, controle e uma segunda entrada; não gaste o combo inteiro no protetor. "
            + "Conversão: depois que a proteção sair, mude imediatamente para "
            + ameaca.heroi() + ".";
    }

    private static String planoContraEloFraco(
        AlvoPrioritarioAmeacaResponse eloFraco,
        PerfilAmeacaHeroiResponse ameaca
    ) {
        return "Janela a negar: puna " + eloFraco.heroi()
            + " apenas quando estiver isolado e sem cobertura. "
            + "Recurso a guardar: não comprometa ultimates ou mobilidade demais em uma isca, pois "
            + ameaca.heroi() + " continua sendo a condição principal. "
            + "Conversão: transforme a vantagem numérica em visão ou objetivo e reinicie a luta em posição favorável.";
    }

    private static AlvoPrioritarioAmeacaResponse buscar(
        List<AlvoPrioritarioAmeacaResponse> alvos,
        PapelAmeaca papel
    ) {
        return alvos.stream()
            .filter(alvo -> alvo.papel() == papel)
            .findFirst()
            .orElse(null);
    }

    private static boolean mesmoHeroi(String primeiro, String segundo) {
        return primeiro != null
            && segundo != null
            && primeiro.equalsIgnoreCase(segundo);
    }
}
