# Base de conhecimento — Jungle — S15

## Objetivo

A base de Jungle responde cinco perguntas diferentes:

1. Qual jungler encaixa melhor na composição aliada?
2. Qual jungler quebra o plano da composição inimiga?
3. Qual jungler possui vantagem direta contra o adversário?
4. Qual escolha controla melhor invasão, ganks e objetivos?
5. Quais aliados transformam o kit do jungler em um combo completo?

Tier, confronto, sinergia e composição permanecem separados. Um jungler de tier inferior pode ser a melhor escolha por possuir counter mecânico, vantagem de invasão ou encaixe estrutural superior.

## Versão

- Versão interna: `S15-HOK-PLUS-2.0`
- Data de referência: `2026-07-02`
- Região: Honor of Kings Global
- Fonte-base de tier: `HOKSTATS_TIER_LIST`
- Dados não oficiais

## Normalização dos nomes enviados

- `Aguu` foi normalizado para `Agu`.
- `Charlottr` foi normalizado para `Charlotte`.
- `Yuan Ge` foi cadastrado como alias de `Yango`, nome usado no servidor Global.
- `Xuance` foi incluído para completar os 43 junglers da referência S15.

## Junglers cadastrados

Ata, Butterfly, Fatih, Agu, Arke, Arthur, Athena, Augran, Chano,
Charlotte, Chicha, Cirrus, Dharma, Dian Wei, Fang, Feyd, Gao Changgong,
Han Xin, Jing, Kaizer, Kongming, Lam, Li Bai, Liu Bei, Luna, Mayene,
Menki, Mi Yue, Musashi, Nakoruru, Pei, Sima Yi, Sun Ce, Ukyo Tachibana,
Umbrosa, Wukong, Wuyan, Yang Jian, Yao, Ying, Zilong, Xuance e Yango.

## Tier de referência

| Tier | Heróis |
|---|---|
| S | Augran |
| A | Chicha, Kaizer, Kongming, Wukong |
| B | Arke, Arthur, Chano, Charlotte, Dian Wei, Fang, Gao Changgong, Lam, Li Bai, Mi Yue, Musashi, Sima Yi, Sun Ce, Ying |
| C | Ata, Butterfly, Fatih, Agu, Athena, Cirrus, Dharma, Feyd, Han Xin, Jing, Liu Bei, Luna, Mayene, Menki, Nakoruru, Pei, Ukyo Tachibana, Umbrosa, Wuyan, Xuance, Yang Jian, Yango, Yao, Zilong |

## Arquétipos de Jungle

- `ASSASSINO_EXECUTOR`
- `LUTADOR_DUELISTA`
- `TANQUE_INICIADOR`
- `INVASOR`
- `GANKER`
- `FARMADOR`
- `CONTROLADOR`
- `OBJETIVOS`
- `RESET`
- `GLOBAL`
- `ANTI_TANQUE`
- `MAGICO`
- `ATIRADOR_JUNGLE`
- `ESCALAMENTO`
- `UTILITARIO`

## Indicadores estratégicos

Cada jungler recebe notas de `0 a 10` para:

- limpeza da jungle;
- pressão de gank;
- capacidade de invasão;
- duelo;
- controle de objetivos;
- mobilidade;
- explosão;
- dano sustentado;
- linha de frente;
- segurança;
- dependência de coordenação.

## Tipos de vantagem no confronto

- `DUELO`: vantagem em combate direto.
- `INVASAO`: capacidade de roubar campos ou contestar rotas de jungle.
- `GANK`: maior pressão sobre as lanes.
- `OBJETIVOS`: vantagem em Tyrant, Overlord e estruturas.
- `MECANICA`: uma habilidade responde diretamente ao kit inimigo.
- `LUTA`: vantagem em escaramuças ou team fights.
- `MAPA`: pressão global, split push ou rotação.
- `ESCALAMENTO`: capacidade de atrasar ou superar o pico adversário.

## Exemplos de confrontos

- Ata e Han Xin contra Agu: engage e invasão contra atiradora jungle.
- Musashi contra Fatih, Umbrosa e Mi Yue: anti-cura contra sustain.
- Arthur contra Luna, Jing, Li Bai, Yango e Xuance: silêncio contra sequências móveis.
- Pei e Han Xin contra Chano, Kongming e Wukong: invasão contra escalamento.
- Kaizer e Liu Bei contra assassinos de primeira rotação.
- Sima Yi contra Kongming, Luna, Agu e Chano: silêncio e acesso direto.
- Dian Wei e Augran contra linhas de frente resistentes.
- Wuyan e Dharma contra assassinos dependentes de combos.

## Combos importantes

### Engage e dano em área

- Ata + Marco Polo
- Ata + Yixing
- Dharma + Wang Zhaojun
- Sun Ce + Gao Jianli
- Sun Ce + Marco Polo
- Wuyan + Wang Zhaojun

### Controle e execução

- Butterfly + Liang
- Arke + Daji
- Jing + Liang
- Kongming + Donghuang
- Lam + Liang
- Xuance + Daji
- Yango + Liang

### Amplificação de duelistas

- Augran + Ming
- Athena + Ming
- Kaizer + Ming
- Liu Bei + Ming
- Pei + Ming
- Wukong + Ming

### Proteção de assassinos móveis

- Arke + Yaria
- Jing + Yaria
- Li Bai + Yaria
- Luna + Yaria
- Musashi + Yaria
- Xuance + Yaria

### Macro e invasão

- Han Xin + Da Qiao
- Athena + Da Qiao
- Pei + Da Qiao
- Mi Yue + Da Qiao
- Agu + Da Qiao
- Sima Yi + Guiguzi

### Reset de ultimate

- Augran + Dolia
- Fang + Dolia
- Jing + Dolia
- Kongming + Dolia
- Li Bai + Dolia
- Luna + Dolia
- Sun Ce + Dolia
- Yao + Dolia
- Ying + Dolia

## Níveis de confiança

- `ALTA`: interação mecânica clara ou apoiada por referências consistentes.
- `MEDIA`: relação estratégica forte, mas sem estatística oficial de dupla.
- `EXPLORATORIA`: interação de herói recente ou com poucos dados públicos.

## Endpoints

### Listar todos os junglers

```http
GET /api/junglers
```

### Consultar um jungler

```http
GET /api/junglers/Augran
```

### Encontrar counters para um jungler inimigo

```http
GET /api/junglers/counters?inimigo=Agu&limite=10
```

### Analisar confronto específico

```http
GET /api/junglers/confronto?candidato=Ata&inimigo=Agu
```

### Consultar combos de um jungler

```http
GET /api/junglers/Augran/combos?limite=10
```

### Consultar junglers que combinam com um aliado

```http
GET /api/junglers/combos?aliado=Dolia&limite=10
```

### Analisar combo específico

```http
GET /api/junglers/combo?jungler=Sun%20Ce&aliado=Gao%20Jianli
```

### Recomendar contra uma composição inimiga

```http
GET /api/junglers/contra-composicao?tipos=FRONT_TO_BACK,ESCUDOS_E_CURA&limite=10
```

### Recomendar para fortalecer uma composição aliada

```http
GET /api/junglers/fortalecer-composicao?tipos=FRONT_TO_BACK,LUTAS_LONGAS&limite=10
```

## Integração com o motor

`ConhecimentoJungleService` disponibiliza:

- `pontuarConfronto(Heroi candidato, Heroi adversario)`
- `pontuarSinergia(Heroi candidato, List<Heroi> aliados)`
- `pontuarRespostaAosInimigos(Heroi candidato, List<Heroi> inimigos)`
- `pontuarEncaixeAliado(Heroi candidato, List<Heroi> aliados)`

O encaixe aliado também observa necessidades estruturais:

- ausência de linha de frente;
- falta de controle para ganks;
- falta de dano sustentado;
- necessidade de controle de objetivos.
