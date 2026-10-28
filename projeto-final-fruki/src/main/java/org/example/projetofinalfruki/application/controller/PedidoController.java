package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.domain.model.Pedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedidoRequest) {
        String codigoGerado = "FRK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Pedido novoPedido = new Pedido(
                codigoGerado,
                pedidoRequest.getCodCliente(),
                pedidoRequest.getProduto(),
                pedidoRequest.getQuantidade(),
                pedidoRequest.getValorTotal() != null ? pedidoRequest.getValorTotal() : BigDecimal.ZERO
        );

        return ResponseEntity.ok(novoPedido);
    }
}