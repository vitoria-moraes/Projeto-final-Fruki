package org.example.projetofinalfruki.application.dtos;

/** Resposta do cadastro/consulta de cliente. */
public record ClienteResponseDTO(
        Long codCliente,
        String cnpj,
        String nomeFantasia,
        String cidade,
        String uf,
        Long codHierarquia
) {
}
