package org.example.projetofinalfruki.application.dtos;

import org.example.projetofinalfruki.domain.model.Estacao;

import java.time.LocalDate;
import java.util.List;

/** Contexto usado pelo recomendador e exibido no catálogo. */
public record ContextoDTO(
        String cidade,
        String uf,
        LocalDate data,
        Estacao estacao,
        String estacaoDescricao,
        double temperatura,
        String fonteClima,
        boolean feriadoProximo,
        String descricao,
        List<FeriadoDTO> feriados
) {
}
