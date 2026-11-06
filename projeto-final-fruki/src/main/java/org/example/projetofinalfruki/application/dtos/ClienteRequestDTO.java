package org.example.projetofinalfruki.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada para cadastro de um novo lojista. */
public record ClienteRequestDTO(

        @NotBlank(message = "codCliente é obrigatório")
        @Size(max = 20, message = "codCliente deve ter no máximo 20 caracteres")
        String codCliente,

        @NotBlank(message = "CNPJ é obrigatório")
        @Size(max = 20, message = "CNPJ inválido")
        String cnpj,

        @NotBlank(message = "nomeFantasia é obrigatório")
        @Size(max = 120, message = "nomeFantasia deve ter no máximo 120 caracteres")
        String nomeFantasia,

        @NotBlank(message = "cidade é obrigatória")
        @Size(max = 80, message = "cidade deve ter no máximo 80 caracteres")
        String cidade,

        @NotBlank(message = "UF é obrigatória")
        @Pattern(regexp = "(?i)[a-z]{2}", message = "UF deve ter 2 letras (ex.: RS)")
        String uf,

        @Size(max = 20, message = "codHierarquia deve ter no máximo 20 caracteres")
        String codHierarquia
) {
}
