# SENTINEL

O SENTINEL é uma plataforma para organizar o histórico técnico de projetos de software.

Durante o desenvolvimento, decisões importantes ficam espalhadas entre issues, pull requests, commits e documentos. Depois de algum tempo, entender por que uma solução foi escolhida pode exigir uma busca manual em várias ferramentas.

O SENTINEL reúne esse histórico e relaciona os acontecimentos do projeto. A ideia não é apenas registrar o que foi feito, mas preservar também o contexto e os motivos por trás das decisões.

## Exemplo

Uma equipe pode registrar a decisão de utilizar uma determinada biblioteca em um módulo. Essa decisão pode ser relacionada à issue que descreveu o problema, ao pull request que implementou a mudança e aos commits envolvidos.

```text
Decisão: utilizar a Biblioteca X no módulo Y

Relacionamentos:
	- Issue #31
	- Pull Request #44
	- Commit 8f31a2
	- Issue #52
```

Assim, quando alguém encontrar essa parte do sistema no futuro, poderá consultar a decisão e recuperar o contexto sem precisar procurar em cada ferramenta separadamente.

## MVP

O primeiro objetivo do projeto é permitir que uma equipe conecte um repositório do GitHub ao SENTINEL e construa uma memória estruturada desse projeto.

O MVP contempla:

- cadastro e autenticação de usuários;
- cadastro de projetos;
- integração com o GitHub;
- coleta de issues, pull requests e commits;
- normalização e persistência dos eventos;
- registro de decisões técnicas;
- relacionamento entre decisões e eventos do projeto;
- busca textual;
- histórico básico do projeto;
- API REST;
- processamento da coleta em Go;
- testes automatizados.

## Arquitetura

O sistema será dividido em um serviço principal e um serviço de ingestão.

### Serviço principal

Desenvolvido em Java com Quarkus, o serviço principal concentra o domínio do SENTINEL. Ele será responsável por usuários, projetos, repositórios, decisões técnicas, relacionamentos, regras de negócio, autenticação, persistência e API REST.

Esse serviço funciona como a fonte de verdade do sistema.

### Serviço de ingestão

Desenvolvido em Go, o serviço de ingestão faz a comunicação com sistemas externos. No início, seu foco será o GitHub: ele consulta issues, pull requests e commits, normaliza os dados, identifica novos eventos e os envia para o serviço principal.

O fluxo inicial é:

```text
GitHub
	↓
Serviço de ingestão (Go)
	↓
Normalização e processamento
	↓
Serviço principal (Java / Quarkus)
	↓
Banco de dados
```

Essa separação permite que a coleta, que depende de várias requisições externas, não fique acoplada às regras principais do sistema.

## Modelo do projeto

O SENTINEL trata um projeto como uma coleção de eventos técnicos relacionados. Uma issue pode dar origem a um pull request e a vários commits; uma decisão técnica pode estar ligada a todos esses acontecimentos.

```text
Issue
	├── Pull Request
	│     └── Commit
	└── Decisão técnica
				└── Alteração relacionada
```

Esse relacionamento é o núcleo do produto e permite consultar o histórico de uma decisão com seus eventos de origem.

## Fora do escopo inicial

O MVP não inclui:

- respostas com IA generativa;
- geração automática de decisões;
- busca semântica com embeddings;
- integrações com GitLab, Jira, Slack ou Discord;
- dashboard avançado de métricas;
- aplicativo mobile;
- recomendações automáticas;
- análise preditiva do projeto.

A IA poderá ser considerada no futuro, depois que a base de conhecimento estiver estruturada. O primeiro passo é construir um histórico confiável e relacionado, que seja útil mesmo sem um chatbot.

## Público-alvo

O foco inicial são desenvolvedores, tech leads e equipes pequenas e médias que utilizam o GitHub no fluxo de desenvolvimento.

## Estrutura do repositório

- `frontend/`: interface do sistema;
- `services/core/quarkus/`: serviço principal em Java com Quarkus;
- `services/ingestion/go/`: serviço de ingestão em Go;
- `infra/`: configurações de infraestrutura, banco de dados e containers;
- `docs/`: documentação complementar.

## Execução Local com Docker Compose

O banco de dados PostgreSQL do SENTINEL é orquestrado via `docker-compose.yml` na raiz:

```bash
# Subir o PostgreSQL em background
docker compose up -d

# Visualizar logs do banco
docker compose logs -f postgres

# Parar o container
docker compose down
```

As variáveis de ambiente podem ser customizadas no arquivo `.env` (baseado no `.env.example`).

## Migrações de Banco de Dados (Flyway)

O esquema do banco de dados é versionado e gerenciado pelo Flyway:
- `V1__create_auth_and_projects.sql`: tabelas `users`, `refresh_sessions` e `projects`.
- `V2__create_technical_decisions.sql`: tabela `technical_decisions` com integridade referencial (`ON DELETE CASCADE`) e índices em `project_id` e `status`.

As migrações são executadas automaticamente na inicialização da aplicação (`quarkus.flyway.migrate-at-start=true`), e a validação estrita do Hibernate ORM (`quarkus.hibernate-orm.database.generation=validate`) assegura que o ORM não altera o esquema.

## Arquitetura (Clean Architecture)

O serviço principal em Quarkus segue a **Clean Architecture** (Ports & Adapters):

- `com.sentinel.core.domain`: entidades e records puros (`Project`, `TechnicalDecision`, `DecisionStatus`, `PageResult`) e portas de repositório (`ProjectRepository`, `TechnicalDecisionRepository`), **100% agnósticos de frameworks**.
- `com.sentinel.core.application`: casos de uso (`ListProjectsUseCase`, `GetProjectUseCase`, `CreateDecisionUseCase`, `ListDecisionsUseCase`, `GetDecisionUseCase`).
- `com.sentinel.core.adapter.in.rest`: adaptadores JAX-RS REST (`ProjectResource`, `TechnicalDecisionResource`), DTOs com validação Bean Validation e tratamento de erros RFC 9457 (`ProblemDetailsExceptionMapper`).
- `com.sentinel.core.adapter.out.persistence`: adaptadores Panache/PostgreSQL (`PanacheProjectRepository`, `PanacheTechnicalDecisionRepository`) e entidades JPA com conversão isolada.
- `com.sentinel.core.adapter.out.memory`: implementações de repositório em memória (`InMemoryProjectRepository`, `InMemoryTechnicalDecisionRepository`) para testes unitários rápidos.

A pureza arquitetural é verificada de forma contínua pelo teste ArchUnit em `ArchitectureTest.java`.

## Endpoints e OpenAPI (Swagger UI)

Todos os endpoints são documentados e acessíveis no runtime:

- **Swagger UI**: `http://localhost:8080/q/swagger-ui`
- **Especificação OpenAPI**: `http://localhost:8080/q/openapi`

### Endpoints da Sprint 1 (`/api/v1`):
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/auth/register` | Cadastro de usuário |
| POST | `/api/v1/auth/login` | Login e emissão de JWT |
| POST | `/api/v1/auth/refresh` | Rotação de refresh token |
| POST | `/api/v1/auth/logout` | Revogação de sessão |
| GET | `/api/v1/auth/me` | Dados do usuário autenticado |
| GET | `/api/v1/projects` | Listagem paginada de projetos com filtro por nome |
| GET | `/api/v1/projects/{id}` | Consulta de projeto por ID |
| POST | `/api/v1/projects/{projectId}/decisions` | Criação de decisão técnica (201 + `Location`) |
| GET | `/api/v1/projects/{projectId}/decisions` | Listagem paginada de decisões com filtros por `status` e `title` |
| GET | `/api/v1/projects/{projectId}/decisions/{decisionId}` | Consulta de decisão técnica por ID |

### Padronização de Erros (RFC 9457 Problem Details)
Todas as falhas de validação e regras de negócio respondem com `application/problem+json`:
- `400 Bad Request`: erros de validação sintática ou de campos (com lista `invalidParams`).
- `401 Unauthorized`: token ausente ou inválido.
- `404 Not Found`: recurso ou projeto inexistente.
- `409 Conflict`: duplicidade (ex: email já cadastrado).
- `500 Internal Server Error`: erros inesperados sem vazamento de stacktrace.

## Testes Automatizados e CI

A suíte de testes contempla:
- **Testes Unitários**: domínio e casos de uso com repositórios em memória.
- **Testes de Arquitetura**: ArchUnit verificando ausência de acoplamento do domínio.
- **Testes de Integração REST**: `@QuarkusTest` com banco em memória H2 (modo PostgreSQL) e Flyway executando no perfil `%test`.

Para rodar os testes localmente:
```bash
cd services/core/quarkus
mvn -B -ntp test
```

## Apresentação da Sprint 1 (Vídeo)

- **Roteiro detalhado de gravação:** [docs/roteiro-video-sprint-1.md](docs/roteiro-video-sprint-1.md)
- **Vídeo de Demonstração (5 min):** [Assistir no YouTube/Drive](https://youtu.be/SEU_LINK_AQUI) *(substituir pelo link após a gravação)*

## Realizado por:
- [João Eduardo](https://github.com/joapedu)
- [Luis Eduardo](https://github.com/edurs2602)
- [Lucas Mangabeira](https://github.com/Mangabas)
