# Base de conhecimento — Clash Lane — S15

## Objetivo

A base de Clash Lane responde cinco perguntas diferentes:

1. Qual top laner possui vantagem no confronto direto?
2. Qual escolha controla melhor a rota e o split push?
3. Qual top quebra o plano da composição inimiga?
4. Qual top completa as necessidades estruturais da equipe?
5. Quais aliados transformam o kit do top em um combo decisivo?

Ganhar o X1 não significa automaticamente ser a melhor escolha para a partida. Pressão de lane, team fight, rotação, engage, linha de frente, split push e escalamento são avaliados separadamente.

## Versão

- Versão interna: `S15-HOK-PLUS-2.0`
- Data de referência: `2026-07-02`
- Região: Honor of Kings Global
- Fonte-base de tier: `HOKSTATS_TIER_LIST`
- Dados não oficiais

## Normalização da lista

- `Ser do Fluxo (Tanque)` usa os aliases `Flowborn (Tank)` e `Flowborn Tank`.
- `Lapulapu` aceita `Lapu-Lapu` e `Lapu Lapu`.
- `Fuzi` aceita `Old Master`.
- `Dun` aceita `Xiahou Dun`.
- `Musashi` aceita `Miyamoto Musashi`.
- `Dharma` foi incluído para completar os 39 Clash Laners da referência S15.

## Top laners cadastrados

Ata, Devara, Fatih, Florentino, Ser do Fluxo (Tanque), Lapulapu,
Allain, Arthur, Augran, Bai Qi, Biron, Charlotte, Chicha, Diaochan,
Donghuang, Dun, Fuzi, Guan Yu, Heino, Kaizer, Li Xin, Lian Po,
Liu Bang, Lu Bu, Mayene, Menki, Mi Yue, Mulan, Musashi, Nezha,
Sun Ce, Ukyo Tachibana, Umbrosa, Wuyan, Xiang Yu, Yang Jian,
Yao, Zhuangzi e Dharma.

## Tier de referência

| Tier | Heróis |
|---|---|
| S | Devara, Florentino, Augran, Li Xin |
| A | Chicha, Donghuang, Dun, Kaizer, Lian Po |
| B | Arthur, Biron, Charlotte, Diaochan, Heino, Lu Bu, Mi Yue, Musashi, Sun Ce, Xiang Yu, Zhuangzi |
| C | Ata, Fatih, Ser do Fluxo (Tanque), Lapulapu, Allain, Bai Qi, Dharma, Fuzi, Guan Yu, Liu Bang, Mayene, Menki, Mulan, Nezha, Ukyo Tachibana, Umbrosa, Wuyan, Yang Jian, Yao |

## Arquétipos da Clash Lane

- `DUELISTA`
- `BRUISER`
- `TANQUE`
- `JUGGERNAUT`
- `SPLIT_PUSHER`
- `INICIADOR`
- `FLANQUEADOR`
- `MAGO_DE_ROTA`
- `ANTI_TANQUE`
- `ANTI_CARRY`
- `SUSTENTACAO`
- `ESCALAMENTO`
- `CONTROLADOR`
- `GLOBAL`
- `UTILITARIO`

## Indicadores estratégicos

Cada top recebe notas de `0 a 10` para:

- pressão de rota;
- duelo;
- sustentação;
- wave clear;
- split push;
- rotação;
- engage;
- linha de frente;
- dano sustentado;
- explosão;
- escalamento;
- segurança;
- dependência de coordenação.

## Tipos de vantagem no confronto

- `ROTA`: prioridade, poke, sustain ou controle da onda.
- `DUELO`: vantagem no combate direto.
- `SUSTENTACAO`: capacidade de vencer por recuperação e trocas repetidas.
- `MECANICA`: uma habilidade responde diretamente ao kit adversário.
- `WAVE_CLEAR`: domínio da onda e liberdade para rotacionar.
- `SPLIT_PUSH`: capacidade de pressionar a lateral e estruturas.
- `ROTACAO`: impacto em outras rotas ou lutas distantes.
- `LUTA`: contribuição superior em team fights.
- `ESCALAMENTO`: vantagem conforme itens e níveis avançam.

Os confrontos são direcionais. Quando existem relações nas duas direções, o motor calcula o saldo das vantagens. Um counter mecânico parcial pode reduzir uma desvantagem sem transformar o herói no vencedor geral do matchup.

## Exemplos de confrontos cadastrados

### Duelistas

- Florentino contra Arthur, Ata, Dun, Lu Bu e Kaizer.
- Donghuang e Fuzi como respostas mecânicas a Florentino.
- Fuzi contra Guan Yu, Yao, Charlotte e Heino.
- Mi Yue contra Arthur, Kaizer e lutadores lineares.

### Anti-sustentação

- Musashi contra Fatih, Biron, Umbrosa, Mi Yue e Augran.
- Nezha contra Fatih, Umbrosa e Mi Yue.
- Augran contra tanques e linhas de frente resistentes.

### Controle contra mobilidade

- Arthur contra Diaochan, Yao e Mayene.
- Donghuang contra Florentino, Diaochan e Mulan.
- Wuyan contra Yao e Mayene.
- Dharma contra Guan Yu, Heino e Li Xin.

### Pressão lateral e macro

- Li Xin contra Liu Bang, Bai Qi e Zhuangzi.
- Guan Yu e Sun Ce contra tops dependentes de poke ou posicionamento.
- Liu Bang como resposta macro a iniciações em outras rotas.

### Anti-tanque

- Lu Bu contra Ata, Bai Qi e Menki.
- Allain contra tanques conforme escala.
- Diaochan contra frontlines sem controle confiável.
- Augran contra composições de front-to-back e sustain.

## Combos importantes

### Engage seguido de dano em área

- Ata + Marco Polo
- Bai Qi + Wang Zhaojun
- Bai Qi + Yixing
- Lian Po + Yixing
- Lian Po + Wang Zhaojun
- Lu Bu + Guiguzi
- Sun Ce + Gao Jianli
- Sun Ce + Marco Polo
- Wuyan + Wang Zhaojun
- Dharma + Wang Zhaojun

### Proteção de duelistas

- Florentino + Yaria
- Allain + Yaria
- Charlotte + Yaria
- Kaizer + Yaria
- Musashi + Yaria
- Yao + Yaria

### Amplificação

- Devara + Ming
- Allain + Ming
- Augran + Ming
- Kaizer + Ming
- Menki + Ming
- Mi Yue + Ming
- Yang Jian + Ming

### Macro e split push

- Fuzi + Da Qiao
- Guan Yu + Da Qiao
- Li Xin + Da Qiao
- Mi Yue + Da Qiao
- Nezha + Da Qiao
- Yao + Da Qiao
- Liu Bang + Gao Jianli
- Nezha + Liu Bang

### Reset de ultimate

- Devara + Dolia
- Florentino + Dolia
- Augran + Dolia
- Bai Qi + Dolia
- Diaochan + Dolia
- Dun + Dolia
- Kaizer + Dolia
- Lu Bu + Dolia
- Yao + Dolia

### Proteção de hipercarregadores

- Ser do Fluxo (Tanque) + Garo
- Lapulapu + Garo
- Dun + Garo
- Xiang Yu + Garo
- Zhuangzi + Hou Yi
- Zhuangzi + Garo

## Devara

Devara é tratada como herói novo da S15. Seu perfil estratégico considera:

- energia gerada durante o combate;
- escudos;
- mobilidade;
- controle e knockback;
- janela de imunidade a controle;
- força em trocas prolongadas.

Matchups e sinergias específicos recebem confiança `EXPLORATORIA` ou `MEDIA` até existirem mais dados públicos e partidas observadas.

## Níveis de confiança

- `ALTA`: interação mecânica clara ou amplamente consistente.
- `MEDIA`: relação estratégica forte, mas dependente de execução ou contexto.
- `EXPLORATORIA`: herói recente, flex pick incomum ou matchup com poucos dados.

## Endpoints

### Listar todos os tops

```http
GET /api/tops
```

### Consultar um top

```http
GET /api/tops/Florentino
```

### Encontrar counters

```http
GET /api/tops/counters?inimigo=Florentino&limite=10
```

### Analisar um X1 específico

```http
GET /api/tops/confronto?candidato=Donghuang&inimigo=Florentino
```

### Consultar combos de um top

```http
GET /api/tops/Li%20Xin/combos?limite=10
```

### Encontrar tops para um aliado

```http
GET /api/tops/combos?aliado=Da%20Qiao&limite=10
```

### Analisar um combo

```http
GET /api/tops/combo?top=Sun%20Ce&aliado=Gao%20Jianli
```

### Recomendar contra a composição inimiga

```http
GET /api/tops/contra-composicao?tipos=FRONT_TO_BACK,ESCUDOS_E_CURA&limite=10
```

### Recomendar para fortalecer a composição aliada

```http
GET /api/tops/fortalecer-composicao?tipos=ENGAGE_AGRUPADO,FRONT_TO_BACK&limite=10
```

## Integração com o motor

`ConhecimentoClashService` disponibiliza:

- `pontuarConfronto(Heroi candidato, Heroi adversario)`
- `pontuarSinergia(Heroi candidato, List<Heroi> aliados)`
- `pontuarRespostaAosInimigos(Heroi candidato, List<Heroi> inimigos)`
- `pontuarEncaixeAliado(Heroi candidato, List<Heroi> aliados)`

O encaixe aliado também observa necessidades estruturais:

- ausência de linha de frente;
- falta de engage;
- falta de dano sustentado;
- falta de pressão lateral.
