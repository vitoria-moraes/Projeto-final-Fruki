package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.domain.model.RecomendacaoB2B;
import org.example.projetofinalfruki.infrastructure.db.supabase.databricks.RecomendacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recomendacoes")
public class RecomendacaoController {

    private final RecomendacaoRepository recomendacaoRepository;

    // Injeção por construtor (em vez de @Autowired em campo): facilita teste e deixa a dependência explícita
    public RecomendacaoController(RecomendacaoRepository recomendacaoRepository) {
        this.recomendacaoRepository = recomendacaoRepository;
    }

    @GetMapping("/{idCliente}")
    public List<RecomendacaoB2B> listarRecomendacoesPorCliente(@PathVariable Long idCliente) {
        return recomendacaoRepository.findByIdClienteOrderByScoreRecomendacaoDesc(idCliente);
    }

    /** Databricks fora do ar não vira erro 500 genérico: responde 503 com mensagem clara. */
    @ExceptionHandler(RecomendacaoRepository.DatabricksIndisponivelException.class)
    public ProblemDetail databricksIndisponivel(RecomendacaoRepository.DatabricksIndisponivelException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        pd.setTitle("Camada analítica indisponível");
        return pd;
    }
}