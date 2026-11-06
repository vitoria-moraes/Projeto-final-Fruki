package org.example.projetofinalfruki.application.dtos;

import java.util.List;

/** Resposta estável da API de recomendação. */
public record RecomendacaoResponseDTO(
        Long idCliente,
        String nomeCliente,
        String cidade,
        String uf,
        String perfil,
        ContextoDTO contexto,
        List<ItemRecomendadoDTO> itens
) {
}
