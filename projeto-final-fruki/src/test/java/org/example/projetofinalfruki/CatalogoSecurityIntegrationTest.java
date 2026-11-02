package org.example.projetofinalfruki;

import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogoSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void deveNegarAcessoSemTokenNaRotaCatalogo() throws Exception {
        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoComTokenValido() throws Exception {
        // Gera um token válido de teste usando o JwtService existente
        String token = jwtService.gerarTokenCatalogo(
                "5551999998888@s.whatsapp.net",
                "Lojista Integrado",
                "Porto Alegre - RS",
                "TABELA_VIP_RS"
        );

        mockMvc.perform(get("/catalogo")
                        .param("token", token))
                .andExpect(status().isOk());
    }
}