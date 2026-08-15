# Contexto para Agentes

Este repositorio implementa uma central de chamados helpdesk. As specs de
entrega incremental ficam em `docs/spacks/` e devem guiar a ordem de trabalho.

## Stack

- Backend em Java 21 com Spring Boot 4.1.0.
- Maven Wrapper em `api/mvnw`.
- Spring Web MVC, Spring Security, Spring Data JPA e Bean Validation.
- PostgreSQL via `docker-compose.yml`.
- Pacote base da API: `helpdesk.api`.

## Estado atual

Specs ja implementadas no backend:

- Spec 00: projeto Spring Boot base.
- Spec 01: configuracao local com PostgreSQL.
- Spec 02: dominio de usuario.
- Spec 03: dominio de chamado.
- Spec 04: comentarios/historico de chamados.
- Spec 05: cadastro de usuario solicitante.

## Implementacoes recentes

### Cadastro de usuario

Arquivos principais:

- `api/src/main/java/helpdesk/api/auth/RegisterRequestDTO.java`
- `api/src/main/java/helpdesk/api/auth/UserResponseDTO.java`
- `api/src/main/java/helpdesk/api/auth/AuthService.java`
- `api/src/main/java/helpdesk/api/auth/AuthController.java`
- `api/src/main/java/helpdesk/api/config/SecurityConfig.java`
- `api/src/test/java/helpdesk/api/auth/AuthServiceTests.java`

Endpoint implementado:

```http
POST /api/auth/register
```

Payload esperado:

```json
{
  "name": "Maria Silva",
  "email": "maria@example.com",
  "password": "123456"
}
```

Comportamento:

- Valida `name`, `email` e `password` como obrigatorios via Bean Validation.
- Valida formato de e-mail via `@Email`.
- Bloqueia e-mail duplicado com `409 Conflict`.
- Salva senha usando `BCryptPasswordEncoder`.
- Cria usuario com papel padrao `SOLICITANTE`.
- Retorna dados do usuario sem expor senha ou hash.

Decisoes tecnicas tomadas:

- E-mail duplicado retorna `409 Conflict`, pois representa conflito com recurso
  existente.
- `SecurityConfig` libera publicamente apenas `POST /api/auth/register`.
- CSRF foi desabilitado na configuracao atual por se tratar de API stateless em
  preparacao para JWT.
- Demais rotas exigem autenticacao por padrao.

## Dominio existente

Usuario:

- Entidade `User` em `helpdesk.api.user`.
- Campos: `id`, `name`, `email`, `passwordHash`, `role`, `createdAt`.
- `email` possui constraint unica.
- `UserRole`: `ADMIN`, `SOLICITANTE`.
- `UserRepository` ja possui `findByEmail` e `existsByEmail`.

Chamado:

- Entidade `Ticket` em `helpdesk.api.ticket`.
- Enums: `TicketPriority`, `TicketStatus`, `TicketCategory`,
  `ClassificationOrigin`.
- Chamado nasce com status `ABERTO`.
- Relaciona solicitante obrigatorio e responsavel opcional.

Comentarios:

- Entidade `TicketComment`.
- Repositorio lista comentarios por chamado em ordem cronologica.

## Validacao

Comando de compilacao:

```bash
cd api
./mvnw -DskipTests test
```

Comando de testes:

```bash
cd api
./mvnw test
```

Observacao: os testes `@SpringBootTest` dependem de PostgreSQL acessivel em
`localhost:5432` com as credenciais padrao do projeto, a menos que variaveis de
ambiente sobrescrevam a configuracao.

Para subir o banco local:

```bash
docker compose up -d
```

## Cuidados para proximas tarefas

- Antes de implementar uma spec, ler o arquivo correspondente em `docs/spacks/`.
- Manter codigo novo dentro dos pacotes de dominio ja existentes quando fizer
  sentido.
- Usar o sufixo `DTO` no nome de todos os records/classes de transferencia de
  dados e nos respectivos arquivos, em toda a aplicacao. Exemplos:
  `RegisterRequestDTO`, `UserResponseDTO`, `LoginRequestDTO`,
  `TokenResponseDTO`.
- Nao retornar senha nem `passwordHash` em DTOs de resposta.
- Dashboard global e eventos SSE de dashboard devem exigir usuario autenticado
  com papel `ADMIN`; `SOLICITANTE` deve acessar apenas os proprios chamados.
- Nao commitar segredos reais; usar `.env.example` para documentar variaveis.
- Em consultas SQL/JPQL manuais, usar sintaxe SQL em letras maiusculas e manter
  nomes de campos, aliases e valores em letras minusculas quando aplicavel.
- Se uma decisao tecnica tiver mais de uma opcao razoavel, consultar o usuario
  antes de implementar.
