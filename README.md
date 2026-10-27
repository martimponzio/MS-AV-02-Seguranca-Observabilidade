# MS-AV-02 - Segurança e Observabilidade

Implementação da atividade **MS-AV-02-Segurança e Observabilidade**, baseada no conteúdo publicado no repositório `esensato/ms-2026-02`.

## Tecnologias

- Java 17
- Spring Boot 3.5.0
- Spring Web
- Spring Security
- `JdbcUserDetailsManager`
- H2
- Spring Boot Actuator
- Micrometer + Prometheus
- Grafana
- OpenAPI / Swagger (`springdoc-openapi`)

## Endpoints

| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/login` | Público |
| PUT | `/trocarsenha/{userid}` | Público |
| POST | `/` | `ROLE_MANUTENCAO` |
| PUT | `/unlock/{userid}` | `ROLE_ADMIN` |
| GET | `/` | `ROLE_MANUTENCAO` |

## Usuários iniciais para teste

| Username | Senha | Roles |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN`, `ROLE_MANUTENCAO` |
| `manutencao` | `manut123` | `ROLE_MANUTENCAO` |
| `user` | `user123` | `ROLE_USER` |

Os endpoints protegidos utilizam **HTTP Basic**, conforme o conteúdo de Spring Security do professor.

## Login e bloqueio

O endpoint `POST /login` consulta diretamente a tabela `usuarios`.

- login válido: HTTP 200;
- login inválido: HTTP 401;
- primeira falha: `failed_attempts = 1`;
- segunda falha: o usuário passa a `locked = TRUE`;
- usuário bloqueado continua recebendo HTTP 401 até ser desbloqueado por `PUT /unlock/{userid}`.

Um login válido zera as tentativas anteriores.

## JdbcUserDetailsManager e roles

O enunciado fornece as tabelas `usuarios` e `login_attempts`, mas não fornece uma estrutura para armazenar roles.

Foi acrescentada a tabela `authorities`, seguindo o padrão mostrado pelo professor na seção de `JdbcUserDetailsManager`.

O `JdbcUserDetailsManager` foi configurado para:

- buscar username/senha na tabela `usuarios`;
- considerar `login_attempts.locked` como o estado `enabled`;
- buscar roles na tabela `authorities`.

Como a tabela do enunciado limita `senha` a `VARCHAR(10)`, a senha é mantida como no modelo fornecido. Na consulta usada pelo Spring Security é aplicado o prefixo `{noop}`, padrão também apresentado pelo professor.

Novos usuários cadastrados por `POST /` recebem `ROLE_USER`, pois o corpo exigido pelo enunciado contém somente `username` e `senha`.

## Ajuste no SQL fornecido

No SQL do enunciado existe uma vírgula após:

```sql
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

Ela foi removida em `data.sql`, pois impediria a criação da tabela no H2.

## Métricas

Foram criados os indicadores:

- `logins.validos` — contador de logins válidos;
- `logins.invalidos` — contador de logins inválidos;
- `usuarios.bloqueados` — gauge com o total atual de usuários bloqueados.

Actuator:

- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/metrics/logins.validos`
- `http://localhost:8080/actuator/metrics/logins.invalidos`
- `http://localhost:8080/actuator/metrics/usuarios.bloqueados`

Prometheus:

- `http://localhost:8080/actuator/prometheus`

Nomes expostos ao Prometheus:

- `logins_validos_total`
- `logins_invalidos_total`
- `usuarios_bloqueados`

## OpenAPI / Swagger

Swagger UI:

`http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON:

`http://localhost:8080/v3/api-docs`

OpenAPI YAML:

`http://localhost:8080/v3/api-docs.yaml`

## H2

Console:

`http://localhost:8080/h2-console`

Configuração:

- JDBC URL: `jdbc:h2:mem:segurancadb`
- User: `sa`
- Password: `password`

## Grafana

Foi incluído o dashboard exportado em:

`observability/grafana/dashboards/ms-av-02-dashboard.json`

O dashboard contém três indicadores:

1. Logins válidos;
2. Logins inválidos;
3. Usuários bloqueados.

Também foi incluído um `docker-compose.yml` opcional para subir Prometheus e Grafana de forma simples.

Com Docker Desktop:

```powershell
docker compose up -d
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Grafana: `admin` / `admin`

O Prometheus foi configurado para consultar a aplicação Spring em `host.docker.internal:8080`, adequado ao Docker Desktop no Windows.

## Evidência exigida na entrega

O enunciado exige um **print real do dashboard** no repositório.

Depois dos testes, salve o print em:

`evidencias/dashboard-grafana.png`

O projeto não contém um print artificial; ele deve ser produzido após a aplicação e o dashboard estarem funcionando.

## Testes

Consulte `TESTES.md` para o roteiro completo de testes com `curl.exe`.
