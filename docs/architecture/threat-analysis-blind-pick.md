# Análise de ameaças e segurança de blind pick

## Objetivo

Esta camada responde duas perguntas adicionais antes de ordenar os candidatos:

1. Qual parte da composição inimiga realmente deve ser neutralizada?
2. O candidato é apropriado para a posição atual do jogador na ordem do draft?

A implementação evita duas simplificações comuns:

- assumir que o maior causador de dano é sempre o melhor alvo de counter;
- utilizar os mesmos pesos para o primeiro e para o último pick.

## Mapa de ameaças inimigas

Cada herói inimigo recebe cinco pontuações de `0 a 100`:

- potencial de vitória;
- proteção;
- iniciação;
- habilitação;
- vulnerabilidade.

As pontuações utilizam:

- DNA individual;
- curva temporal;
- dependência de recursos;
- condição de vitória principal;
- participação em sinergias de grupo;
- anti-sinergias existentes;
- classe e características do herói.

## Papéis identificados

### Ameaça principal

Herói com maior capacidade de converter ouro, dano, escalamento, objetivos ou pressão lateral em vitória.

### Protetor principal

Herói que mantém a ameaça viva por meio de:

- peel;
- proteção;
- cura;
- escudos;
- desengage;
- frontline.

### Iniciador principal

Herói que cria a janela inicial da luta com:

- engage;
- controle;
- resistência;
- mobilidade;
- entrada em área.

### Habilitador crítico

Herói que conecta ou amplia a condição de vitória, por exemplo:

- reset de ultimate;
- amplificação;
- agrupamento;
- controle em área;
- visão e macro;
- proteção que permite repetir uma rotação.

### Elo fraco

Componente com maior combinação de:

- exposição;
- dependência de recursos;
- baixa autonomia;
- fragilidade;
- participação em anti-sinergias;
- contribuição estrutural inferior.

## Exemplo: Marco Polo + Lian Po + Dolia

O sistema separa os papéis:

```text
Maior ameaça: Marco Polo
Iniciador: Lian Po
Habilitadora: Dolia
```

Marco Polo possui o maior potencial de vencer a partida pelo dano.

Lian Po cria a janela que permite a ultimate de Marco Polo funcionar.

Dolia permite repetir a habilidade decisiva e prolonga o valor do combo.

A lista de alvos não é ordenada apenas pelo dano. O impacto de neutralização considera também a dependência entre os membros.

Nesse cenário, Lian Po pode receber prioridade de resposta superior a Marco Polo, porque impedir sua entrada reduz a janela do carregador e também dificulta o reset de Dolia.

## Alvos prioritários

Cada alvo retorna:

- herói;
- papel;
- prioridade;
- dimensões recomendadas para responder;
- justificativa.

Exemplos de respostas por papel:

### Contra a ameaça principal

- controle;
- dive;
- explosão;
- mobilidade.

### Contra o iniciador

- desengage;
- peel;
- controle;
- mobilidade.

### Contra o habilitador

- controle;
- explosão;
- alcance;
- mobilidade.

### Contra o protetor

- anti-cura;
- dive;
- controle;
- explosão.

### Contra o elo fraco

- explosão;
- dive;
- alcance.

## Influência na recomendação

Cada candidato recebe:

```text
bonusRespostaAmeaca
alvosAmeacaRespondidos
```

O motor compara o DNA do candidato com as respostas necessárias para os alvos prioritários.

Um herói pode ganhar pontos por:

- impedir a iniciação de Lian Po;
- separar Dolia do núcleo do combo;
- acessar Marco Polo;
- explorar o elo fraco;
- remover o protetor antes da luta principal.

O bônus é limitado para não substituir necessidades, economia, curva temporal, matchup ou sinergia.

## Segurança de blind pick

Cada candidato recebe um perfil com:

- segurança de blind pick;
- flexibilidade;
- consistência;
- risco de counters;
- especificidade para o cenário atual.

### Flexibilidade

Considera:

- quantidade de rotas possíveis;
- classe híbrida;
- capacidade de manter a distribuição da equipe oculta.

### Consistência

Considera:

- cobertura das hipóteses de função;
- pior cenário;
- variação entre cenários.

### Risco de counters

Considera:

- consistência;
- resistência;
- mobilidade;
- alcance;
- flexibilidade;
- dificuldade de execução.

### Especificidade

Considera:

- matchup conhecido;
- resposta às características inimigas;
- bônus contra alvos prioritários;
- sinergia de grupo completada.

## Momentos do draft

O contexto é derivado de `minhaOrdem`.

### BLIND_PICK_INICIAL

Ordens 1 e 2.

Prioridades:

- múltiplas rotas;
- alta cobertura de hipóteses;
- pior cenário estável;
- poucos counters graves;
- funcionamento em diferentes composições.

### PICK_INTERMEDIARIO

Ordem 3.

Prioridades:

- equilíbrio entre segurança e encaixe;
- necessidades já reveladas;
- sinergias parciais;
- manutenção de flexibilidade.

### COUNTER_PICK

Ordem 4.

Prioridades:

- matchup favorável;
- resposta à ameaça;
- counter de composição;
- encaixe por rota.

### LAST_PICK

Ordem 5.

Prioridades:

- counter específico;
- resposta exata à condição inimiga;
- neutralização do habilitador ou iniciador;
- fechamento de combo;
- máximo valor contextual.

Nesse momento, um herói com segurança genérica inferior pode superar um blind pick estável quando sua resposta é perfeita para o cenário revelado.

## Componentes da recomendação

O fluxo de próximo pick passa a expor:

```text
ajusteOrdemDraft
segurancaBlindScore
especificidadeDraft
respostaAmeaca
```

A resposta também inclui:

```text
contextoDraft
perfilBlindPick
```

## API de ameaças

```http
GET /api/composicoes/ameacas?inimigos=Marco%20Polo,Lian%20Po,Dolia
```

O diagnóstico completo também inclui:

```text
analiseAmeacasInimigas
```

## Integração

As novas regras influenciam:

- `GET /api/composicoes/diagnostico`
- `GET /api/composicoes/recomendacoes`
- `POST /api/draft/recomendar`
- `POST /api/draft/recomendar-proximo-pick`

A adaptação por ordem é aplicada no motor de próximo pick, porque esse fluxo possui `meuLado` e `minhaOrdem`.

## Validação

A suíte verifica:

- Marco Polo como maior ameaça;
- Lian Po como iniciador;
- Dolia como habilitadora;
- prioridade estrutural de Lian Po sobre o confronto direto com Marco Polo;
- candidatos explicando quais alvos estratégicos respondem;
- blind pick favorecendo flexibilidade e consistência;
- último pick favorecendo counter específico;
- perfil de segurança anexado a cada recomendação;
- contexto de draft anexado à resposta;
- integração com os motores existentes;
- imagem Docker;
- inicialização do contêiner;
- smoke test da API.
