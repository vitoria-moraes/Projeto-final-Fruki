package org.example.projetofinalfruki.controller;

import org.example.projetofinalfruki.application.controller.RecomendacaoController;
import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.ItemRecomendadoDTO;
import org.example.projetofinalfruki.application.dtos.RecomendacaoResponseDTO;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.exception.DatabricksIndisponivelException;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.example.projetofinalfruki.domain.service.RecomendacaoService;
import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecomendacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecomendacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecomendacaoService service;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("200 com o contrato de recomendação")
    void retorna200() throws Exception {
        when(service.recomendar(41L, 10)).thenReturn(resposta());

        mockMvc.perform(get("/api/v1/recomendacoes/41"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(41))
                .andExpect(jsonPath("$.itens[0].posicao").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Fruki Guaraná 2L"))
                .andExpect(jsonPath("$.itens[0].score").value(92.0));
    }

    @Test
    @DisplayName("404 quando o cliente não existe")
    void retorna404() throws Exception {
        when(service.recomendar(99L, 10)).thenThrow(new ClienteNaoEncontradoException("99"));

        mockMvc.perform(get("/api/v1/recomendacoes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
    }

    @Test
    @DisplayName("503 quando o Databricks está fora")
    void retorna503() throws Exception {
        when(service.recomendar(41L, 10))
                .thenThrow(new DatabricksIndisponivelException("fora", new RuntimeException()));

        mockMvc.perform(get("/api/v1/recomendacoes/41"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Camada analítica indisponível"));
    }

    @Test
    @DisplayName("400 quando o id não é numérico")
    void retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/recomendacoes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetro inválido"));
    }

    private RecomendacaoResponseDTO resposta() {
        ContextoDTO contexto = new ContextoDTO("Porto Alegre", "RS", LocalDate.now(),
                Estacao.VERAO, "Verão", 31.0, "TESTE", false, "teste", List.of());
        ItemRecomendadoDTO item = new ItemRecomendadoDTO(1, "00001", "Fruki Guaraná 2L",
                "Refrigerantes", new BigDecimal("7.49"), 92.0, List.of("teste"),
                Map.of("rfm", 0.9));
        return new RecomendacaoResponseDTO(41L, "Mercado Central", "Porto Alegre", "RS",
                "Mercado Central", contexto, List.of(item));
    }
}
