package aula.microservicos.segurancaobservabilidade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import aula.microservicos.segurancaobservabilidade.dto.UsuarioResponse;
import aula.microservicos.segurancaobservabilidade.model.Usuario;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Usuario> buscar(String username) {
        try {
            Usuario usuario = jdbc.queryForObject(
                    "SELECT username, senha FROM usuarios WHERE username = ?",
                    (rs, rowNum) -> new Usuario(
                            rs.getString("username"),
                            rs.getString("senha")),
                    username);
            return Optional.ofNullable(usuario);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existe(String username) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE username = ?",
                Integer.class,
                username);
        return total != null && total > 0;
    }

    @Transactional
    public void cadastrar(String username, String senha) {
        jdbc.update(
                "INSERT INTO usuarios(username, senha) VALUES (?, ?)",
                username,
                senha);

        jdbc.update(
                "INSERT INTO authorities(username, authority) VALUES (?, ?)",
                username,
                "ROLE_USER");

        jdbc.update("""
                INSERT INTO login_attempts(
                    username, failed_attempts, locked, updated_at
                ) VALUES (?, 0, FALSE, CURRENT_TIMESTAMP)
                """,
                username);
    }

    public void trocarSenha(String username, String novaSenha) {
        jdbc.update(
                "UPDATE usuarios SET senha = ? WHERE username = ?",
                novaSenha,
                username);
    }

    public List<UsuarioResponse> listar() {
        return jdbc.query("""
                SELECT
                    u.username,
                    COALESCE(l.locked, FALSE) AS locked
                FROM usuarios u
                LEFT JOIN login_attempts l ON l.username = u.username
                ORDER BY u.username
                """,
                (rs, rowNum) -> new UsuarioResponse(
                        rs.getString("username"),
                        rs.getBoolean("locked")));
    }

    public boolean estaBloqueado(String username) {
        try {
            Boolean locked = jdbc.queryForObject(
                    "SELECT locked FROM login_attempts WHERE username = ?",
                    Boolean.class,
                    username);
            return Boolean.TRUE.equals(locked);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public void registrarFalha(String username) {

        Integer tentativas;

        try {
            tentativas = jdbc.queryForObject(
                    "SELECT failed_attempts FROM login_attempts WHERE username = ?",
                    Integer.class,
                    username);
        } catch (EmptyResultDataAccessException e) {
            tentativas = null;
        }

        if (tentativas == null) {
            jdbc.update("""
                    INSERT INTO login_attempts(
                        username,
                        failed_attempts,
                        locked,
                        last_failed_at,
                        locked_at,
                        updated_at
                    ) VALUES (?, 1, FALSE, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP)
                    """,
                    username);
            return;
        }

        int novasTentativas = tentativas + 1;
        boolean bloquear = novasTentativas > 1;

        jdbc.update("""
                UPDATE login_attempts
                SET failed_attempts = ?,
                    locked = ?,
                    last_failed_at = CURRENT_TIMESTAMP,
                    locked_at = CASE WHEN ? = TRUE THEN CURRENT_TIMESTAMP ELSE locked_at END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE username = ?
                """,
                novasTentativas,
                bloquear,
                bloquear,
                username);
    }

    public void resetarTentativas(String username) {

        int atualizados = jdbc.update("""
                UPDATE login_attempts
                SET failed_attempts = 0,
                    locked = FALSE,
                    last_failed_at = NULL,
                    locked_at = NULL,
                    updated_at = CURRENT_TIMESTAMP
                WHERE username = ?
                """,
                username);

        if (atualizados == 0) {
            jdbc.update("""
                    INSERT INTO login_attempts(
                        username, failed_attempts, locked, updated_at
                    ) VALUES (?, 0, FALSE, CURRENT_TIMESTAMP)
                    """,
                    username);
        }
    }

    public void desbloquear(String username) {
        resetarTentativas(username);
    }

    public int contarBloqueados() {
        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM login_attempts l
                INNER JOIN usuarios u ON u.username = l.username
                WHERE l.locked = TRUE
                """,
                Integer.class);

        return total == null ? 0 : total;
    }
}
