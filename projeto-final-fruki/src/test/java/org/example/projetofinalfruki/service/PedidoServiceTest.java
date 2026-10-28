package org.example.projetofinalfruki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.projetofinalfruki.application.dtos.PedidoRequestDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Test
    @DisplayName("Deve validar a estrutura do pedido com sucesso")
    public void testValidarEstruturaPedido() {
        // Cenário
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setCnpjCliente("12.345.678/0001-99");

        PedidoRequestDTO.ItemPedidoDTO item = new PedidoRequestDTO.ItemPedidoDTO();
        item.setCodigoProduto("FRUKI-COLATINA-2L");
        item.setQuantidade(10);

        request.setItens(List.of(item));

        // Verificação
        assertNotNull(request.getCnpjCliente());
        assertNotNull(request.getItens());
    }
}