package org.example.projetofinalfruki.application.controller;

import jakarta.validation.Valid;
import org.example.projetofinalfruki.application.dtos.PedidoRequestDTO;
import org.example.projetofinalfruki.application.dtos.PedidoResponseDTO;
import org.example.projetofinalfruki.domain.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Pedido real: calcula o total e persiste em fato_vendas (Requisito 5). */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody @Valid PedidoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }
}
