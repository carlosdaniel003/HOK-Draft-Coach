# Base de conhecimento — Roaming / Suporte — S15

## Objetivo

Esta base responde principalmente:

1. Qual suporte combina melhor com o atirador já escolhido?
2. Qual suporte quebra os arquétipos da composição inimiga?
3. Qual suporte supre a necessidade da composição aliada?
4. Qual é o nível de confiança da informação usada na recomendação?

## Versão

- Versão interna: `S15-HOK-PLUS-2.0`
- Data de referência: `2026-07-02`
- Região principal: Honor of Kings Global
- Tiers e relações estratégicas: referências não oficiais

## Suportes cadastrados

Annette, Lapulapu, Cai Yan, Da Qiao, Dolia, Donghuang, Dun, Dyadia,
Guiguzi, Kui, Lian Po, Liu Bang, Liu Shan, Ming, Mozi, Sakeer, Sun Bin,
Xiang Yu, Yaria, Yuhuan, Zhang Fei, Zhuangzi e Ziya.

## Tier de roaming usado como referência

| Tier | Heróis |
|---|---|
| S | Dyadia, Dolia, Mozi, Yaria, Lian Po, Zhang Fei, Da Qiao |
| A | Annette, Liu Shan, Donghuang, Cai Yan, Zhuangzi, Ming, Sun Bin, Sakeer |
| B | Guiguzi, Kui, Lapulapu |
| C | Ziya, Dun, Xiang Yu, Liu Bang, Yuhuan |

O tier não substitui o contexto do draft. Um herói de tier inferior pode ser a melhor escolha quando responde diretamente à composição inimiga. Zhuangzi, por exemplo, recebe prioridade elevada contra controle pesado, independentemente de uma comparação genérica de força.

## Fontes de referência

- Pocket Gamer — tier list de Honor of Kings, atualização de 2 de julho de 2026, versão 11.4.1.1.
- HOKStats — páginas de heróis, parceiros, counters e funções.
- Site oficial de Honor of Kings — nomes e classes dos heróis.
- Discussões de jogadores de alto elo — usadas somente como sinais exploratórios ou para explicar interações específicas.

Os dados comunitários nunca são marcados como oficiais. As notas de `1 a 10` são heurísticas internas e devem ser recalibradas quando houver dados confiáveis de partidas por dupla.

## Níveis de confiança das duplas

- `ALTA`: interação mecânica clara e confirmada por mais de uma referência ou pelo funcionamento direto dos kits.
- `MEDIA`: interação consistente, mas ainda sem amostra estatística oficial de partidas da dupla.
- `EXPLORATORIA`: hipótese estratégica válida que precisa de mais dados ou testes em partidas.

## Modelo de análise de suporte

Cada suporte recebe:

- arquétipos funcionais;
- composições aliadas que fortalece;
- composições inimigas que quebra;
- composições contra as quais sofre;
- proteção do carry;
- iniciação;
- desengage;
- sustentação;
- pressão de rota;
- mobilidade de mapa;
- dependência de coordenação.

Os valores variam de `0` a `10`.

## Arquétipos de composição

- `DIVE`
- `ENGAGE_AGRUPADO`
- `POKE`
- `EXPLOSAO`
- `LUTAS_LONGAS`
- `FRONT_TO_BACK`
- `HIPERCARREGADOR`
- `ESCUDOS_E_CURA`
- `ALVOS_IMOVEIS`
- `ALTA_MOBILIDADE`
- `CONTROLE_PESADO`
- `SPLIT_PUSH`
- `ULTIMATES_DECISIVAS`
- `PICKOFF`
- `DESENGAGE`

## Marco Polo

A matriz inicial prioriza:

1. Lian Po — nota 10
2. Lapulapu — nota 9
3. Sakeer — nota 9
4. Da Qiao — nota 9
5. Zhang Fei — nota 9
6. Yaria — nota 9
7. Dolia — nota 8
8. Liu Shan — nota 8

A lógica principal é criar uma janela segura para a ultimate, manter os inimigos dentro da área e proteger o ADC durante ou após a entrada.

## Endpoints

### Listar os suportes

```http
GET /api/suportes
```

### Consultar um suporte

```http
GET /api/suportes/Lian%20Po
```

### Melhores suportes para um ADC

```http
GET /api/suportes/recomendados?atirador=Marco%20Polo&limite=10
```

### Melhores ADCs para um suporte

```http
GET /api/suportes/Dolia/atiradores?limite=10
```

### Analisar uma dupla

```http
GET /api/suportes/sinergia?suporte=Lian%20Po&atirador=Marco%20Polo
```

### Recomendar contra uma composição

```http
GET /api/suportes/contra-composicao?tipos=DIVE,CONTROLE_PESADO&limite=10
```

## Integração com o motor

`ConhecimentoSuporteService` expõe dois métodos destinados ao motor principal:

- `pontuarSinergia(Heroi candidato, List<Heroi> aliados)`
- `pontuarRespostaAosInimigos(Heroi candidato, List<Heroi> inimigos)`

O catálogo completo já é fornecido como implementação `@Primary` de `HeroiService`. Assim, todos os suportes participam da inferência e da lista de candidatos. A etapa seguinte é incluir explicitamente os dois bônus curados na composição final da nota do próximo pick.
