# Curva de poder, sinergias de grupo e anti-sinergias

## Objetivo

Esta camada amplia o motor estratégico com três perguntas executadas antes da recomendação:

1. Em qual fase cada herói e composição são mais fortes?
2. Quais combinações de três ou mais heróis estão ativas ou quase completas?
3. Quais escolhas individualmente fortes funcionam mal em conjunto?

O serviço `AnaliseTemporalSinergiaService` combina essas respostas com o DNA, condições de vitória, necessidades, redundância e economia já existentes.

## Curva de poder individual

Todo herói recebe um perfil com valores de `0 a 100` para:

- `earlyGame`
- `midGame`
- `lateGame`

O perfil também retorna:

- fase de pico;
- motivos do formato da curva.

A curva utiliza:

- controle;
- resistência;
- mobilidade;
- alcance;
- dano sustentado;
- dano explosivo;
- classe;
- rota;
- tags como início de jogo, pressão, rotação, meio de jogo, fim de jogo, escalamento e hipercarregador.

### Exemplos de interpretação

```text
Garo
Early: baixo
Mid: adequado
Late: excepcional
Pico: LATE_GAME
```

```text
Fang
Early: excepcional
Mid: forte
Late: adequado
Pico: EARLY_GAME
```

## Curva de poder da composição

Os perfis individuais são agregados em:

- força média no early game;
- força média no mid game;
- força média no late game;
- fase de pico;
- plano temporal;
- alertas;
- lista dos perfis individuais.

Diagnósticos implementados:

### Todos fracos no início

```text
TODOS_FRACOS_NO_INICIO
```

É ativado quando a média inicial é baixa e nenhum aliado possui força suficiente para estabilizar a fase.

### Precisa terminar cedo

```text
PRECISA_TERMINAR_CEDO
```

Exige duas condições simultâneas:

- vantagem real no early game;
- desvantagem clara no late game.

O sistema evita recomendar aceleração quando a equipe escala menos, mas também não possui força inicial para converter.

### Escala menos sem vantagem inicial

```text
ESCALA_MENOS_SEM_VANTAGEM_INICIAL
```

Nesse caso, a próxima escolha deve criar pressão imediata ou reduzir o déficit de late game.

### Outros diagnósticos

- inimigo mais forte no early;
- vantagem de escalamento;
- janela forte no meio da partida;
- curva equilibrada.

## Influência temporal na recomendação

Cada candidato recebe o campo:

```text
ajusteTemporal
```

O valor aumenta quando o herói:

- estabiliza uma equipe inteira fraca no início;
- reforça a janela de uma composição que precisa terminar cedo;
- reduz uma diferença crítica de late game;
- cobre a fase mais fraca da equipe.

O valor diminui quando o candidato reforça uma fase que não corresponde ao plano temporal necessário.

## Sinergias de grupo

As sinergias de grupo exigem de três a cinco membros.

Uma regra pode combinar:

- heróis obrigatórios;
- classes;
- tags;
- dimensões mínimas do DNA;
- quantidade necessária de cada capacidade.

O sistema retorna combinações:

- ativas;
- quase completas, quando falta apenas um componente.

Cada sinergia informa:

- código;
- tipo;
- nota;
- membros encontrados;
- componente ausente;
- descrição;
- sequência de execução;
- benefícios.

## Tipos de sinergia cadastrados

- `CONTROLE_EM_CADEIA`
- `COMBO_DE_ULTIMATES`
- `SEQUENCIA_DE_ENGAGE`
- `PROTECAO_EM_CAMADAS`
- `RESET_DE_HABILIDADES`
- `AGRUPAMENTO_SEGUIDO_DE_DANO_EM_AREA`
- `WOMBO_COMBO`

## Lian Po + Marco Polo + Dolia

Regra exata:

```text
LIAN_PO_MARCO_POLO_DOLIA
```

Sequência:

1. Lian Po inicia e mantém os inimigos ocupados;
2. Marco Polo usa a ultimate sobre o controle;
3. Dolia reinicia a ultimate de maior impacto;
4. a equipe usa uma segunda janela de dano ou engage.

A combinação recebe nota máxima e gera bônus relevante quando Dolia completa a dupla Lian Po + Marco Polo.

## Guiguzi + mago de área + ADC de área

Regra por capacidade:

```text
GUIGUZI_MAGO_AREA_ADC_AREA
```

Ela não exige nomes fixos para o mago e o ADC.

Requisitos:

- Guiguzi;
- um mago com dano, controle ou zona em área;
- um atirador com dano ou ultimate relevante em área.

Isso permite detectar diferentes versões do mesmo plano estratégico.

## Outras regras de grupo

- dois controladores + finalizador de área;
- dois iniciadores + follow-up;
- hipercarregador + frontline + protetor;
- Dolia + engage + ultimate em área;
- agrupador + duas fontes diferentes de dano em área.

## Impacto da sinergia na recomendação

Cada candidato recebe:

```text
bonusSinergiaGrupo
```

A pontuação aumenta quando a escolha:

- ativa uma combinação que ainda não existia;
- completa uma regra exata;
- cria uma sequência de três ou mais habilidades;
- transforma uma combinação parcial em plano executável.

O bônus é limitado para não substituir matchup, necessidades, economia ou DNA.

## Anti-sinergias

Anti-sinergias não são proibições absolutas. Elas representam risco de execução e recebem penalidade moderada com orientação de mitigação.

Tipos implementados:

- `RITMOS_INCOMPATIVEIS`
- `DESLOCAMENTO_QUE_QUEBRA_COMBO`
- `MOBILIDADE_INCOMPATIVEL`
- `ALCANCE_INCOMPATIVEL`
- `FORMACAO_INCOMPATIVEL`

## Regras explícitas iniciais

- Xiang Yu + Marco Polo;
- Xiang Yu + Wang Zhaojun;
- Guan Yu + Wang Zhaojun;
- Cai Yan + Arli;
- Cai Yan + Marco Polo.

### Exemplo de deslocamento

Xiang Yu pode retirar inimigos da área da ultimate de Marco Polo ou Wang Zhaojun quando a ordem das habilidades não é coordenada.

Mitigação:

- empurrar em direção à zona;
- guardar o deslocamento para impedir fuga;
- usar o deslocamento apenas após a ultimate terminar.

### Exemplo de mobilidade

Arli pode sair do alcance confortável de Cai Yan durante reposicionamentos longos.

Mitigação:

- trocas mais curtas;
- retorno à zona de cura;
- guardar proteção para a saída.

## Regras estruturais de anti-sinergia

O motor também detecta padrões sem depender de pares cadastrados:

### ADC móvel + suporte estático

Um ADC com mobilidade muito alta recebe alerta quando o suporte possui baixa mobilidade e nenhuma ferramenta de acompanhamento.

### Poke + all-in sem conexão

Uma composição com múltiplas fontes de poke e múltiplos mergulhadores recebe penalidade quando não possui sustain ou proteção que conecte os dois ritmos.

### Deslocamento + ultimate de zona

Empurradores são comparados com heróis que precisam manter inimigos dentro de uma área de dano ou controle.

## Impacto da anti-sinergia na recomendação

Cada candidato recebe:

```text
penalidadeAntiSinergia
```

A penalidade considera apenas incompatibilidades novas criadas pela escolha. Anti-sinergias que já existiam antes do candidato não são cobradas novamente.

A resposta inclui explicação e mitigação em `alertas`.

## Novos campos do diagnóstico

```text
curvaPoderNossaComposicao
curvaPoderComposicaoInimiga
diagnosticosTemporais
sinergiasGrupoNossaComposicao
sinergiasGrupoComposicaoInimiga
antiSinergiasNossaComposicao
antiSinergiasComposicaoInimiga
```

## Novos campos por candidato

```text
ajusteTemporal
bonusSinergiaGrupo
penalidadeAntiSinergia
```

## API

### Perfil temporal individual

```http
GET /api/composicoes/perfil-temporal?heroi=Garo
```

### Curva de uma composição

```http
GET /api/composicoes/curva-poder?herois=Garo,Hou%20Yi,Cai%20Yan
```

### Diagnóstico completo

```http
GET /api/composicoes/diagnostico?aliados=Fang,Liu%20Shan,Guiguzi&inimigos=Garo,Hou%20Yi,Cai%20Yan
```

### Recomendação com as novas camadas

```http
GET /api/composicoes/recomendacoes?aliados=Lian%20Po,Marco%20Polo&rota=ROAMING&limite=10
```

Nesse último exemplo, Dolia recebe bônus por completar a sinergia de três heróis.

## Integração com o draft

As novas camadas também influenciam:

- `POST /api/draft/recomendar`
- `POST /api/draft/recomendar-proximo-pick`

Os componentes detalhados da nota incluem:

```text
dnaComposicao
curvaTemporal
sinergiaGrupo
antiSinergia
```

## Validação

A suíte cobre:

- perfil temporal válido para todos os heróis;
- composição inteira fraca no início;
- composição que precisa terminar cedo;
- Lian Po + Marco Polo + Dolia;
- Guiguzi + mago de área + ADC de área;
- bônus de Dolia ao completar o trio;
- suporte que não acompanha ADC móvel;
- penalidade de Cai Yan com Arli;
- ausência da mesma penalidade para Yaria;
- deslocamento que pode quebrar ultimate aliada;
- integração nos dois motores principais;
- construção da imagem Docker;
- inicialização do contêiner;
- smoke test da API.
