# Projeção de picks e opções estratégicas

## Objetivo

O motor deixa de avaliar somente o estado atual do draft e passa a simular a pergunta:

```text
Se escolhermos este herói, quais respostas inimigas são mais plausíveis e qual será nosso pior cenário?
```

A projeção é heurística e explicável. Ela não representa frequência estatística real de partidas.

## Projeção de respostas inimigas

Para cada candidato aliado, o sistema:

1. adiciona temporariamente o herói à composição;
2. identifica as rotas ainda abertas nas hipóteses inimigas;
3. procura candidatos inimigos disponíveis para essas rotas;
4. calcula o encaixe estratégico da resposta;
5. mede a capacidade da resposta de neutralizar nossas ameaças;
6. ordena até três respostas prováveis;
7. calcula o pior cenário e a robustez do nosso pick.

Cada resposta projetada informa:

- herói;
- rota;
- probabilidade heurística;
- impacto contra nossa composição;
- pontuação da resposta inimiga;
- motivos da projeção.

## Robustez

A robustez combina:

- pior cenário após a resposta inimiga;
- segurança de blind pick;
- cobertura das hipóteses de função.

Cada candidato recebe:

```text
ajusteProjecao
robustezProjetada
piorCenarioProjetado
```

Escolhas que permanecem funcionais contra as melhores respostas ganham pontos. Escolhas que ficam muito frágeis no pior caso perdem pontos.

## Explicação narrativa

A recomendação agora é explicada em cinco blocos:

### Leitura inimiga

Exemplos:

```text
A equipe inimiga possui dive, controle em área e alto dano explosivo.
```

```text
Marco Polo é a maior ameaça, Lian Po cria a janela de luta e Dolia amplifica a execução.
```

### Leitura aliada

Exemplos:

```text
Sua equipe possui dano suficiente, mas não tem proteção consistente para o ADC.
```

```text
A equipe já possui poke e wave clear, mas ainda precisa de engage.
```

### Por que a escolha funciona

Inclui:

- necessidades corrigidas;
- fraquezas inimigas exploradas;
- alvos estratégicos neutralizados;
- sinergias de grupo completadas;
- ajuste temporal;
- segurança e especificidade;
- robustez projetada.

### Riscos

Inclui:

- anti-sinergias;
- risco de counter;
- pior resposta inimiga projetada;
- pior cenário abaixo do nível aceitável.

### Plano de jogo

Transforma a recomendação em instrução estratégica de execução.

## Três opções finais

A resposta passa a oferecer três categorias:

### Melhor escolha geral

Maior equilíbrio entre:

- pontuação total;
- robustez;
- pior cenário projetado;
- encaixe estratégico.

### Escolha mais segura

Prioriza:

- segurança de blind pick;
- robustez após counters;
- cobertura de hipóteses;
- pior cenário estável;
- flexibilidade.

### Escolha de maior impacto

Prioriza:

- especificidade para o draft;
- resposta às ameaças inimigas;
- counter direto ou estrutural;
- fechamento de combo;
- potencial de desmontar a condição de vitória adversária.

A escolha de maior impacto pode ser mais arriscada e exigir execução superior.

## Campos adicionados

A resposta de próximo pick inclui:

```text
opcoesEstrategicas
```

Cada opção contém:

```text
tipo
titulo
pontuacaoCategoria
escolha
projecao
explicacao
```

Tipos:

```text
MELHOR_GERAL
MAIS_SEGURA
MAIOR_IMPACTO
```

## Compatibilidade

Os campos anteriores continuam disponíveis:

- `recomendacaoPrincipal` corresponde à melhor escolha geral;
- `alternativas` contém as demais opções estratégicas quando distintas.

## Validação

A suíte verifica:

- criação das três categorias;
- explicação narrativa não vazia;
- projeção associada a cada opção;
- ajuste de projeção no candidato;
- compatibilidade com diagnóstico e ordem do draft;
- suíte Maven;
- build da imagem Docker;
- inicialização do contêiner;
- smoke test da API.
