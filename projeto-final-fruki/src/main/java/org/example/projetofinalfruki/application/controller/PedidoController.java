package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dtos.PedidoRequestDTO;
import org.example.projetofinalfruki.application.dtos.PedidoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody @Valid PedidoRequestDTO request) {
        String codigoGerado = "FRK-" + System.currentTimeMillis();

        PedidoResponseDTO response = new PedidoResponseDTO(
                codigoGerado,
                "CRIADO",
                new BigDecimal("150.00"),
                LocalDateTime.now(),
                "Pedido recebido e registado com sucesso!"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}