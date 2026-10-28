package org.example.projetofinalfruki.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @GetMapping
    public String exibirCatalogo(Model model) {
        // Lista mockada com produtos da Fruki para o protótipo B2B
        List<ProdutoMock> produtos = List.of(
                new ProdutoMock("FRK-01", "Guaraná Fruki 2L", "Refrigerante de Guaraná Tradicional", "R$ 6,50"),
                new ProdutoMock("FRK-02", "Água da Pedra 500ml", "Água Mineral sem Gás", "R$ 2,00"),
                new ProdutoMock("FRK-03", "Fruki Cola 350ml", "Refrigerante de Cola Lata", "R$ 3,50"),
                new ProdutoMock("FRK-04", "Fruki Laranja 2L", "Refrigerante sabor Laranja", "R$ 6,00"),
                new ProdutoMock("FRK-05", "Água da Pedra com Gás 500ml", "Água Mineral com Gás", "R$ 2,20"),
                new ProdutoMock("FRK-06", "H2OH! Limão 500ml", "Bebida levemente gaseificada", "R$ 4,50")
        );

        model.addAttribute("produtos", produtos);
        model.addAttribute("tituloPage", "Catálogo B2B - Fruki Bebidas");

        return "catalogo.html"; // Vai procurar o arquivo catalogo.html dentro de resources/templates/
    }

    // Record para simular os dados no protótipo visual
    public record ProdutoMock(String codigo, String nome, String descricao, String preco) {}
}