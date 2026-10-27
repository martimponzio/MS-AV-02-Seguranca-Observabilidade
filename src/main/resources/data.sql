DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE IF NOT EXISTS login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS authorities (
    username VARCHAR(100) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_usuarios
        FOREIGN KEY(username) REFERENCES usuarios(username)
);

CREATE UNIQUE INDEX ix_auth_username
    ON authorities (username, authority);

INSERT INTO usuarios(username, senha)
VALUES ('admin', 'admin123');

INSERT INTO usuarios(username, senha)
VALUES ('manutencao', 'manut123');

INSERT INTO usuarios(username, senha)
VALUES ('user', 'user123');

INSERT INTO authorities(username, authority)
VALUES ('admin', 'ROLE_ADMIN');

INSERT INTO authorities(username, authority)
VALUES ('admin', 'ROLE_MANUTENCAO');

INSERT INTO authorities(username, authority)
VALUES ('manutencao', 'ROLE_MANUTENCAO');

INSERT INTO authorities(username, authority)
VALUES ('user', 'ROLE_USER');

INSERT INTO login_attempts(username, failed_attempts, locked)
VALUES ('admin', 0, FALSE);

INSERT INTO login_attempts(username, failed_attempts, locked)
VALUES ('manutencao', 0, FALSE);

INSERT INTO login_attempts(username, failed_attempts, locked)
VALUES ('user', 0, FALSE);
