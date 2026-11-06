package org.example.projetofinalfruki.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstacaoTest {

    @Test
    @DisplayName("classifica as quatro estações no Hemisfério Sul")
    void classificaEstacoes() {
        assertEquals(Estacao.VERAO, Estacao.de(LocalDate.of(2026, 1, 10)));
        assertEquals(Estacao.VERAO, Estacao.de(LocalDate.of(2026, 12, 25)));
        assertEquals(Estacao.OUTONO, Estacao.de(LocalDate.of(2026, 4, 10)));
        assertEquals(Estacao.INVERNO, Estacao.de(LocalDate.of(2026, 7, 10)));
        assertEquals(Estacao.PRIMAVERA, Estacao.de(LocalDate.of(2026, 10, 10)));
    }

    @Test
    @DisplayName("verão favorece águas e inverno favorece sucos")
    void fatoresPorCategoria() {
        assertTrue(Estacao.VERAO.fator(CategoriaProduto.AGUAS)
                > Estacao.VERAO.fator(CategoriaProduto.SUCOS));
        assertTrue(Estacao.INVERNO.fator(CategoriaProduto.SUCOS)
                > Estacao.INVERNO.fator(CategoriaProduto.AGUAS));
    }
}
