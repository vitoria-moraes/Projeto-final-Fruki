package org.example.projetofinalfruki.application.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.example.projetofinalfruki.application.dtos.RecomendacaoResponseDTO;
import org.example.projetofinalfruki.domain.service.RecomendacaoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consulta da recomendação. O controller apenas delega para o service. */
@RestController
@RequestMapping("/api/v1/recomendacoes")
@Validated
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @GetMapping("/{idCliente}")
    public RecomendacaoResponseDTO listarRecomendacoesPorCliente(
            @PathVariable @Positive(message = "idCliente deve ser positivo") Long idCliente,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "limite mínimo é 1")
            @Max(value = 50, message = "limite máximo é 50") int limite) {
        return recomendacaoService.recomendar(idCliente, limite);
    }
}
