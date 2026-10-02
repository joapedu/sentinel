# Roteiro de Gravação do Vídeo da Sprint 1 — SENTINEL
**Duração Alvo:** ~5 minutos (± 30 segundos)  
**Disciplina:** DIM0547 — Desenvolvimento de Sistemas Web II  
**Integrantes:**
- **Lucas Mangabeira** (@Mangabas)
- **João Eduardo** (@joapedu)
- **Luis Eduardo** (@edurs2602)

---

## Estrutura e Divisão de Tempo

| Bloco | Duração | Tópico | Responsável | O que mostrar na tela |
|---|---|---|---|---|
| **1** | 00:00 - 01:15 | Apresentação, Visão do SENTINEL, Entidades e Demonstração do CRUD | **Lucas Mangabeira** | Apresentação rápida + Swagger UI (`/q/swagger-ui`) |
| **2** | 01:15 - 02:30 | Persistência, Migrações Flyway e Docker Compose | **João Eduardo** | `docker-compose.yml`, arquivos SQL `V1` e `V2`, logs do Flyway |
| **3** | 02:30 - 03:45 | Clean Architecture e Teste de Arquitetura com ArchUnit | **Luis Eduardo** | Árvore de pacotes (`domain`, `application`, `adapter`) e `ArchitectureTest.java` |
| **4** | 03:45 - 05:00 | Suíte de Testes Automatizados, CI no GitHub Actions e Conclusão | **Lucas / Todos** | Terminal rodando `mvn test` + tela do GitHub Actions |

---

## Fala a Fala Detalhado

---

### Bloco 1: Abertura, Produto e Demonstração do CRUD (00:00 – 01:15)
**Responsável:** Lucas Mangabeira  
**O que mostrar:** Slide de introdução / Tela do Swagger UI (`http://localhost:8080/q/swagger-ui`)

> **[Lucas]:**  
> *"Olá a todos! Somos a equipe do SENTINEL, composta por mim, Lucas Mangabeira, pelo João Eduardo e pelo Luis Eduardo.*  
>  
> *O SENTINEL é uma plataforma de memória operacional para projetos de software. Nosso objetivo é conectar acontecimentos do desenvolvimento — como commits, PRs e issues — às decisões técnicas de arquitetura, preservando não apenas o que foi feito, mas o porquê foi feito.*  
>  
> *Para esta primeira sprint, o foco foi a fundação técnica: autenticação, projetos e decisões técnicas em um relacionamento 1:N.*  
>  
> *(Mostrando o Swagger UI)*  
> *Aqui na Swagger UI vemos a nossa API REST gerada automaticamente pelo SmallRye OpenAPI. Temos as rotas de projetos e as rotas aninhadas de decisões técnicas.*  
>  
> *Primeiro, na rota `GET /api/v1/projects`, listamos os projetos do usuário com paginação e filtro por nome.*  
>  
> *Agora, na rota aninhada `POST /api/v1/projects/{projectId}/decisions`, criamos uma decisão técnica vinculada a este projeto. Notem que a resposta é um `201 Created` trazendo o cabeçalho `Location` com a URI do recurso criado.*  
>  
> *Podemos consultar a lista paginada em `GET /api/v1/projects/{projectId}/decisions` com filtros por status (`ACCEPTED`, `PROPOSED`, etc.) e busca textual no título.*  
>  
> *Além disso, se enviarmos uma requisição inválida com campos em branco, a API rejeita com status `400 Bad Request` no formato padrão RFC 9457 Problem Details (`application/problem+json`), detalhando as violações na lista de `invalidParams`, sem nunca expor erro 500.*  
>  
> *Agora passo a palavra para o João Eduardo, que vai explicar a infraestrutura e a camada de persistência."*

---

### Bloco 2: Persistência, Migrações Flyway e Docker Compose (01:15 – 02:30)
**Responsável:** João Eduardo  
**O que mostrar:** `docker-compose.yml` no VS Code/IntelliJ, pasta `db/migration/`, terminal com `docker compose ps`

> **[João Eduardo]:**  
> *"Obrigado, Lucas! Falando sobre a infraestrutura e persistência, nosso banco de dados relacional é o PostgreSQL 16.*  
>  
> *(Mostrando o docker-compose.yml)*  
> *Para que qualquer pessoa do time consiga subir o ambiente do zero sem passos manuais, configuramos o `docker-compose.yml` na raiz do monorepo, com volume persistente nomeado e variáveis de ambiente documentadas no `.env.example`. Basta um `docker compose up -d` e o banco fica pronto para receber conexões.*  
>  
> *(Mostrando os arquivos V1 e V2 em db/migration)*  
> *O esquema do banco é 100% versionado via Flyway. Temos a migração `V1` criando as tabelas de usuários, sessões e projetos, e a migração `V2`, que modela a tabela `technical_decisions`. Nela garantimos integridade referencial com chave estrangeira apontando para `projects` com `ON DELETE CASCADE`, além de índices em `project_id` e no `status` para otimizar as consultas.*  
>  
> *(Mostrando application.properties)*  
> *No `application.properties`, definimos `quarkus.flyway.migrate-at-start=true`, permitindo que o banco vazio atinja o estado final automaticamente ao subir a API. E, conforme exige a rubrica, a geração automática pelo ORM foi estritamente desligada com `quarkus.hibernate-orm.database.generation=validate`, garantindo que o Hibernate apenas valide o esquema construído pelo Flyway.*  
>  
> *Passo a palavra agora para o Luis Eduardo apresentar a arquitetura e o teste de verificação."*

---

### Bloco 3: Clean Architecture e Verificação com ArchUnit (02:30 – 03:45)
**Responsável:** Luis Eduardo  
**O que mostrar:** Estrutura de pacotes no explorador de arquivos e o código de `ArchitectureTest.java`

> **[Luis Eduardo]:**  
> *"Obrigado, João! Um dos pilares centrais da nossa entrega técnica é a adesão rigorosa à Clean Architecture e o desacoplamento de camadas.*  
>  
> *(Mostrando os pacotes no editor)*  
> *Organizamos o Core Quarkus em três camadas bem definidas:*  
> *1. `domain`: contém as entidades de negócio puras — `Project` e `TechnicalDecision` —, enums, paginação e as interfaces de portas de repositório. O domínio é Java puro: não importa nenhuma classe de frameworks como Jakarta, Quarkus ou Hibernate.*  
> *2. `application`: contém os casos de uso, como `CreateDecisionUseCase` e `ListProjectsUseCase`, orquestrando a lógica sem conhecer detalhes de persistência.*  
> *3. `adapter`: dividido em adaptadores de entrada (rotas REST em JAX-RS) e saída. Na saída, implementamos repositórios com Panache/PostgreSQL e também repositórios em memória (`InMemoryProjectRepository` e `InMemoryTechnicalDecisionRepository`), permitindo testar casos de uso de forma isolada.*  
>  
> *(Mostrando ArchitectureTest.java)*  
> *Para garantir que essas regras não sejam violadas no dia a dia, escrevemos testes automáticos de arquitetura usando o ArchUnit. Nosso teste verifica que a camada de domínio não possui dependências de pacotes externos como `jakarta..`, `io.quarkus..` ou `adapter..`.*  
>  
> *Se alguém tentar importar uma anotação de framework no domínio, o teste falha imediatamente e bloqueia a integração no CI.*  
>  
> *Volto a palavra para o Lucas demonstrar os testes e a integração contínua."*

---

### Bloco 4: Testes Automatizados, CI e Encerramento (03:45 – 05:00)
**Responsável:** Lucas Mangabeira (com encerramento do time)  
**O que mostrar:** Terminal rodando `mvn test` + Tela do GitHub com o workflow do GitHub Actions verde

> **[Lucas]:**  
> *"Para comprovar a robustez de toda a aplicação, construímos uma suíte completa de testes automatizados.*  
>  
> *(Executando no terminal: `mvn -B -ntp test`)*  
> *Temos testes unitários do domínio, testes de casos de uso com repositórios em memória, os testes de arquitetura com ArchUnit e testes de integração REST com `@QuarkusTest`, cobrindo autenticação JWT, CRUD, paginação, filtros e as respostas RFC 9457.*  
>  
> *(Mostrando o terminal finalizado com `BUILD SUCCESS` e 27 testes aprovados)*  
> *Como podem ver, todos os 27 testes passaram com 100% de sucesso.*  
>  
> *(Mudando para a aba do navegador no GitHub Actions)*  
> *Essa mesma suíte é executada automaticamente no nosso pipeline de CI via GitHub Actions em cada push e pull request, garantindo que a branch principal permaneça sempre estável e verde.*  
>  
> *Com isso, cobrimos com sucesso todos os objetivos da Sprint 1: persistência com migrações, Docker Compose, Clean Architecture verificada, OpenAPI, Problem Details e testes automatizados.*  
>  
> *Muito obrigado a todos!"*  
>  
> **[João e Luis]:** *"Obrigado!"*

---

## Checklist para a Gravação

- [ ] Subir o Docker Compose antes de iniciar o vídeo: `docker compose up -d`
- [ ] Iniciar a aplicação Quarkus em background: `mvn quarkus:dev`
- [ ] Deixar aberto no navegador:
  - Aba 1: Swagger UI ([http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui))
  - Aba 2: GitHub Actions com os checks verdes
- [ ] Deixar o VS Code/IntelliJ com as abas abertas:
  - `docker-compose.yml`
  - `V1` e `V2` (Flyway)
  - `ArchitectureTest.java`
  - Estrutura de pacotes visível na barra lateral
- [ ] Limpar o terminal para rodar o `mvn test` ao vivo no final.
