# Helpdesk

Central de chamados helpdesk com API em Spring Boot.

## Configuracao local

Crie o arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

Gere um segredo local para assinar os tokens JWT:

```bash
openssl rand -base64 32
```

Use o valor gerado em `JWT_SECRET` no `.env`. Para substituir automaticamente:

```bash
sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$(openssl rand -base64 32)|" .env
```

## Usuarios de teste

A API cria os usuarios abaixo ao iniciar, sem duplica-los em reinicializacoes:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| ADMIN | `admin@helpdesk.local` | `admin123` |
| SOLICITANTE | `user@helpdesk.local` | `user123` |

As senhas sao persistidas com hash BCrypt.

## Classificacao automatica

Ao criar um chamado, a API classifica categoria e prioridade automaticamente por
IA usando a API da Groq quando `GROQ_API_KEY` estiver configurada. Se a chave
nao estiver presente, se a integracao estiver desabilitada ou se a Groq falhar,
a API usa uma heuristica local de palavras-chave como fallback. A origem da
classificacao inicial e registrada como `IA`.

Decisao tecnica:

- O campo `classificationOrigin` segue o modelo do desafio e diferencia apenas
  classificacao automatica (`IA`) de correcao humana (`MANUAL`).
- A implementacao principal usa Groq atras da interface `TicketClassifier`,
  mantendo o service desacoplado do provedor externo.
- A heuristica deterministica local permanece como fallback. Isso garante que a
  criacao de chamados continue funcional sem chave externa, sem internet ou em
  caso de indisponibilidade/limite da API.
- A origem inicial permanece `IA`, pois no dominio do desafio ela representa
  classificacao automatica. Correcoes feitas pelo ADMIN continuam sendo
  registradas como `MANUAL`.

Variaveis de ambiente da integracao:

| Variavel | Padrao | Descricao |
| --- | --- | --- |
| `GROQ_CLASSIFIER_ENABLED` | `true` | Habilita tentativa de classificacao pela Groq |
| `GROQ_API_KEY` | vazio | Chave da API da Groq; vazio aciona fallback local |
| `GROQ_BASE_URL` | `https://api.groq.com/openai/v1` | Base URL compativel com OpenAI |
| `GROQ_MODEL` | `openai/gpt-oss-20b` | Modelo usado na classificacao |
| `GROQ_TIMEOUT` | `3s` | Timeout de conexao e leitura |

Regras iniciais:

| Termos | Resultado |
| --- | --- |
| `senha`, `login`, `acesso` | Categoria `ACESSO` |
| `internet`, `rede`, `wifi` | Categoria `REDE` |
| `computador`, `impressora`, `teclado` | Categoria `HARDWARE` |
| `sistema`, `erro`, `bug`, `pagina` | Categoria `SOFTWARE` |
| `fora do ar`, `indisponivel`, `urgente`, `parada` | Prioridade `ALTA` |
| `duvida`, `ajuste`, `baixa criticidade` | Prioridade `BAIXA` |
| Sem correspondencia | Categoria `OUTROS` e prioridade `MEDIA` |
