package org.example.projetofinalfruki.application.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.projetofinalfruki.application.dtos.ProdutoDTO;
import org.example.projetofinalfruki.domain.exception.ProdutoNaoEncontradoException;
import org.example.projetofinalfruki.domain.service.CatalogoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogo de produtos vindo da tabela de preços, para o front. */
@RestController
@RequestMapping("/api/v1/produtos")
@Validated
public class ProdutoController {

    private final CatalogoService catalogo;

    public ProdutoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    public List<ProdutoDTO> listar(
            @RequestParam(defaultValue = "50")
            @Min(value = 1, message = "limite mínimo é 1")
            @Max(value = 200, message = "limite máximo é 200") int limite) {
        return catalogo.listarProdutos(limite).stream()
                .map(catalogo::paraDTO)
                .toList();
    }

    @GetMapping("/{codItem}")
    public ProdutoDTO buscar(@PathVariable String codItem) {
        return catalogo.buscarProduto(codItem)
                .map(catalogo::paraDTO)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(codItem));
    }
}
