package org.example.projetofinalfruki.application.dtos;

/** Indicadores simples calculados a partir da base. */
public record IndicadoresResponseDTO(
        long clientesTotal,
        long clientesComRecomendacao,
        double coberturaPercent,
        long itensDistintos,
        long totalRecomendacoes,
        double taxaRecomendacaoPercent,
        double mediaItensPorCliente
) {
}
