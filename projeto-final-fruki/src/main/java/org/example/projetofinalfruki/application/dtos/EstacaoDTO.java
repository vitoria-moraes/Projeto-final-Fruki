package org.example.projetofinalfruki.application.dtos;

import org.example.projetofinalfruki.domain.model.Estacao;

import java.time.LocalDate;

/** Resposta do endpoint de estação do ano. */
public record EstacaoDTO(
        LocalDate data,
        Estacao estacao,
        String descricao
) {
}
