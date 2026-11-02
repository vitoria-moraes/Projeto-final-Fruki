package org.example.projetofinalfruki;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WhatsAppWebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveSimularMensagemWebhookCatalogoComSucesso() throws Exception {
        String jsonPayload = "{"
                + "\"data\": {"
                + "\"remoteJid\": \"5551988887777@s.whatsapp.net\","
                + "\"pushName\": \"Cliente Teste\","
                + "\"message\": {"
                + "\"conversation\": \"Quero ver o catalogo\""
                + "}"
                + "}"
                + "}";

        mockMvc.perform(post("/api/v1/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook da Evolution API processado com sucesso!"));
    }

    @Test
    void deveValidarTempoDeRespostaDoWebhook() throws Exception {
        String jsonPayload = "{"
                + "\"data\": {"
                + "\"remoteJid\": \"5551977776666@s.whatsapp.net\","
                + "\"pushName\": \"Lojista Performance\","
                + "\"message\": {"
                + "\"conversation\": \"catalogo\""
                + "}"
                + "}"
                + "}";

        long inicio = System.currentTimeMillis();

        mockMvc.perform(post("/api/v1/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        long duracao = System.currentTimeMillis() - inicio;

        // Garante que o bot responde com alta performance (abaixo de 1 segundo)
        org.junit.jupiter.api.Assertions.assertTrue(duracao < 1000,
                "O tempo de resposta do webhook excedeu o limite aceitável: " + duracao + "ms");
    }
}