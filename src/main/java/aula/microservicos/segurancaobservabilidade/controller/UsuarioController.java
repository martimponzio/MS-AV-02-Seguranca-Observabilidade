package aula.microservicos.segurancaobservabilidade.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import aula.microservicos.segurancaobservabilidade.dto.LoginRequest;
import aula.microservicos.segurancaobservabilidade.dto.TrocaSenhaRequest;
import aula.microservicos.segurancaobservabilidade.dto.UsuarioRequest;
import aula.microservicos.segurancaobservabilidade.dto.UsuarioResponse;
import aula.microservicos.segurancaobservabilidade.model.Usuario;
import aula.microservicos.segurancaobservabilidade.repository.UsuarioRepository;
import aula.microservicos.segurancaobservabilidade.service.LoginResultado;
import aula.microservicos.segurancaobservabilidade.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Segurança", description = "Login, usuários, bloqueio e troca de senha")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final LoginService loginService;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            LoginService loginService) {
        this.usuarioRepository = usuarioRepository;
        this.loginService = loginService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Efetua login",
            description = "Valida username e senha diretamente na tabela usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário e senha válidos"),
            @ApiResponse(responseCode = "401", description = "Usuário/senha inválidos ou usuário bloqueado",
                    content = @Content)
    })
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest login) {

        LoginResultado resultado = loginService.autenticar(login);

        if (resultado == LoginResultado.VALIDO) {
            return ResponseEntity.ok(Map.of("mensagem", "Login válido"));
        }

        if (resultado == LoginResultado.BLOQUEADO) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário bloqueado"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Usuário ou senha inválidos"));
    }

    @PutMapping("/trocarsenha/{userid}")
    @Operation(
            summary = "Troca a senha",
            description = "Endpoint público. A senha atual é validada na tabela usuarios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "401", description = "Senha atual inválida"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado")
    })
    public ResponseEntity<Map<String, String>> trocarSenha(
            @PathVariable String userid,
            @Valid @RequestBody TrocaSenhaRequest senha) {

        Usuario usuario = usuarioRepository.buscar(userid).orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Usuário não localizado"));
        }

        if (!usuario.senha().equals(senha.atual())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Senha atual inválida"));
        }

        usuarioRepository.trocarSenha(userid, senha.nova());

        return ResponseEntity.ok(
                Map.of("mensagem", "Senha alterada com sucesso"));
    }

    @PostMapping("/")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "Cadastra um usuário",
            description = "Requer ROLE_MANUTENCAO. Novos usuários recebem ROLE_USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem ROLE_MANUTENCAO"),
            @ApiResponse(responseCode = "409", description = "Username já cadastrado")
    })
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody UsuarioRequest usuario) {

        try {
            usuarioRepository.cadastrar(
                    usuario.username(),
                    usuario.senha());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Usuário cadastrado com sucesso"));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Username já cadastrado"));
        }
    }

    @PutMapping("/unlock/{userid}")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "Desbloqueia um usuário",
            description = "Requer ROLE_ADMIN e zera a quantidade de falhas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário desbloqueado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado")
    })
    public ResponseEntity<Map<String, String>> desbloquear(
            @PathVariable String userid) {

        if (!usuarioRepository.existe(userid)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Usuário não localizado"));
        }

        usuarioRepository.desbloquear(userid);

        return ResponseEntity.ok(
                Map.of("mensagem", "Usuário desbloqueado com sucesso"));
    }

    @GetMapping("/")
    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "Lista usuários",
            description = "Retorna todos os usuários cadastrados. Requer ROLE_MANUTENCAO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem ROLE_MANUTENCAO")
    })
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioRepository.listar());
    }
}
