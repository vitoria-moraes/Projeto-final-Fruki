package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dtos.HistoricoCompraDTO;
import org.example.projetofinalfruki.application.dtos.RecomendacaoResponseDTO;
import org.example.projetofinalfruki.domain.service.MockPedeFrukiService;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Fluxo local independente do banco para demonstrar WhatsApp, recomendações e catálogo. */
@RestController
@RequestMapping("/mock/demo")
public class DemoController {

    private static final String DEMO_CNPJ = "11222333000181";
    private final MockPedeFrukiService mockService;
    private final ClienteJpaRepository clienteRepository;
    private final JwtService jwtService;
    private final String baseUrl;

    public DemoController(MockPedeFrukiService mockService, ClienteJpaRepository clienteRepository,
                          JwtService jwtService,
                          @Value("${app.catalogo.base-url:http://localhost:8080}") String baseUrl) {
        this.mockService = mockService;
        this.clienteRepository = clienteRepository;
        this.jwtService = jwtService;
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    @GetMapping
    public DemoPayload carregarDemo() {
        ClienteEntity cliente = new ClienteEntity(9001L, "Pub Hundte", DEMO_CNPJ, "Lajeado", "RS", null);
        try {
            cliente = clienteRepository.findByCnpj(DEMO_CNPJ).orElse(cliente);
        } catch (DataAccessException ignored) {
            // A demonstração continua funcionando sem conexão ao Supabase.
        }
        RecomendacaoResponseDTO recomendacoes = mockService.recomendacoes(
                cliente.getCodCliente(), cliente, List.of(), 5, null);
        List<HistoricoCompraDTO> compras = mockService.historico(10);
        String token = jwtService.gerarTokenCatalogo("9001", cliente.getNomeFantasia(),
                "Lajeado - RS", "TABELA_RS", cliente.getCnpj());
        String catalogo = baseUrl + "/catalogo?token=" + token;
        return new DemoPayload(cliente.getCodCliente(), cliente.getCnpj(), cliente.getNomeFantasia(),
                "Lajeado - RS", recomendacoes, compras, catalogo);
    }

    public record DemoPayload(Long clienteId, String cnpj, String nome, String regiao,
                              RecomendacaoResponseDTO recomendacoes,
                              List<HistoricoCompraDTO> compras, String catalogoUrl) { }
}
