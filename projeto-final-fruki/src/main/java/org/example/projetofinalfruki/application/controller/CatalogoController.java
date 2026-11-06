package org.example.projetofinalfruki.application.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoController {

    /** O CatalogoInterceptor já validou o JWT e colocou os dados do lojista nos atributos da requisição. */
    @GetMapping("/catalogo")
    public String exibirCatalogo(Model model, HttpServletRequest request) {
        model.addAttribute("nomeCliente", request.getAttribute("nomeCliente"));
        model.addAttribute("regiaoCliente", request.getAttribute("regiaoCliente"));
        model.addAttribute("tabelaPreco", request.getAttribute("tabelaPrecoCliente"));
        model.addAttribute("clienteId", request.getAttribute("clienteId"));
        model.addAttribute("cnpjCliente", request.getAttribute("cnpjCliente"));
        return "catalogo-react";
    }
}
