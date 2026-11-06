package org.example.projetofinalfruki.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoCompraDTO(
        Long id,
        LocalDateTime dataPedido,
        BigDecimal valorTotal,
        String status,
        String resumoItens,
        boolean demonstracao) {
    public HistoricoCompraDTO(Long id, LocalDateTime dataPedido, BigDecimal valorTotal,
                              String status, String resumoItens) {
        this(id, dataPedido, valorTotal, status, resumoItens, false);
    }
}
