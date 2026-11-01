package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.example.projetofinalfruki.domain.service.FeriadoService;
import org.example.projetofinalfruki.domain.service.MotorSazonalidadeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contexto")
public class ContextoController {

    private final MotorSazonalidadeService motorSazonalidadeService;
    private final FeriadoService feriadoService;

    public ContextoController(MotorSazonalidadeService motorSazonalidadeService, FeriadoService feriadoService) {
        this.motorSazonalidadeService = motorSazonalidadeService;
        this.feriadoService = feriadoService;
    }

    @GetMapping
    public String getContexto(@RequestParam(defaultValue = "Porto Alegre") String cidade) {
        return motorSazonalidadeService.gerarRecomendacaoParaCidade(cidade);
    }

    // Endpoint do Calendário: Retorna 2026 + 2027 (ou apenas o ano informado)
    @GetMapping("/feriados")
    public List<FeriadoDTO> getFeriados(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cidade) {
        return feriadoService.buscarFeriadosCompletos(ano, cidade);
    }
}