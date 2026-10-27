package aula.microservicos.segurancaobservabilidade;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaObservabilidadeApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void loginValidoRetorna200() throws Exception {
        mvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"user","senha":"user123"}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void endpointDeListagemExigeRoleManutencao() throws Exception {
        mvc.perform(get("/")
                .with(httpBasic("manutencao", "manut123")))
                .andExpect(status().isOk());

        mvc.perform(get("/")
                .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointUnlockExigeRoleAdmin() throws Exception {
        mvc.perform(put("/unlock/user")
                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        mvc.perform(put("/unlock/user")
                .with(httpBasic("manutencao", "manut123")))
                .andExpect(status().isForbidden());
    }
}
