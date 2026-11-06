package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dtos.IndicadoresResponseDTO;
import org.example.projetofinalfruki.domain.service.IndicadoresService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Indicadores da solução (Requisito 4). */
@RestController
@RequestMapping("/api/v1/indicadores")
public class IndicadoresController {

    private final IndicadoresService service;

    public IndicadoresController(IndicadoresService service) {
        this.service = service;
    }

    @GetMapping
    public IndicadoresResponseDTO indicadores() {
        return service.calcular();
    }
}
