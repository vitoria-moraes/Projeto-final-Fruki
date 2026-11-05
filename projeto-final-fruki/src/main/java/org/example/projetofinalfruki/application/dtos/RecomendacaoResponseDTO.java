package org.example.projetofinalfruki.application.dtos;

import java.util.List;

/**
 * Contrato principal de resposta do Motor de Recomendações.
 */
public record RecomendacaoResponseDTO(
        String idCliente,
        String perfilClassificado, // Ex: "PEQUENO_VAREJO"
        String contextoSazonal,    // Ex: "🔥 Dia quente em Porto Alegre..." gerado pelo MotorSazonalidade
        List<RecomendacaoItemDTO> sugestoes
) {}