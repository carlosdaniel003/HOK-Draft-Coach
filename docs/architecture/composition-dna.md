# DNA estratégico de composição

## Objetivo

O DNA de composição é a camada de diagnóstico executada antes da recomendação de heróis.

A ordem do motor passa a ser:

1. resolver os heróis já escolhidos;
2. gerar o DNA individual de cada herói;
3. agregar o vetor da composição aliada;
4. agregar o vetor da composição inimiga;
5. detectar déficits, excessos e ameaças;
6. criar prioridades de draft;
7. somente então pontuar os candidatos da rota solicitada.

A pergunta principal deixa de ser apenas "qual herói é forte?" e passa a ser:

> Qual escolha corrige o maior problema da nossa composição e responde ao plano inimigo?

## Dimensões universais

Cada herói e composição recebe valores de `0 a 100` para:

- `ENGAGE`
- `DESENGAGE`
- `PEEL`
- `POKE`
- `EXPLOSAO`
- `DPS`
- `LINHA_DE_FRENTE`
- `SUSTAIN`
- `WAVE_CLEAR`
- `OBJETIVOS`
- `CONTROLE`
- `MOBILIDADE`
- `ALCANCE`
- `ANTI_TANQUE`
- `ANTI_CURA`
- `PRESSAO_LATERAL`
- `DIVE`
- `PROTECAO`
- `ESCALAMENTO`

## Origem dos valores

O DNA individual combina duas fontes:

### Atributos universais

- controle;
- resistência;
- mobilidade;
- alcance;
- dano sustentado;
- dano explosivo.

### Capacidades registradas nas tags

As tags acrescentam capacidades que os seis atributos não representam diretamente, como:

- anti-cura;
- dano verdadeiro;
- dano percentual;
- split push;
- purificação;
- ressurreição;
- engage global;
- proteção;
- escudos;
- cura;
- execução.

O serviço normaliza acentos, espaços e variações de escrita durante a leitura das tags.

## Agregação da composição

A composição não utiliza média simples.

Para cada dimensão, as contribuições são ordenadas da maior para a menor e recebem pesos de redundância:

| Contribuidor | Peso |
|---|---:|
| melhor herói | 60% |
| segundo | 23% |
| terceiro | 10% |
| quarto | 5% |
| quinto | 2% |

Durante um draft incompleto, os pesos presentes são normalizados. Portanto, o sistema consegue diagnosticar composições com uma, duas, três, quatro ou cinco escolhas.

Esse método diferencia:

- possuir uma ferramenta;
- possuir uma ferramenta confiável;
- possuir redundância estratégica.

## Níveis

| Faixa | Nível |
|---:|---|
| 0–29 | `CRITICO` |
| 30–49 | `BAIXO` |
| 50–69 | `ADEQUADO` |
| 70–84 | `FORTE` |
| 85–100 | `EXCEPCIONAL` |

## Diagnósticos iniciais

A primeira versão detecta:

- dano sem iniciação;
- iniciação sem follow-up de dano;
- ausência de linha de frente;
- múltiplos carregadores frágeis sem peel;
- wave clear insuficiente;
- controle de objetivos insuficiente;
- distribuição de dano excessivamente física ou mágica;
- falta de DPS e anti-tanque contra três tanques ou frontline pesada;
- vulnerabilidade a dive;
- falta de resposta a poke;
- sustain inimigo sem anti-cura;
- falta de resposta a split push.

## Exemplo: dano sem engage

Composição aliada:

- Garo;
- Hou Yi.

O sistema pode gerar:

```text
DPS: forte
Escalamento: forte
Alcance: forte
Engage: crítico
Linha de frente: crítica
Peel: baixo
```

Prioridade principal:

```text
Adicionar iniciação confiável antes de reforçar mais dano.
```

## Exemplo: três tanques inimigos

Composição inimiga:

- Ata;
- Bai Qi;
- Lian Po.

Quando a equipe aliada possui baixo DPS ou anti-tanque, o diagnóstico é:

```text
SEM_RESPOSTA_A_TANQUES
```

Prioridades:

1. `ANTI_TANQUE`;
2. `DPS`.

O motor favorece heróis com dano verdadeiro, dano percentual, execução e dano sustentado.

## Distribuição de dano

O DNA também retorna a proporção de heróis com dano:

- físico;
- mágico;
- misto.

Quando 80% ou mais da composição está concentrada em dano físico ou mágico, o sistema alerta que o inimigo pode responder com uma única linha de resistência.

## Recomendações baseadas no DNA

Para cada candidato, o sistema:

1. adiciona temporariamente o herói à composição;
2. recalcula todo o vetor;
3. mede quanto cada prioridade melhora;
4. verifica se o herói explora o DNA inimigo;
5. produz uma pontuação e explicações.

Exemplos de relações ofensivas:

- frontline inimiga → `ANTI_TANQUE`;
- sustain inimigo → `ANTI_CURA`;
- poke inimigo → `ENGAGE`;
- dive inimigo → `PEEL`.

## API

### Gerar somente o DNA de uma composição

```http
GET /api/composicoes/dna?herois=Marco%20Polo,Angela,Lian%20Po
```

### Diagnosticar os dois drafts

```http
GET /api/composicoes/diagnostico?aliados=Garo,Hou%20Yi&inimigos=Ata,Bai%20Qi,Lian%20Po
```

O parâmetro `inimigos` é opcional. Sem ele, o sistema analisa apenas os déficits próprios.

### Recomendar uma escolha após o diagnóstico

```http
GET /api/composicoes/recomendacoes?aliados=Garo,Hou%20Yi&inimigos=Ata,Bai%20Qi&rota=ROAMING&limite=10
```

A resposta informa:

- herói;
- rota;
- pontuação;
- dimensões corrigidas;
- dimensões inimigas exploradas;
- motivos da recomendação.

## Limites atuais

- O DNA depende da qualidade dos atributos e tags dos heróis.
- A taxonomia de tags ainda será centralizada em uma etapa posterior.
- A pontuação de DNA não substitui matchup direto, tier ou sinergia específica.
- A versão final do motor deverá combinar DNA, matchup, sinergia, tier contextual e restrições de rota.
