package org.example.projetofinalfruki.application.dtos;

/** Resposta do cadastro/consulta de cliente. */
public record ClienteResponseDTO(
        String codCliente,
        String cnpj,
        String nomeFantasia,
        String cidade,
        String uf,
        String codHierarquia
) {
}
