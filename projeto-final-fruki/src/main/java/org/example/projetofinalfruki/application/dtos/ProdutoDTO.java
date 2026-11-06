package org.example.projetofinalfruki.application.dtos;

import java.math.BigDecimal;

/** Produto da tabela de preços, para o catálogo do front. */
public record ProdutoDTO(
        String codItem,
        String nome,
        String categoria,
        BigDecimal preco
) {
}
