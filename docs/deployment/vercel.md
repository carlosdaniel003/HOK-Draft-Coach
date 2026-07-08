# Deploy na Vercel

O HOK Draft Coach é uma aplicação única: o Spring Boot serve a interface estática e os endpoints `/api`. A publicação usa um contêiner OCI construído a partir de `Dockerfile.vercel`.

## Arquivos envolvidos

- `Dockerfile.vercel`: compila o projeto com Maven e executa o JAR em Java 21.
- `.dockerignore`: reduz o contexto enviado ao build.
- `src/main/resources/application.properties`: usa `PORT` em produção e `8080` localmente.

Para um único contêiner, não é necessário `vercel.json`. A Vercel detecta `Dockerfile.vercel` na raiz e cria automaticamente a rota que encaminha todo o tráfego ao contêiner.

## Publicação pelo painel

1. Entre no painel da Vercel.
2. Selecione **Add New > Project**.
3. Importe o repositório `carlosdaniel003/HOK-Draft-Coach`.
4. Mantenha **Root Directory** como `./`.
5. Selecione **Framework Preset: Other**.
6. Deixe vazios Build Command, Output Directory e Install Command.
7. Inicie o deploy.
8. Mantenha a branch de produção como `main`.

## Corrigir deployment com erro

Confirme em **Settings > Build and Deployment**:

- Root Directory: vazio ou `./`;
- Framework Preset: `Other`;
- Build Command: vazio;
- Output Directory: vazio;
- Install Command: vazio.

Depois abra **Deployments**, selecione o deployment mais recente da `main` e use **Redeploy** sem reutilizar o Build Cache.

Para erros de invocação, abra a URL do deployment com `/_logs` ou consulte os Runtime Logs e expanda a requisição com status 500.

## Publicação por linha de comando

Na raiz local do projeto:

```powershell
git switch main
git pull origin main
npm install --global vercel
vercel link
vercel --prod --force
```

## Validação após o deploy

Verifique:

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

O Spring Boot lê:

```properties
server.port=${PORT:8080}
```

Assim:

- Vercel/contêiner: `PORT=80`.
- `mvn spring-boot:run`: porta 8080.

## Persistência

O catálogo atual está em memória e é recriado a cada inicialização. Quando PostgreSQL for adicionado, a conexão deverá ser configurada por variáveis de ambiente da Vercel.
