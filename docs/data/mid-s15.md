# Base de conhecimento — Mid Lane — S15

## Objetivo

A base de Mid responde quatro perguntas diferentes:

1. Qual mid encaixa melhor na composição aliada?
2. Qual mid responde melhor ao plano da composição inimiga?
3. Qual herói possui vantagem no confronto direto da rota?
4. Quais aliados transformam o kit do mid em um combo completo?

Tier, confronto e composição são tratados separadamente. Um herói de tier inferior pode ser a melhor escolha quando countera diretamente o mid inimigo ou resolve uma deficiência específica da equipe.

## Versão

- Versão interna: `S15-HOK-PLUS-2.0`
- Data de referência: `2026-07-02`
- Região principal: Honor of Kings Global
- Fonte-base de tier: `HOKSTATS_TIER_LIST`
- Dados não oficiais

## Mid laners cadastrados

Ser do Fluxo (Mago), Garuda, Lorion, Angela, Da Qiao, Daji, Diaochan,
Dr Bian Qe, Gan & Mo, Gao Jianli, Haya, Heino, Kongming, Lady Zhen,
Liang, Mai Shiranui, Milady, Mozi, Nuwa, Shangguan, Shi, Sima Yi,
Wang Zhaojun, Xiao Qiao, Yixing, Zhou Yu, Ziya e Yuhuan.

Yuhuan foi mantida porque a base anterior já a reconhecia como flex Mid/Roaming e a lista S15 utilizada possui 28 opções de Mid.

## Tier de referência

| Tier | Heróis |
|---|---|
| S | Lorion, Angela, Daji, Liang, Milady |
| A | Haya, Kongming, Mozi, Wang Zhaojun, Xiao Qiao |
| B | Garuda, Da Qiao, Diaochan, Heino, Lady Zhen, Mai Shiranui, Nuwa, Sima Yi, Yixing, Ziya |
| C | Ser do Fluxo (Mago), Dr Bian Qe, Gan & Mo, Gao Jianli, Shangguan, Shi, Yuhuan, Zhou Yu |

## Arquétipos de Mid

- `ARTILHEIRO`
- `CONTROLADOR`
- `MAGO_DE_BATALHA`
- `ASSASSINO_MAGICO`
- `EXPLOSAO`
- `POKE`
- `ZONEAMENTO`
- `PICKOFF`
- `CERCO`
- `PUSHER`
- `ROAMER`
- `ANTI_DIVE`
- `SUSTENTACAO`
- `ESCALAMENTO`
- `UTILITARIO`

## Tipos de vantagem no confronto

- `ROTA`: pressão, limpeza, trocas ou prioridade.
- `MECANICA`: uma habilidade responde diretamente ao kit inimigo.
- `ROTACAO`: capacidade de sair da rota e impactar o mapa.
- `LUTA`: vantagem em team fights ou escaramuças.
- `MAPA`: torres, objetivos, visão, terreno ou alcance global.
- `ESCALAMENTO`: vantagem conforme itens e níveis avançam.
- `PICKOFF`: capacidade de isolar e eliminar antes da luta.

Os confrontos são direcionais. `A > B` não implica que `B` seja inútil; indica apenas que A possui uma vantagem cadastrada em determinado contexto.

## Exemplos de confrontos cadastrados

- Angela contra Diaochan: burst antes da luta prolongada.
- Milady contra Mai Shiranui: pressão de onda e torre contra rotações.
- Haya contra Milady: mobilidade e pressão constante.
- Lady Zhen contra Sima Yi: controle em área contra entrada previsível.
- Liang contra Kongming, Mai Shiranui, Shangguan e Sima Yi: supressão contra mobilidade.
- Sima Yi contra Gan & Mo, Nuwa, Xiao Qiao e Ziya: acesso direto à artilharia imóvel.
- Yixing contra Gao Jianli e Diaochan: confinamento e zoneamento.
- Mozi contra Shangguan e Gao Jianli: interrupção à distância.

## Perfil estratégico

Cada mid recebe notas de `0 a 10` para:

- pressão de rota;
- wave clear;
- rotação;
- controle;
- poke;
- explosão;
- dano sustentado;
- segurança;
- dependência de coordenação.

Também recebe listas de:

- composições que fortalece;
- composições que quebra;
- composições contra as quais sofre.

## Combos importantes

### Agrupamento e dano em área

- Lian Po + Yixing
- Lian Po + Wang Zhaojun
- Guiguzi + Lorion
- Guiguzi + Gan & Mo
- Guiguzi + Mai Shiranui

### Alvo imóvel e artilharia

- Kui + Angela
- Kui + Gan & Mo
- Kui + Zhou Yu
- Donghuang + Xiao Qiao
- Donghuang + Ziya

### Reset de ultimate

- Dolia + Yixing
- Dolia + Wang Zhaojun
- Dolia + Gan & Mo
- Dolia + Lorion
- Dolia + Diaochan

### Dive e proteção

- Liu Bang + Gao Jianli
- Zhang Fei + Sima Yi
- Cai Yan + Shangguan
- Guiguzi + Kongming

### Macro e cerco

- Da Qiao + Nuwa
- Da Qiao + Zhou Yu
- Da Qiao + Ziya
- Sun Bin + Milady
- Liu Shan + Milady

## Níveis de confiança

- `ALTA`: interação mecânica clara ou apoiada por referências consistentes.
- `MEDIA`: interação estratégica forte, mas sem estatística oficial de dupla.
- `EXPLORATORIA`: hipótese coerente que ainda exige mais partidas e dados.

## Endpoints

### Listar todos os mids

```http
GET /api/mids
```

### Consultar um mid

```http
GET /api/mids/Liang
```

### Encontrar counters para um mid inimigo

```http
GET /api/mids/counters?inimigo=Kongming&limite=10
```

### Analisar confronto específico

```http
GET /api/mids/confronto?candidato=Liang&inimigo=Kongming
```

### Consultar melhores combos de um mid

```http
GET /api/mids/Yixing/combos?limite=10
```

### Consultar mids que combinam com um aliado

```http
GET /api/mids/combos?aliado=Lian%20Po&limite=10
```

### Analisar combo específico

```http
GET /api/mids/combo?mid=Gan%20%26%20Mo&aliado=Kui
```

### Recomendar contra uma composição

```http
GET /api/mids/contra-composicao?tipos=DIVE,ALTA_MOBILIDADE&limite=10
```

## Integração com o motor

`ConhecimentoMidService` disponibiliza:

- `pontuarConfronto(Heroi candidato, Heroi adversario)`
- `pontuarSinergia(Heroi candidato, List<Heroi> aliados)`
- `pontuarRespostaAosInimigos(Heroi candidato, List<Heroi> inimigos)`

Essas pontuações ficam separadas para que o motor consiga explicar se a recomendação veio do matchup, do combo aliado ou da resposta à composição inimiga.
