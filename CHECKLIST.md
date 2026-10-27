# Checklist do enunciado

| Requisito | Implementação |
|---|---|
| `POST /login` público | `UsuarioController.login` |
| Validação na tabela `usuarios` | `LoginService` + `UsuarioRepository.buscar` |
| HTTP 200 válido / 401 inválido | `UsuarioController.login` |
| `PUT /trocarsenha/{userid}` público | `UsuarioController.trocarSenha` |
| Validação da senha atual | `UsuarioController.trocarSenha` |
| `POST /` com `ROLE_MANUTENCAO` | `SecurityConfig` + `UsuarioController.cadastrar` |
| `JdbcUserDetailsManager` | `SecurityConfig.userDetailsService` |
| `PUT /unlock/{userid}` com `ROLE_ADMIN` | `SecurityConfig` + `UsuarioController.desbloquear` |
| `GET /` com `ROLE_MANUTENCAO` | `SecurityConfig` + `UsuarioController.listar` |
| Métrica de logins válidos | `logins.validos` / `logins_validos_total` |
| Métrica de logins inválidos | `logins.invalidos` / `logins_invalidos_total` |
| Métrica de usuários bloqueados | `usuarios.bloqueados` / `usuarios_bloqueados` |
| Bloqueio após mais de uma falha | `UsuarioRepository.registrarFalha` |
| OpenAPI | `OpenApiConfig` + anotações no controller |
| Swagger UI | `/swagger-ui/index.html` |
| Prometheus | `/actuator/prometheus` + `observability/prometheus.yml` |
| Dashboard Grafana | `observability/grafana/dashboards/ms-av-02-dashboard.json` |
| Export do dashboard | JSON incluído; reexportar após validação final |
| Print do dashboard | gerar após os testes e salvar em `evidencias/dashboard-grafana.png` |
| Repositório público | etapa final após os testes |
