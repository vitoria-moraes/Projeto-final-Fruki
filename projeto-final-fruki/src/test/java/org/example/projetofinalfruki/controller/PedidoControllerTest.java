package org.example.projetofinalfruki.controller;

import org.example.projetofinalfruki.application.controller.PedidoController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false) // <--- Adiciona isso aqui para desligar o security no teste
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar CREATED (201) ao enviar um pedido válido para a API")
    public void deveRetornarCriadoAoEnviarPedidoValido() throws Exception {
        String jsonPayload = """
            {
                "cnpjCliente": "12345678000199",
                "itens": [
                    {
                        "codigoProduto": "FRK-01",
                        "quantidade": 5
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());
    }
}