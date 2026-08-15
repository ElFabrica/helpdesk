# Helpdesk

Central de chamados internos com API REST, autenticacao JWT, triagem automatica
de chamados e dashboard administrativo em tempo real via Server-Sent Events
SSE. O projeto foi desenvolvido para o desafio tecnico da Fadex e inclui backend
Spring Boot, frontend React, PostgreSQL, Docker Compose, Swagger/OpenAPI e
testes automatizados.

## Tecnologias

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC, Spring Security, OAuth2 Resource Server, Bean Validation
- Spring Data JPA
- PostgreSQL 16
- JWT com assinatura HS256
- Swagger/OpenAPI com springdoc
- React 19, TypeScript, Vite, Tailwind CSS
- Docker e Docker Compose
- Maven Wrapper em `api/mvnw`
- PNPM no frontend

## Funcionalidades

- Cadastro de solicitantes com senha criptografada por BCrypt.
- Login com emissao de token JWT.
- Autorizacao por perfil `ADMIN` e `SOLICITANTE`.
- CRUD de chamados com filtros por status, prioridade e categoria.
- Solicitante acessa apenas os proprios chamados.
- ADMIN lista todos os chamados, ajusta categoria/prioridade e pode atribuir
  responsavel.
- Comentarios e historico de alteracoes em ordem cronologica.
- Regras de status, incluindo bloqueio de reabertura de chamado fechado.
- Triagem automatica de categoria e prioridade ao abrir chamado.
- Dashboard com contadores por status/prioridade.
- Eventos SSE para atualizacao automatica do dashboard e alerta de chamado de
  prioridade alta.
- Tratamento global de erros com respostas HTTP adequadas.
- Frontend simples consumindo a API.
- Testes automatizados unitarios e de integracao.

## Requisitos

Para executar com Docker:

- Docker
- Docker Compose

Para executar sem Docker:

- Java 21
- PostgreSQL 16
- Node.js 22
- PNPM

## Variaveis de ambiente

Copie o arquivo de exemplo antes de executar localmente:

```bash
cp .env.example .env
```

Gere um segredo local para assinar os tokens JWT:

```bash
openssl rand -base64 32
```

Atualize `JWT_SECRET` no `.env` com o valor gerado. O segredo precisa ter pelo
menos 32 bytes.

Variaveis principais:

| Variavel | Padrao | Descricao |
| --- | --- | --- |
| `POSTGRES_DB` | `helpdesk` | Nome do banco PostgreSQL |
| `POSTGRES_USER` | `helpdesk` | Usuario do banco |
| `POSTGRES_PASSWORD` | `helpdesk` | Senha do banco |
| `POSTGRES_PORT` | `5432` | Porta local do PostgreSQL |
| `SERVER_PORT` | `8080` | Porta local da API |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/helpdesk` | URL JDBC usada pela API fora do Docker |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Estrategia de criacao/atualizacao do schema |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Origens permitidas para chamadas do frontend |
| `JWT_SECRET` | sem padrao seguro | Segredo usado para assinar JWT |
| `GROQ_CLASSIFIER_ENABLED` | `true` | Habilita tentativa de classificacao pela Groq |
| `GROQ_API_KEY` | vazio | Chave da API da Groq; vazio aciona fallback local |
| `GROQ_BASE_URL` | `https://api.groq.com/openai/v1` | Base URL compativel com OpenAI |
| `GROQ_MODEL` | `openai/gpt-oss-20b` | Modelo usado na classificacao |
| `GROQ_TIMEOUT` | `3s` | Timeout da integracao de IA |

O frontend tambem aceita `VITE_API_BASE_URL`. Em desenvolvimento com Vite, o
padrao recomendado e `/api`, usando proxy para a API.

## Infraestrutura

A infraestrutura local esta definida em `docker-compose.yml` e sobe tres
servicos principais:

| Servico | Container | Porta local | Funcao |
| --- | --- | --- | --- |
| `postgres` | `helpdesk-postgres` | `${POSTGRES_PORT:-5432}` | Banco relacional PostgreSQL 16 |
| `api` | `helpdesk-api` | `${SERVER_PORT:-8080}` | API Spring Boot |
| `web` | `helpdesk-web` | `${WEB_PORT:-5173}` | Frontend React/Vite |

Fluxo de comunicacao:

- O navegador acessa o frontend em `http://localhost:5173`.
- O frontend chama a API usando `VITE_API_BASE_URL=/api`.
- O Vite faz proxy de `/api` para `http://api:8080` dentro da rede do Docker.
- A API conecta no banco usando
  `jdbc:postgresql://postgres:5432/${POSTGRES_DB:-helpdesk}`.
- O servico `api` so inicia depois que o healthcheck do PostgreSQL confirma que
  o banco esta pronto.

Volumes persistentes:

| Volume | Uso |
| --- | --- |
| `postgres-data` | Mantem os dados do PostgreSQL entre reinicializacoes |
| `maven-repository` | Cache local do Maven dentro do container da API |
| `web-node-modules` | Dependencias Node do frontend dentro do container |

Os Dockerfiles atuais usam alvos de desenvolvimento:

- A API usa imagem `eclipse-temurin:21-jdk`, executa `./mvnw spring-boot:run` e
  reinicia quando arquivos em `src` ou `pom.xml` mudam.
- O frontend usa `node:22-alpine`, habilita Corepack e executa
  `pnpm dev --host 0.0.0.0`.

Essa configuracao prioriza reproducibilidade e avaliacao local com hot reload.
Para producao, o caminho natural seria criar builds finais menores, servir o
frontend estatico por Nginx/CDN ou pelo backend, configurar migracoes
versionadas e gerenciar segredos por um provedor de infraestrutura.

## Executar com Docker

Este e o caminho mais simples para subir PostgreSQL, API e frontend:

```bash
cp .env.example .env
docker compose up --build
```

Acesse:

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Para parar:

```bash
docker compose down
```

Para remover tambem o volume do banco:

```bash
docker compose down -v
```

## Executar localmente sem Docker

Suba apenas o banco:

```bash
docker compose up -d postgres
```

Execute a API:

```bash
cd api
./mvnw spring-boot:run
```

Em outro terminal, execute o frontend:

```bash
cd web
corepack enable
pnpm install
pnpm dev
```

Acesse o frontend em `http://localhost:5173`.

## Acesso pela rede local

Com Docker Compose, o frontend escuta em `0.0.0.0`. Descubra o IP da maquina:

```bash
hostname -I
```

Outras maquinas da mesma rede podem acessar:

```text
http://SEU_IP_NA_REDE:5173
```

Se configurar o frontend para chamar a API diretamente por
`http://SEU_IP_NA_REDE:8080`, inclua tambem a origem do frontend em
`APP_CORS_ALLOWED_ORIGINS` e reinicie os containers.

## Usuarios de teste

A API cria os usuarios abaixo ao iniciar, sem duplica-los em reinicializacoes:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| `ADMIN` | `admin@helpdesk.local` | `admin123` |
| `SOLICITANTE` | `user@helpdesk.local` | `user123` |

As senhas sao persistidas com hash BCrypt.

## Exemplos curl

Defina a URL base:

```bash
API_URL=http://localhost:8080/api
```

Cadastrar um novo solicitante:

```bash
curl -i -X POST "$API_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Silva",
    "email": "maria@example.com",
    "password": "123456"
  }'
```

Login como solicitante de teste:

```bash
curl -i -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@helpdesk.local",
    "password": "user123"
  }'
```

Copie o campo `token` da resposta e defina:

```bash
TOKEN=cole_o_token_aqui
```

Criar chamado com triagem automatica:

```bash
curl -i -X POST "$API_URL/tickets" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sistema financeiro fora do ar",
    "description": "Nao consigo acessar o sistema financeiro desde cedo. Parece urgente."
  }'
```

Listar chamados do usuario autenticado:

```bash
curl -i "$API_URL/tickets" \
  -H "Authorization: Bearer $TOKEN"
```

Listar chamados com filtro:

```bash
curl -i "$API_URL/tickets?status=ABERTO&priority=ALTA&category=SOFTWARE" \
  -H "Authorization: Bearer $TOKEN"
```

Detalhar um chamado:

```bash
curl -i "$API_URL/tickets/1" \
  -H "Authorization: Bearer $TOKEN"
```

Adicionar comentario:

```bash
curl -i -X POST "$API_URL/tickets/1/comments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Inclui mais detalhes sobre o erro apresentado."
  }'
```

Listar historico/comentarios:

```bash
curl -i "$API_URL/tickets/1/comments" \
  -H "Authorization: Bearer $TOKEN"
```

Atualizar titulo, descricao ou status:

```bash
curl -i -X PATCH "$API_URL/tickets/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "EM_ANDAMENTO"
  }'
```

Cancelar chamado:

```bash
curl -i -X DELETE "$API_URL/tickets/1" \
  -H "Authorization: Bearer $TOKEN"
```

Login como ADMIN:

```bash
curl -i -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@helpdesk.local",
    "password": "admin123"
  }'
```

Copie o token de ADMIN para `ADMIN_TOKEN`.

Consultar indicadores do dashboard:

```bash
curl -i "$API_URL/dashboard/indicators" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Corrigir classificacao como ADMIN:

```bash
curl -i -X PATCH "$API_URL/tickets/1/classification" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "REDE",
    "priority": "MEDIA"
  }'
```

Atualizar campos administrativos, como responsavel:

```bash
curl -i -X PATCH "$API_URL/tickets/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "responsibleId": 1,
    "priority": "ALTA",
    "category": "SOFTWARE"
  }'
```

Conectar ao stream SSE do dashboard:

```bash
curl -N "$API_URL/dashboard/events" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Enquanto esse comando estiver aberto, crie ou atualize chamados em outro
terminal. Eventos esperados:

- `indicators-updated`
- `ticket-created`
- `ticket-updated`
- `high-priority-alert`

## Autenticacao e autorizacao

As rotas publicas sao:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

As demais rotas exigem JWT no header:

```http
Authorization: Bearer <token>
```

Tokens carregam o papel do usuario. A API converte esse papel em authorities
`ROLE_ADMIN` ou `ROLE_SOLICITANTE`.

Regras principais:

- `ADMIN` lista todos os chamados, acessa dashboard, corrige classificacao e
  atualiza campos administrativos.
- `SOLICITANTE` cria chamados e visualiza/gerencia apenas os proprios chamados.
- Dashboard global e SSE exigem `ADMIN`.

## Triagem inteligente

Ao criar um chamado, a API chama a interface `TicketClassifier` para sugerir
categoria e prioridade com base no titulo e na descricao.

Implementacao:

- Quando `GROQ_CLASSIFIER_ENABLED=true` e `GROQ_API_KEY` esta preenchida, a API
  tenta classificar usando a API da Groq.
- Se a chave estiver vazia, a integracao estiver desabilitada ou a Groq falhar,
  a API usa uma heuristica deterministica local de palavras-chave.
- O chamado nasce com `classificationOrigin=IA`, pois a classificacao inicial
  foi automatica.
- Quando o ADMIN corrige categoria ou prioridade, o chamado passa a
  `classificationOrigin=MANUAL` e a alteracao e registrada no historico.

Justificativa:

- O desafio permite API externa, modelo local ou heuristica/mock desde que a
  abordagem funcione e esteja explicada.
- A integracao real fica atras de uma interface, mantendo o dominio desacoplado
  do provedor externo.
- O fallback local permite reproduzir a entrega sem chave secreta, internet ou
  limite disponivel em API externa.

Regras da heuristica local:

| Termos | Resultado |
| --- | --- |
| `senha`, `login`, `acesso` | Categoria `ACESSO` |
| `internet`, `rede`, `wifi` | Categoria `REDE` |
| `computador`, `impressora`, `teclado` | Categoria `HARDWARE` |
| `sistema`, `erro`, `bug`, `pagina` | Categoria `SOFTWARE` |
| `fora do ar`, `indisponivel`, `urgente`, `parada` | Prioridade `ALTA` |
| `duvida`, `ajuste`, `baixa criticidade` | Prioridade `BAIXA` |
| Sem correspondencia | Categoria `OUTROS` e prioridade `MEDIA` |

## Dashboard e SSE

O dashboard administrativo usa:

- `GET /api/dashboard/indicators` para obter o estado atual.
- `GET /api/dashboard/events` para manter uma conexao SSE aberta.

Quando um chamado e criado ou atualizado, a API publica eventos internos e
envia atualizacoes para os clientes conectados. Chamados de prioridade `ALTA`
geram evento especifico `high-priority-alert`.

Justificativa:

- SSE atende bem ao caso de uso porque o fluxo principal e servidor para
  cliente.
- A implementacao e mais simples que WebSocket para notificacoes unidirecionais
  de dashboard.
- O endpoint permanece protegido por JWT e restrito a `ADMIN`.

## Regras de negocio

- `email` de usuario e unico.
- Senhas nunca sao retornadas em DTOs de resposta.
- Chamado nasce com status `ABERTO`.
- Categoria e prioridade iniciais sao definidas automaticamente.
- Transicoes permitidas:

| Status atual | Proximos status permitidos |
| --- | --- |
| `ABERTO` | `EM_ANDAMENTO`, `RESOLVIDO`, `FECHADO` |
| `EM_ANDAMENTO` | `RESOLVIDO`, `FECHADO` |
| `RESOLVIDO` | `FECHADO` |
| `FECHADO` | nenhum |

- Alteracoes de status e classificacao geram historico.
- Comentarios sao listados em ordem cronologica.
- Campos administrativos (`priority`, `category`, `responsibleId`) exigem
  `ADMIN`.

## Tratamento de erros

A API possui tratamento global para erros esperados:

| Status | Uso |
| --- | --- |
| `400` | Dados invalidos, payload malformado ou transicao de status invalida |
| `401` | Ausencia de autenticacao ou token invalido |
| `403` | Usuario autenticado sem permissao |
| `404` | Recurso nao encontrado |
| `409` | Conflito, como e-mail duplicado |
| `500` | Erro interno inesperado |

Formato geral:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Dados invalidos",
  "timestamp": "2026-08-15T12:00:00"
}
```

## Swagger

Com a API em execucao:

- Interface: `http://localhost:8080/swagger-ui.html`
- Especificacao JSON: `http://localhost:8080/v3/api-docs`

No Swagger UI, use o botao `Authorize` e informe o token JWT retornado no login.

## Testes

Executar todos os testes da API:

```bash
cd api
./mvnw test
```

O projeto configura H2 em memoria para os testes via Maven Surefire, em modo
compativel com PostgreSQL. Para apenas compilar e montar o contexto sem rodar
testes:

```bash
cd api
./mvnw -DskipTests test
```

Testes cobrem autenticacao, autorizacao, regras de negocio criticas,
classificacao, comentarios, dashboard, SSE e documentacao OpenAPI.

## Estrutura

```text
api/
  src/main/java/helpdesk/api/
    auth/        autenticacao, JWT e usuario autenticado
    config/      seguranca, CORS, Swagger e seed
    dashboard/   indicadores e SSE
    error/       tratamento global de erros
    ticket/      chamados, comentarios e classificacao
    user/        usuarios e repositorio
web/
  src/
    features/    clientes e tipos por dominio
    routes/      telas da aplicacao
    components/  componentes reutilizaveis
docs/spacks/     specs incrementais do projeto
```
