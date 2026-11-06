package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.CompraRequestDTO;
import org.example.projetofinalfruki.application.dtos.CompraResponseDTO;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private ClienteJpaRepository clienteRepository;
    @Mock
    private PedidoJpaRepository pedidoRepository;
    @Mock
    private CatalogoService catalogo;

    private CompraService service;
    private ClienteEntity cliente;

    @BeforeEach
    void setUp() {
        service = new CompraService(clienteRepository, pedidoRepository, catalogo);
        cliente = new ClienteEntity();
        cliente.setCodCliente(41L);
        cliente.setCnpj("11222333000181");
        cliente.setNomeFantasia("Mercado Central");
    }

    @Test
    @DisplayName("registra compra e calcula o total pelo preço da tabela")
    void registraCompra() {
        when(clienteRepository.findByCnpj("11222333000181")).thenReturn(Optional.of(cliente));
        when(catalogo.buscarProduto("00001")).thenReturn(Optional.of(produto()));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> {
            PedidoEntity p = inv.getArgument(0);
            p.setId(7L);
            return p;
        });

        CompraResponseDTO resposta = service.registrar(new CompraRequestDTO(
                "11.222.333/0001-81", "00001", 3, LocalDate.now(), null));

        assertEquals(7L, resposta.id());
        assertEquals(new BigDecimal("30.00"), resposta.valorTotal());
    }

    @Test
    @DisplayName("404 quando o cliente não existe")
    void clienteInexistente() {
        when(clienteRepository.findByCnpj("11222333000181")).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> service.registrar(
                new CompraRequestDTO("11222333000181", "00001", 1, LocalDate.now(), null)));
    }

    @Test
    @DisplayName("400 quando o produto não existe")
    void produtoInexistente() {
        when(clienteRepository.findByCnpj("11222333000181")).thenReturn(Optional.of(cliente));
        when(catalogo.buscarProduto("99999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.registrar(
                new CompraRequestDTO("11222333000181", "99999", 1, LocalDate.now(), null)));
    }

    private TabelaPrecoEntity produto() {
        TabelaPrecoEntity p = new TabelaPrecoEntity();
        p.setId("1");
        p.setCodProduto("00001");
        p.setNomeProduto("Fruki Guaraná 2L");
        p.setPrecoBase(new BigDecimal("10.00"));
        return p;
    }
}
