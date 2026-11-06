package org.example.projetofinalfruki;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração real: exige Supabase e JWT_SECRET no ambiente.
 * Sem essas variáveis, é ignorado (não quebra o build de quem só quer rodar os testes unitários).
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "SUPABASE_JDBC_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "JWT_SECRET", matches = ".{32,}")
class WhatsAppWebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveProcessarMensagemDoWebhook() throws Exception {
        String jsonPayload = """
                {
                  "event": "messages.upsert",
                  "data": {
                    "key": {"remoteJid": "5551988887777@s.whatsapp.net", "fromMe": false},
                    "pushName": "Cliente Teste",
                    "message": {"conversation": "oi"}
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook da Evolution API processado com sucesso!"));
    }
}
