# Fluxo de autenticacao do SENTINEL

Este documento descreve o fluxo atualmente implementado no Core Quarkus em `services/core/quarkus`.

## Visao geral

O Core usa email normalizado e senha como identidade. O cadastro e o login entregam dois tokens:

- **Access token:** JWT curto, enviado no header `Authorization: Bearer <token>`.
- **Refresh token:** valor opaco, aleatorio e de longa duracao. Apenas o SHA-256 do valor e persistido.

O access token identifica o usuario pelo claim `sub`. A existencia de um JWT valido nao concede acesso a todos os dados: recursos de dominio tambem verificam o proprietario persistido.

## Endpoints

Todos os endpoints usam o prefixo `/api/v1`.

| Metodo | Endpoint | Protecao | Funcao |
| --- | --- | --- | --- |
| POST | `/auth/register` | Publica | Cria usuario e inicia uma sessao. Retorna `201`. |
| POST | `/auth/login` | Publica | Valida email e senha. Retorna `200`. |
| POST | `/auth/refresh` | Publica | Faz a rotacao do refresh token. Retorna `200`. |
| POST | `/auth/logout` | Publica | Revoga o refresh token informado. Retorna `204`. |
| GET | `/auth/me` | JWT obrigatorio | Retorna o usuario do `sub` do access token. |
| POST | `/projects` | JWT obrigatorio | Cria projeto pertencente ao usuario autenticado. Retorna `201`. |
| GET | `/projects` | JWT obrigatorio | Lista apenas projetos do usuario autenticado. |

Exemplo de cadastro:

```http
POST /api/v1/auth/register
Content-Type: application/json

{"email":"person@example.com","password":"uma-senha-com-12-caracteres"}
```

A resposta contem `accessToken`, `refreshToken`, `tokenType`, `expiresIn` e apenas os dados publicos do usuario. Senhas e hashes nunca sao retornados.

## Cadastro e login

1. O email e tratado com `trim` e convertido para minusculas usando `Locale.ROOT`.
2. A senha exige entre 12 e 72 caracteres e no maximo 72 bytes em UTF-8, limite adotado para nao permitir que o bcrypt ignore parte da credencial.
3. A senha e armazenada com bcrypt; o valor original nunca e persistido.
4. O indice unico case-insensitive impede dois cadastros para o mesmo email.
5. O login usa uma resposta generica `invalid_credentials` para email inexistente, senha incorreta ou usuario inativo.

Falha de cadastro por email existente retorna `409` com `email_already_registered`. Erros de validacao de JSON sao tratados pelo mecanismo Bean Validation do Quarkus.

## JWT

O access token e assinado pelo mecanismo SmallRye JWT e contem, no minimo:

| Claim | Uso |
| --- | --- |
| `sub` | UUID do usuario autenticado. |
| `iss` | `sentinel-core`. |
| `aud` | `sentinel-api`. |
| `iat` | Instante de emissao. |
| `exp` | Expiracao, por padrao 15 minutos. |
| `jti` | Identificador unico do token. |

O runtime valida assinatura, issuer, audience e expiracao antes de liberar qualquer endpoint anotado com `@Authenticated`. A chave de assinatura/verificacao deve ser fornecida por ambiente e nao deve ser commitada.

## Refresh token e logout

Cada login ou cadastro cria uma linha em `refresh_sessions`, com hash do token, usuario, criacao, expiracao e instante de revogacao.

O refresh segue este fluxo:

1. O cliente envia o refresh token para `/auth/refresh`.
2. O Core calcula SHA-256 e procura a sessao correspondente.
3. A sessao precisa estar nao revogada, dentro da validade e associada a usuario ativo.
4. O token usado e revogado imediatamente.
5. Um novo par access/refresh e emitido.

O uso de um refresh token ja revogado e tratado como possivel reutilizacao indevida: todas as sessoes ativas do usuario sao revogadas e a resposta e `401 invalid_credentials`. O logout revoga a sessao correspondente; ele nao precisa aguardar a expiracao do refresh token.

O access token ja emitido permanece valido ate `exp`. A revogacao imediata de access tokens exigira uma futura denylist ou introspeccao; por isso o access token e deliberadamente curto.

## Protecao de recursos

`/auth/me` e `/projects` exigem um JWT valido. O usuario e recuperado pelo UUID do claim `sub`.

No endpoint de projetos, a consulta sempre inclui `owner.id = sub`. Portanto, um usuario autenticado nao consegue listar projetos de outro usuario apenas alterando um identificador na requisicao. Novos recursos devem seguir a mesma regra de ownership e nao confiar somente na anotacao de autenticacao.

RBAC, equipes, convites e compartilhamento entre usuarios ainda nao fazem parte desta implementacao.

## Configuracao

As propriedades principais sao configuraveis por ambiente:

- `DB_JDBC_URL`, `DB_USERNAME` e `DB_PASSWORD`: PostgreSQL.
- `JWT_ISSUER` e `JWT_AUDIENCE`: identidade do emissor e publico da API.
- `JWT_PUBLIC_KEY` e `JWT_PRIVATE_KEY`: local das chaves JWT fora do repositorio.
- `AUTH_ACCESS_TOKEN_MINUTES`: validade do access token, padrao 15 minutos.
- `AUTH_REFRESH_TOKEN_DAYS`: validade do refresh token, padrao 30 dias.
- `CORS_ORIGINS`: origens permitidas, devendo ser restrita em producao.

O perfil de teste usa H2 em memoria e executa a migration Flyway. Em producao, `quarkus.hibernate-orm.database.generation=validate` impede que o ORM altere o schema automaticamente.

O frontend deve manter o access token em memoria. Se o refresh token for colocado em cookie, o ambiente precisa usar `HttpOnly`, `Secure`, `SameSite` adequado e protecao CSRF. A escolha definitiva do transporte do frontend ainda depende da implementacao do frontend.

## Core e servico Go

O servico Go nao deve reutilizar o JWT de uma sessao humana. A comunicacao futura Go -> Core deve usar uma credencial propria de maquina, com escopo restrito para ingestao, segredo separado por ambiente e rotacao operacional. Nenhum endpoint de ingestao esta implementado neste slice.

## Testes

No modulo Quarkus, executar:

```powershell
mvn -B -ntp test
```

Os testes atuais cobrem normalizacao de email, uso de bcrypt e armazenamento nao reversivel do refresh token. Os testes REST de fluxo completo, reutilizacao de refresh, assinatura invalida e isolamento entre dois usuarios sao a proxima camada necessaria antes de producao.

## Limites atuais

- Nao ha recuperacao de senha, verificacao de email, MFA ou OAuth.
- Nao ha RBAC ou membership de equipes.
- A revogacao imediata de access JWT ainda nao existe.
- A politica final de cookie/CSRF depende do frontend.
- A integracao autenticada do servico Go ainda precisa ser implementada.