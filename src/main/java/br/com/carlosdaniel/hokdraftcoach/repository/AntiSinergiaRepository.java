package br.com.carlosdaniel.hokdraftcoach.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.carlosdaniel.hokdraftcoach.model.RegraAntiSinergia;
import br.com.carlosdaniel.hokdraftcoach.model.TipoAntiSinergia;

@Repository
public class AntiSinergiaRepository {

    private final List<RegraAntiSinergia> regras = List.of(
        new RegraAntiSinergia(
            "XIANG_YU_MARCO_POLO_DESLOCAMENTO",
            TipoAntiSinergia.DESLOCAMENTO_QUE_QUEBRA_COMBO,
            10,
            List.of("Xiang Yu", "Marco Polo"),
            "O deslocamento de Xiang Yu pode retirar inimigos da área da ultimate de Marco Polo quando as habilidades são usadas sem coordenação.",
            "Usar o deslocamento para empurrar os alvos em direção ao trajeto de Marco Polo ou guardar a habilidade para impedir a fuga."
        ),
        new RegraAntiSinergia(
            "XIANG_YU_WANG_ZHAOJUN_DESLOCAMENTO",
            TipoAntiSinergia.DESLOCAMENTO_QUE_QUEBRA_COMBO,
            10,
            List.of("Xiang Yu", "Wang Zhaojun"),
            "O empurrão pode remover alvos da zona congelada antes que a ultimate complete o dano e o controle.",
            "Congelar primeiro e usar o deslocamento apenas para devolver o inimigo à zona ou bloquear sua saída."
        ),
        new RegraAntiSinergia(
            "GUAN_YU_WANG_ZHAOJUN_DESLOCAMENTO",
            TipoAntiSinergia.DESLOCAMENTO_QUE_QUEBRA_COMBO,
            9,
            List.of("Guan Yu", "Wang Zhaojun"),
            "Entradas sucessivas de Guan Yu podem dispersar os alvos que Wang Zhaojun precisa manter agrupados na ultimate.",
            "Coordenar o primeiro empurrão para dentro da área e evitar atravessar novamente os alvos durante a canalização."
        ),
        new RegraAntiSinergia(
            "CAI_YAN_ARLI_MOBILIDADE",
            TipoAntiSinergia.MOBILIDADE_INCOMPATIVEL,
            9,
            List.of("Cai Yan", "Arli"),
            "Arli reposiciona muito além do alcance confortável de uma curadora estática, reduzindo a disponibilidade de cura e proteção.",
            "Arli deve retornar à zona de Cai Yan após as trocas ou a dupla precisa jogar com entradas mais curtas."
        ),
        new RegraAntiSinergia(
            "CAI_YAN_MARCO_POLO_MOBILIDADE",
            TipoAntiSinergia.MOBILIDADE_INCOMPATIVEL,
            8,
            List.of("Cai Yan", "Marco Polo"),
            "A entrada profunda da ultimate de Marco Polo pode romper a distância necessária para Cai Yan sustentar o ADC.",
            "Guardar a cura e o controle para a saída da ultimate ou iniciar somente quando a frontline já ocupar o espaço."
        )
    );

    public List<RegraAntiSinergia> listar() {
        return regras;
    }
}
