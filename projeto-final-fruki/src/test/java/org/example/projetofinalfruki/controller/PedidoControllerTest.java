package org.example.projetofinalfruki.controller;

import org.example.projetofinalfruki.application.controller.PedidoController;
import org.example.projetofinalfruki.application.dtos.PedidoResponseDTO;
import org.example.projetofinalfruki.domain.service.PedidoService;
import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService service;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("retorna 201 com o total calculado pelo service")
    void deveRetornarCriado() throws Exception {
        when(service.criar(any())).thenReturn(new PedidoResponseDTO(
                "FRK-1", "CRIADO", new BigDecimal("14.98"), LocalDateTime.now(), "ok"));

        String json = """
                {"cnpjCliente":"11222333000181","itens":[{"codigoProduto":"00001","quantidade":2}]}
                """;

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(14.98));
    }
}
