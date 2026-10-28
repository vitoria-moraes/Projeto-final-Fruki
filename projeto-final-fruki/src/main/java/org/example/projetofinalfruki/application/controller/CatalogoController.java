package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.TabelaPrecoJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalogo")
public class CatalogoController {

    private final TabelaPrecoJpaRepository tabelaPrecoRepository;

    public CatalogoController(TabelaPrecoJpaRepository tabelaPrecoRepository) {
        this.tabelaPrecoRepository = tabelaPrecoRepository;
    }

    @GetMapping
    public ResponseEntity<List<TabelaPrecoEntity>> listarProdutos() {
        return ResponseEntity.ok(tabelaPrecoRepository.findAll());
    }
}