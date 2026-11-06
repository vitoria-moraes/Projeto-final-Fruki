package org.example.projetofinalfruki.application.controller;

import jakarta.validation.Valid;
import org.example.projetofinalfruki.application.dtos.ClienteRequestDTO;
import org.example.projetofinalfruki.application.dtos.ClienteResponseDTO;
import org.example.projetofinalfruki.domain.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Entrada e consulta de lojistas (Requisito 6). */
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO criado = service.criar(dto);
        return ResponseEntity
                .created(URI.create("/api/v1/clientes/" + criado.cnpj()))
                .body(criado);
    }

    @GetMapping("/{cnpj}")
    public ClienteResponseDTO buscar(@PathVariable String cnpj) {
        return service.buscarPorCnpj(cnpj);
    }
}
