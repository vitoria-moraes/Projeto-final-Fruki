package org.example.projetofinalfruki.controller;

import org.example.projetofinalfruki.application.controller.DemoController;
import org.example.projetofinalfruki.config.SecurityConfig;
import org.example.projetofinalfruki.domain.service.MockPedeFrukiService;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

class DemoControllerTest {

    @Test
    void forneceClienteRecomendacoesEUrlDeCatalogoSemConsultarBanco() {
        JwtService jwtService = new JwtService("fruki-demo-secret-key-with-at-least-32-bytes", 7200);
        ClienteJpaRepository clienteRepository = mock(ClienteJpaRepository.class);
        when(clienteRepository.findByCnpj("11222333000181")).thenReturn(Optional.empty());
        DemoController controller = new DemoController(new MockPedeFrukiService(), clienteRepository, jwtService, "http://localhost:8080/");

        DemoController.DemoPayload demo = controller.carregarDemo();

        assertEquals("Pub Hundte", demo.nome());
        assertEquals("11222333000181", demo.cnpj());
        assertEquals(5, demo.recomendacoes().itens().size());
        assertTrue(demo.recomendacoes().itens().stream().allMatch(item -> !item.motivos().isEmpty()));
        assertTrue(demo.catalogoUrl().startsWith("http://localhost:8080/catalogo?token="));
        String token = demo.catalogoUrl().substring(demo.catalogoUrl().indexOf("token=") + 6);
        assertTrue(jwtService.validarToken(token));
        assertEquals("Pub Hundte", jwtService.extrairClaims(token).get("nome", String.class));
    }

    @Test
    void usaNomeDoClienteEncontradoQuandoBancoEstaDisponivel() {
        JwtService jwtService = new JwtService("fruki-demo-secret-key-with-at-least-32-bytes", 7200);
        ClienteJpaRepository clienteRepository = mock(ClienteJpaRepository.class);
        when(clienteRepository.findByCnpj("11222333000181")).thenReturn(Optional.of(
                new org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity(
                        9001L, "Loja Teste", "11222333000181", "Lajeado", "RS", null)));
        DemoController controller = new DemoController(new MockPedeFrukiService(), clienteRepository, jwtService, "http://localhost:8080");

        assertEquals("Loja Teste", controller.carregarDemo().nome());
    }

    @Test
    void permiteAOrigemDoServidorEstáticoLocalDoIntelliJ() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mock/demo");
        var cors = new SecurityConfig().corsConfigurationSource().getCorsConfiguration(request);

        assertTrue(cors != null);
        assertEquals("http://localhost:63342", cors.checkOrigin("http://localhost:63342"));
    }
}
