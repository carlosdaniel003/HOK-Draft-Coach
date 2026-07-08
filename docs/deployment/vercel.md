# Deploy na Vercel

O HOK Draft Coach é uma aplicação única: o Spring Boot serve a interface estática e os endpoints `/api`. A publicação usa um contêiner OCI construído a partir de `Dockerfile.vercel`.

## Arquivos envolvidos

- `Dockerfile.vercel`: compila o projeto com Maven e executa o JAR em Java 21.
- `.dockerignore`: reduz o contexto enviado ao build.
- `src/main/resources/application.properties`: usa `PORT` em produção e `8080` localmente.

## Publicação pelo painel

1. Entre no painel da Vercel.
2. Selecione **Add New > Project**.
3. Importe o repositório `carlosdaniel003/HOK-Draft-Coach`.
4. Mantenha **Root Directory** como `./`.
5. Não configure Build Command, Output Directory ou Install Command.
6. Inicie o primeiro deploy.
7. No projeto criado, abra **Settings > Environments**.
8. Abra o ambiente **Production** e acesse **Branch Tracking**.
9. Defina a branch de produção como `feat/backend-draft-engine`.
10. Salve e crie um novo deployment dessa branch em **Deployments > Create Deployment**.

A Vercel detecta `Dockerfile.vercel` na raiz, constrói a imagem e encaminha todo o tráfego para o Spring Boot.

## Publicação por linha de comando

Na raiz local do projeto e com a branch correta selecionada:

```powershell
git switch feat/backend-draft-engine
git pull origin feat/backend-draft-engine
npm install --global vercel
vercel --prod
```

O build da imagem ocorre na infraestrutura da Vercel; Docker local não é necessário para `vercel --prod`.

## Validação após o deploy

Verifique estas URLs no domínio gerado:

```text
/
/api/status
/api/herois?rota=FARM_LANE
```

Resultados esperados:

- `/`: interface do HOK Draft Coach.
- `/api/status`: resposta de status da API.
- `/api/herois?rota=FARM_LANE`: catálogo da Farm Lane em JSON.

## Funcionamento da porta

O contêiner define `PORT=80`. O Spring Boot lê:

```properties
server.port=${PORT:8080}
```

Assim:

- Vercel/contêiner: porta 80.
- `mvn spring-boot:run`: porta 8080.

## Persistência

O catálogo atual está em memória e é recriado a cada inicialização. Isso funciona corretamente para os dados estáticos atuais.

Quando PostgreSQL for adicionado, a conexão deverá ser configurada por variáveis de ambiente da Vercel, e nenhum dado persistente deverá ser gravado no sistema de arquivos do contêiner.

## Observações

- Cada push na branch de produção gera um novo deployment de produção.
- Outras branches recebem deployments de Preview.
- O contêiner pode reduzir a zero quando estiver sem tráfego; a primeira requisição posterior pode ter latência de inicialização.
- Logs da aplicação ficam disponíveis na seção de observabilidade e logs do deployment.
