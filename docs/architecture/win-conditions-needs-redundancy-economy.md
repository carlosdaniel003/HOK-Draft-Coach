# Condições de vitória, necessidades, redundância e economia

## Objetivo

Esta camada amplia o DNA estratégico para responder quatro perguntas antes de recomendar um herói:

1. Como cada equipe pretende vencer?
2. O que ainda está faltando na composição aliada?
3. Quais capacidades já estão redundantes e sem complemento?
4. A economia da equipe suporta mais um herói dependente de ouro?

A análise é executada pelo `AnaliseEstrategicaComposicaoService`, que estende o motor de DNA anterior e é o bean principal utilizado pelos endpoints de composição e pelos orquestradores de draft.

## Condições de vitória

Uma composição pode possuir até três condições identificadas, ordenadas por força. A primeira é marcada como `principal`.

Tipos implementados:

- `PROTEGER_HIPERCARREGADOR`
- `SPLIT_PUSH`
- `CERCO_DE_TORRES`
- `LUTAS_LONGAS`
- `PICKOFF`
- `DIVE_NA_RETAGUARDA`
- `WOMBO_COMBO`
- `CONTROLE_DE_OBJETIVOS`
- `ESCALAMENTO_TARDIO`

Cada condição retorna:

- força de `0 a 100`;
- título e descrição;
- executores principais;
- dependências para o plano funcionar;
- vulnerabilidades;
- respostas que a equipe adversária pode procurar.

### Exemplo: Hou Yi + Ming + frontline

Para uma composição com:

- Hou Yi;
- Ming;
- Lian Po ou outra linha de frente resistente;

O motor identifica:

```text
Condição principal: PROTEGER_HIPERCARREGADOR
Plano: manter Hou Yi atacando livremente enquanto Ming amplifica e a frontline sustenta uma luta front-to-back.
```

Respostas sugeridas para a equipe adversária:

1. acessar e eliminar Hou Yi;
2. separar Ming de Hou Yi;
3. quebrar a formação da linha de frente.

Cada resposta inclui as dimensões necessárias. Por exemplo, acessar Hou Yi procura `DIVE`, `EXPLOSAO` e `MOBILIDADE`.

## Necessidades de composição

As necessidades são diferentes dos diagnósticos narrativos. Elas funcionam como requisitos objetivos para a próxima escolha.

Cada necessidade retorna:

- código;
- dimensão estratégica;
- urgência;
- valor atual;
- alvo mínimo;
- tamanho do déficit;
- capacidades desejadas no próximo herói.

Dimensões estruturais verificadas imediatamente:

- engage;
- controle;
- linha de frente;
- DPS;
- peel;
- wave clear;
- objetivos;
- sustain.

As prioridades contextuais já geradas pelo confronto contra a equipe inimiga são incorporadas e podem substituir a urgência-base.

### Necessidade econômica

Quando existem três ou mais carregadores dependentes ou o conflito econômico é alto, o sistema cria:

```text
BAIXA_DEPENDENCIA_RECURSOS
```

A próxima escolha passa a procurar:

- utilidade com poucos itens;
- controle pelo kit;
- frontline;
- proteção;
- capacidade de ceder farm.

## Penalidades de redundância

O sistema não penaliza simplesmente possuir uma dimensão forte. A penalidade surge quando há excesso em uma função e ausência do complemento necessário.

Regras implementadas:

### Iniciação redundante sem follow-up

```text
Três ou mais iniciadores + DPS e explosão insuficientes
```

Exemplo:

```text
A equipe consegue começar várias lutas, mas ninguém converte o controle em eliminações.
```

### Dois suportes de sustain sem DPS

```text
Dois ou mais suportes de cura/proteção + DPS insuficiente
```

A luta pode ser prolongada, mas não existe carregador para aproveitar o tempo adicional.

### Frontline redundante sem dano

Três ou mais frontliners resistentes perdem valor quando não existe núcleo ofensivo para usar o espaço criado.

### Poke redundante sem espaço

Três ou mais fontes de poke são penalizadas quando faltam frontline e engage para impedir flancos.

### Conflito de recursos

Três ou mais carregadores com alta dependência de ouro geram penalidade econômica.

### Dano totalmente monotipo

Uma equipe com três ou mais heróis e 100% de dano físico ou mágico recebe penalidade adicional.

## Dependência de recursos

Cada herói recebe:

- `dependenciaRecursos` de `0 a 100`;
- nível `BAIXA`, `MEDIA`, `ALTA` ou `CRITICA`;
- `utilidadeSemOuro`;
- indicador `consegueCederRecursos`;
- explicações do cálculo.

### Fatores que aumentam dependência

- DPS elevado;
- explosão elevada;
- classe atirador ou assassino;
- jungler carregador;
- tags de fim de jogo, escalamento e hipercarregador;
- duelistas e split pushers dependentes de itens.

### Fatores que reduzem dependência

- classe suporte ou tanque;
- controle;
- engage;
- proteção;
- cura e escudos;
- visão e macro;
- utilidade que funciona independentemente dos itens.

## Economia da composição

A equipe recebe:

- carga econômica;
- conflito de recursos;
- quantidade de carregadores dependentes;
- indicação de viabilidade;
- perfis individuais ordenados por dependência.

A carga econômica considera principalmente os três heróis mais dependentes. O conflito aumenta quando:

- três ou mais heróis exigem prioridade de farm;
- os três maiores valores possuem média muito alta;
- nenhum herói consegue ceder recursos.

## Impacto na recomendação

Para cada candidato, o motor compara a equipe antes e depois da escolha.

A recomendação recebe novos campos:

- `dependenciaRecursos`;
- `ajusteEconomico`;
- `penalidadeRedundancia`;
- `alertas`.

A pontuação aumenta quando o herói:

- funciona com pouco ouro em uma equipe com vários carregadores;
- fortalece a condição de vitória principal;
- oferece uma resposta à condição de vitória inimiga.

A pontuação diminui quando o herói:

- cria um novo conflito econômico;
- adiciona uma redundância que não existia;
- disputa ouro com três ou mais carregadores já escolhidos.

## Integração

Como `AnaliseEstrategicaComposicaoService` é marcado como `@Primary`, os consumidores existentes de `DnaComposicaoService` utilizam automaticamente a análise avançada:

- `GET /api/composicoes/diagnostico`
- `GET /api/composicoes/recomendacoes`
- `POST /api/draft/recomendar`
- `POST /api/draft/recomendar-proximo-pick`

As APIs anteriores continuam compatíveis porque os DTOs mantêm construtores auxiliares com a assinatura antiga.

## Testes

A suíte valida:

- Hou Yi + Ming + frontline como condição de hipercarregador;
- as três respostas estratégicas contra esse plano;
- necessidades de engage e frontline;
- três iniciadores sem follow-up;
- dois suportes de cura sem DPS;
- três carregadores com conflito econômico;
- criação da necessidade de baixa dependência de recursos;
- preferência econômica por um top capaz de ceder farm;
- compilação, imagem Docker, inicialização do contêiner e smoke test da API.
