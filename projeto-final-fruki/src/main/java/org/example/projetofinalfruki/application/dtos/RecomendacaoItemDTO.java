package org.example.projetofinalfruki.application.dtos;

import java.math.BigDecimal;

/**
 * Representa um item individual recomendado ao lojista.
 */
public record RecomendacaoItemDTO(
        String codigoProduto,
        String nomeProduto,
        BigDecimal precoRecomendado,
        String motivoRecomendacao, // Ex: "Cross-sell com Água da Pedra" ou "Giro rápido na sua região"
        BigDecimal score // Útil para a Pessoa B fazer debug ou ordenar no frontend
) {}