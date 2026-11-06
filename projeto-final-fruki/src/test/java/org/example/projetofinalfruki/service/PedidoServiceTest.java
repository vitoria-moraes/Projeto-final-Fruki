package org.example.projetofinalfruki.service;

import org.example.projetofinalfruki.application.dtos.PedidoRequestDTO;
import org.example.projetofinalfruki.application.dtos.PedidoResponseDTO;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.service.CatalogoService;
import org.example.projetofinalfruki.domain.service.PedidoService;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.PedidoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.PedidoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private ClienteJpaRepository clienteRepository;
    @Mock
    private PedidoJpaRepository pedidoRepository;
    @Mock
    private CatalogoService catalogo;

    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(clienteRepository, pedidoRepository, catalogo);
    }

    @Test
    @DisplayName("calcula o total real do pedido e persiste")
    void calculaTotalReal() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setCodCliente(41L);
        cliente.setCnpj("11222333000181");
        when(clienteRepository.findByCnpj("11222333000181")).thenReturn(Optional.of(cliente));
        when(catalogo.buscarProduto("00001")).thenReturn(Optional.of(produto()));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> {
            PedidoEntity p = inv.getArgument(0);
            p.setId(5L);
            return p;
        });

        PedidoResponseDTO resposta = service.criar(pedido("00001", 2));

        assertEquals(new BigDecimal("14.98"), resposta.getValorTotal());
        assertEquals("CRIADO", resposta.getStatus());
        assertEquals("CRIADO", resposta.getStatus());
    }

    @Test
    @DisplayName("404 quando o CNPJ não existe")
    void clienteInexistente() {
        when(clienteRepository.findByCnpj("11222333000181")).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> service.criar(pedido("00001", 1)));
    }

    private PedidoRequestDTO pedido(String codigo, int quantidade) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setCnpjCliente("11.222.333/0001-81");
        PedidoRequestDTO.ItemPedidoDTO item = new PedidoRequestDTO.ItemPedidoDTO();
        item.setCodigoProduto(codigo);
        item.setQuantidade(quantidade);
        dto.setItens(List.of(item));
        return dto;
    }

    private TabelaPrecoEntity produto() {
        TabelaPrecoEntity p = new TabelaPrecoEntity();
        p.setId("1");
        p.setCodProduto("00001");
        p.setNomeProduto("Fruki Guaraná 2L");
        p.setPrecoBase(new BigDecimal("7.49"));
        return p;
    }
}
