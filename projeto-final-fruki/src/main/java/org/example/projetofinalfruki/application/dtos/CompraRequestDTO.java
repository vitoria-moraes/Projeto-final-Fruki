package org.example.projetofinalfruki.application.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Entrada para registrar uma compra (item vendido a um lojista). */
public record CompraRequestDTO(

        @NotBlank(message = "CNPJ é obrigatório")
        String cnpj,

        @NotBlank(message = "codItem é obrigatório")
        @Pattern(regexp = "\\d{1,5}", message = "codItem deve ter de 1 a 5 dígitos")
        String codItem,

        @NotNull(message = "quantidade é obrigatória")
        @Min(value = 1, message = "quantidade deve ser maior que zero")
        Integer quantidade,

        @NotNull(message = "dataCompra é obrigatória")
        @PastOrPresent(message = "dataCompra não pode ser futura")
        LocalDate dataCompra,

        @DecimalMin(value = "0.01", message = "valorUnitario deve ser maior que zero")
        BigDecimal valorUnitario
) {
}
