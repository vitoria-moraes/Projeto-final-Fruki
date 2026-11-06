package org.example.projetofinalfruki.application.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Item recomendado, já enriquecido com nome, categoria, preço e explicação.
 * "detalhe" mostra a contribuição de cada fator (0..1) e a penalidade aplicada.
 */
public record ItemRecomendadoDTO(
        int posicao,
        String codItem,
        String nome,
        String categoria,
        BigDecimal preco,
        double score,
        List<String> motivos,
        Map<String, Double> detalhe
) {
}
