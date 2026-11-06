package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.EstacaoDTO;
import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.example.projetofinalfruki.domain.service.ContextoService;
import org.example.projetofinalfruki.domain.service.MotorSazonalidadeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Contexto do Sul: clima, estação do ano e feriados/eventos.
 * O mesmo contexto alimenta o recomendador.
 */
@RestController
@RequestMapping("/api/v1/contexto")
public class ContextoController {

    private final ContextoService contextoService;
    private final MotorSazonalidadeService motor;

    public ContextoController(ContextoService contextoService, MotorSazonalidadeService motor) {
        this.contextoService = contextoService;
        this.motor = motor;
    }

    @GetMapping
    public ContextoDTO getContexto(
            @RequestParam(defaultValue = "Porto Alegre") String cidade,
            @RequestParam(required = false) String uf) {
        return contextoService.obterContexto(cidade, uf);
    }

    @GetMapping("/mensagem")
    public String mensagem(@RequestParam(defaultValue = "Porto Alegre") String cidade) {
        return motor.gerarRecomendacaoParaCidade(cidade);
    }

    @GetMapping("/estacao")
    public EstacaoDTO estacao(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return contextoService.estacao(data);
    }

    @GetMapping("/feriados")
    public List<FeriadoDTO> feriados(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cidade) {
        return contextoService.feriados(ano, cidade);
    }
}
