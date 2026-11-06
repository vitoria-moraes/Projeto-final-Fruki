package org.example.projetofinalfruki.application.controller;

import jakarta.validation.Valid;
import org.example.projetofinalfruki.application.dtos.CompraRequestDTO;
import org.example.projetofinalfruki.application.dtos.CompraResponseDTO;
import org.example.projetofinalfruki.domain.service.CompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registro de compras item a item (Requisito 6). */
@RestController
@RequestMapping("/api/v1/compras")
public class CompraController {

    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CompraResponseDTO> registrar(@Valid @RequestBody CompraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }
}
