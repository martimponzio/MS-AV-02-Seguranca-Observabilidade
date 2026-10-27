# Roteiro de testes - MS-AV-02

Os exemplos abaixo usam `curl.exe` no Windows.

## 1. Subir a aplicação

```powershell
mvn spring-boot:run
```

Aplicação: `http://localhost:8080`

## 2. Login válido

```powershell
curl.exe -i -X POST http://localhost:8080/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"user\",\"senha\":\"user123\"}"
```

Esperado: HTTP 200.

## 3. Primeiro login inválido

```powershell
curl.exe -i -X POST http://localhost:8080/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"user\",\"senha\":\"errada\"}"
```

Esperado: HTTP 401. Usuário ainda não está bloqueado.

## 4. Segundo login inválido

Repita o comando anterior.

Esperado: HTTP 401 e usuário bloqueado.

## 5. Login correto após bloqueio

```powershell
curl.exe -i -X POST http://localhost:8080/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"user\",\"senha\":\"user123\"}"
```

Esperado: HTTP 401 com indicação de usuário bloqueado.

## 6. Desbloquear usuário

```powershell
curl.exe -i -X PUT http://localhost:8080/unlock/user ^
  -u admin:admin123
```

Esperado: HTTP 200.

## 7. Trocar senha

```powershell
curl.exe -i -X PUT http://localhost:8080/trocarsenha/user ^
  -H "Content-Type: application/json" ^
  -d "{\"atual\":\"user123\",\"nova\":\"nova123\"}"
```

Esperado: HTTP 200.

## 8. Cadastrar novo usuário com manutenção

```powershell
curl.exe -i -X POST http://localhost:8080/ ^
  -u manutencao:manut123 ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"novo\",\"senha\":\"senha123\"}"
```

Esperado: HTTP 201.

## 9. Tentar cadastrar com ROLE_USER

```powershell
curl.exe -i -X POST http://localhost:8080/ ^
  -u user:nova123 ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"naopode\",\"senha\":\"senha123\"}"
```

Esperado: HTTP 403.

## 10. Listar usuários com manutenção

```powershell
curl.exe -i http://localhost:8080/ -u manutencao:manut123
```

Esperado: HTTP 200 e JSON com usuários.

## 11. Tentar listar com ROLE_USER

```powershell
curl.exe -i http://localhost:8080/ -u user:nova123
```

Esperado: HTTP 403.

## 12. Métricas no Actuator

```powershell
curl.exe http://localhost:8080/actuator/metrics/logins.validos
curl.exe http://localhost:8080/actuator/metrics/logins.invalidos
curl.exe http://localhost:8080/actuator/metrics/usuarios.bloqueados
```

## 13. Métricas Prometheus

```powershell
curl.exe http://localhost:8080/actuator/prometheus
```

Procurar:

- `logins_validos_total`
- `logins_invalidos_total`
- `usuarios_bloqueados`

## 14. Swagger

Abrir no navegador:

`http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON:

`http://localhost:8080/v3/api-docs`

## 15. Prometheus e Grafana

Com Docker Desktop em execução:

```powershell
docker compose up -d
```

Prometheus:

`http://localhost:9090`

Grafana:

`http://localhost:3000`

Login inicial do Grafana:

- usuário: `admin`
- senha: `admin`

O dashboard `MS-AV-02 - Segurança e Observabilidade` é provisionado automaticamente.

Depois de validar os três indicadores, gerar o print e salvar em:

`evidencias/dashboard-grafana.png`
