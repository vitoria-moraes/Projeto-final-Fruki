package org.example.projetofinalfruki.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Resposta do registro de compra. */
public record CompraResponseDTO(
        Long id,
        String cnpj,
        String nomeFantasia,
        String codItem,
        int quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal,
        LocalDate dataCompra
) {
}
