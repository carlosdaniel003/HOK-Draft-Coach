# Deploy na Vercel

O HOK Draft Coach é uma aplicação única: o Spring Boot serve a interface estática e os endpoints `/api`. A publicação usa um contêiner OCI construído a partir de `Dockerfile.vercel`.

## Arquivos envolvidos

- `Dockerfile.vercel`: compila o projeto com Maven e executa o JAR em Java 21.
- `vercel.json`: registra explicitamente o serviço `app` e encaminha todas as URLs ao contêiner.
- `.dockerignore`: reduz o contexto enviado ao build.
- `src/main/resources/application.properties`: usa `PORT` em produção e `8080` localmente.

## Publicação pelo painel

1. Entre no painel da Vercel.
2. Selecione **Add New > Project**.
3. Importe o repositório `carlosdaniel003/HOK-Draft-Coach`.
4. Mantenha **Root Directory** como `./`.
5. Selecione **Framework Preset: Other**.
6. Deixe vazios Build Command, Output Directory e Install Command.
7. Inicie o deploy.
8. Mantenha a branch de produção como `main`.

A Vercel usa `vercel.json` para criar o serviço a partir de `Dockerfile.vercel` e encaminha todo o tráfego ao Spring Boot.

## Corrigir deployment com 404 da própria Vercel

Se aparecer:

```text
404: NOT_FOUND
Code: NOT_FOUND
```

confirme em **Settings > Build and Deployment**:

- Root Directory: vazio ou `./`;
- Framework Preset: `Other`;
- Build Command: vazio;
- Output Directory: vazio;
- Install Command: vazio.

Depois abra **Deployments**, selecione o deployment mais recente da `main` e use **Redeploy** sem reutilizar o Build Cache.

## Publicação por linha de comando

Na raiz local do projeto:

```powershell
git switch main
git pull origin main
npm install --global vercel
vercel link
vercel --prod --force
```

O parâmetro `--force` cria um build novo sem reutilizar o deployment anterior.

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

- Cada push na branch `main` gera um novo deployment de produção.
- Outras branches recebem deployments de Preview.
- O contêiner pode reduzir a zero quando estiver sem tráfego; a primeira requisição posterior pode ter latência de inicialização.
- Logs da aplicação ficam disponíveis na seção de observabilidade e logs do deployment.
