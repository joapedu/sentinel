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

## Status

O projeto está em desenvolvimento, com o escopo inicial definido no documento [proposta.md](proposta.md).


## Realizado por:
- [João Eduardo](https://github.com/joapedu)
- [Luis Eduardo](https://github.com/edurs2602)
- [Lucas Mangabeira](https://github.com/Mangabas)
