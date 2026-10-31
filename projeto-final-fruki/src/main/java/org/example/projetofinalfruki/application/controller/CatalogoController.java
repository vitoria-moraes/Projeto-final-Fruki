package org.example.projetofinalfruki.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoController {

    @GetMapping("/catalogo")
    public String exibirCatalogo() {
        // Correção: Retornar "catalogo" sem o sufixo ".html"
        return "catalogo";
    }
}