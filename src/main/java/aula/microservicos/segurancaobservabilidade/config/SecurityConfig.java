package aula.microservicos.segurancaobservabilidade.config;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource datasource) {

        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(datasource);

        manager.setUsersByUsernameQuery("""
                SELECT
                    u.username,
                    CONCAT('{noop}', u.senha) AS password,
                    CASE
                        WHEN COALESCE(l.locked, FALSE) = TRUE THEN FALSE
                        ELSE TRUE
                    END AS enabled
                FROM usuarios u
                LEFT JOIN login_attempts l ON l.username = u.username
                WHERE u.username = ?
                """);

        manager.setAuthoritiesByUsernameQuery(
                "SELECT username, authority FROM authorities WHERE username = ?");

        return manager;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                        .anyRequest().authenticated())
                .httpBasic(withDefaults())
                .formLogin(form -> form.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
