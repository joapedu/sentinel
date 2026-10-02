# Cartões da Sprint 1 — SENTINEL

Este documento contém a quebra das histórias P1 em cartões executáveis da Sprint 1, organizados para o **GitHub Projects**, com critérios de aceite alinhados à rubrica da disciplina DIM0547, estimativas em pontos de história e atribuição aos membros da equipe.

---

## Membros da Equipe
- **João Eduardo** (@joapedu)
- **Luis Eduardo** (@edurs2602)
- **Lucas Mangabeira** (@Mangabas)

---

## Convenção de Branches e Pull Requests
- **Branch**: `feat/<numero-tarefa>-<descricao-curta>` (ex.: `feat/t2-migracoes-decisoes`, `feat/t6-clean-architecture`)
- **PR**: Vincular ao cartão correspondente usando `Closes #<numero-issue>` ou `Resolves #<numero-issue>`.

---

## Cartões da Sprint

### [CARD-T1] Quebrar as histórias P1 em cartões da sprint
- **Responsável**: Lucas Mangabeira (@Mangabas)
- **Estimativa**: 2 pontos
- **Objetivo**: Estruturar o quadro da Sprint 1 no GitHub Projects, definindo responsáveis, tarefas T1 a T9 e vinculação com PRs.
- **Critérios de Aceite**:
  - [x] Histórias P1 da Sprint 1 movidas para a coluna de execução.
  - [x] Cartões das tarefas T2 a T9 criados com objetivos e critérios de aceite.
  - [x] Responsável atribuído a cada cartão.
  - [x] Cada cartão concluído vinculado ao respectivo Pull Request integrado.

---

### [CARD-T2] Modelar as entidades e escrever as migrações Flyway
- **Responsável**: João Eduardo (@joapedu)
- **Estimativa**: 3 pontos
- **Objetivo**: Ter o esquema do banco de dados versionado para `Project` e `TechnicalDecision`, com integridade referencial e sem geração automática por ORM.
- **Critérios de Aceite**:
  - [x] Migração `V2__create_technical_decisions.sql` criada com campos: `id`, `project_id`, `title`, `description`, `status`, `author`, `decision_date`, `created_at`, `updated_at`.
  - [x] Chave estrangeira `project_id REFERENCES projects(id) ON DELETE CASCADE`.
  - [x] Índices criados em `project_id` e `status`.
  - [x] Flyway configurado para migrar automaticamente na subida (`quarkus.flyway.migrate-at-start=true`).
  - [x] Geração automática de esquema pelo Hibernate ORM desativada (`quarkus.hibernate-orm.database.generation=validate`).

---

### [CARD-T3] Subir o banco com Docker Compose
- **Responsável**: Lucas Mangabeira (@Mangabas)
- **Estimativa**: 2 pontos
- **Objetivo**: Disponibilizar o ambiente PostgreSQL via `docker compose` sem dependência de passos manuais.
- **Critérios de Aceite**:
  - [x] Arquivo `docker-compose.yml` na raiz declarando serviço `postgres:16-alpine`.
  - [x] Volume nomeado persistente `postgres_data`.
  - [x] Variáveis de ambiente configuradas com fallbacks no `docker-compose.yml` e arquivo `.env.example`.
  - [x] Porta `5432:5432` exposta para o host.
  - [x] Instruções de subida e conexão documentadas no `README.md`.

---

### [CARD-T4] Implementar repositórios com banco e em memória
- **Responsável**: Luis Eduardo (@edurs2602)
- **Estimativa**: 5 pontos
- **Objetivo**: Desacoplar a persistência através de interfaces de repositório (ports), com implementações Panache (PostgreSQL) e em memória.
- **Critérios de Aceite**:
  - [x] Interfaces `ProjectRepository` e `TechnicalDecisionRepository` definidas no domínio.
  - [x] Implementações Panache com entidades JPA mapeadas de/para objetos de domínio em camada isolada.
  - [x] Anotação `@Transactional` aplicada nas operações de escrita.
  - [x] Implementações `InMemoryProjectRepository` e `InMemoryTechnicalDecisionRepository` criadas para testes rápidos sem banco.

---

### [CARD-T5] Implementar endpoints de leitura de projetos e criação/leitura de decisões com paginação e filtros
- **Responsável**: João Eduardo (@joapedu)
- **Estimativa**: 5 pontos
- **Objetivo**: Expor os endpoints REST das entidades `Project` e `TechnicalDecision` com semântica HTTP correta, paginação e filtros.
- **Critérios de Aceite**:
  - [x] `GET /api/v1/projects`: Listagem paginada (`page`, `size`) com filtro por `name`.
  - [x] `GET /api/v1/projects/{id}`: Detalhe do projeto garantindo ownership do usuário autenticado.
  - [x] `POST /api/v1/projects/{projectId}/decisions`: Criação de decisão técnica retornando `201 Created` e header `Location`.
  - [x] `GET /api/v1/projects/{projectId}/decisions`: Listagem paginada (`page`, `size`) com filtros por `status` e `title`.
  - [x] `GET /api/v1/projects/{projectId}/decisions/{decisionId}`: Detalhes da decisão técnica vinculada ao projeto.
  - [x] Códigos de status semânticos aplicados (200, 201, 400, 404).

---

### [CARD-T6] Separar as camadas e escrever o teste de arquitetura ArchUnit
- **Responsável**: Lucas Mangabeira (@Mangabas)
- **Estimativa**: 5 pontos
- **Objetivo**: Organizar a aplicação segundo a Clean Architecture e garantir por teste automatizado que o domínio não depende de frameworks.
- **Critérios de Aceite**:
  - [x] Pacotes organizados em `domain`, `application` (casos de uso) e `adapter` (in/out).
  - [x] Classes de domínio sem anotações de Jakarta, Quarkus ou Hibernate.
  - [x] Teste de arquitetura com ArchUnit (`ArchitectureTest.java`) validando o isolamento do domínio.
  - [x] Teste executado no CI através de `mvn test`.

---

### [CARD-T7] Validar entradas e responder erros em Problem Details (RFC 9457)
- **Responsável**: Luis Eduardo (@edurs2602)
- **Estimativa**: 3 pontos
- **Objetivo**: Assegurar validação rigorosa de entradas com Bean Validation e padronização de respostas de erro em RFC 9457 (`application/problem+json`).
- **Critérios de Aceite**:
  - [x] Bean Validation em todos os corpos e parâmetros de entrada.
  - [x] Erros de validação e sintaxe retornam `400 Bad Request` com lista de violações (`invalidParams`).
  - [x] Exceções de recurso não encontrado retornam `404 Not Found`.
  - [x] Exceções inesperadas retornam `500 Internal Server Error` controlado em Problem Details sem expor stacktraces.
  - [x] Content-Type `application/problem+json` em todas as respostas de erro.

---

### [CARD-T8] Gerar e publicar documentação OpenAPI e Swagger UI
- **Responsável**: João Eduardo (@joapedu)
- **Estimativa**: 2 pontos
- **Objetivo**: Disponibilizar documentação viva da API REST gerada diretamente a partir do código.
- **Critérios de Aceite**:
  - [x] Extensão `quarkus-smallrye-openapi` configurada.
  - [x] Swagger UI acessível no endpoint `/q/swagger-ui`.
  - [x] Especificação OpenAPI acessível no endpoint `/q/openapi`.
  - [x] Todas as rotas de projetos e decisões documentadas com parâmetros, corpos e status de resposta.
  - [x] Endereços documentados no `README.md`.

---

### [CARD-T9] Escrever testes automatizados e integrá-los no CI
- **Responsável**: Lucas Mangabeira (@Mangabas)
- **Estimativa**: 4 pontos
- **Objetivo**: Garantir cobertura de testes unitários, testes de arquitetura e integração REST executando no CI.
- **Critérios de Aceite**:
  - [x] Testes unitários do domínio e casos de uso com repositórios em memória.
  - [x] Testes de arquitetura ArchUnit validando as fronteiras de camadas.
  - [x] Testes de integração REST com `@QuarkusTest` validando rotas, paginação, filtros e Problem Details.
  - [x] Suíte executada e com status verde no GitHub Actions via `.github/workflows/ci.yml`.
