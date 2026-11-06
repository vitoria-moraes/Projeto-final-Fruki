package org.example.projetofinalfruki.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CnpjValidatorTest {

    @Test
    @DisplayName("aceita CNPJ válido com e sem máscara")
    void aceitaCnpjValido() {
        assertTrue(CnpjValidator.valido("11.222.333/0001-81"));
        assertTrue(CnpjValidator.valido("11222333000181"));
    }

    @Test
    @DisplayName("rejeita CNPJ com dígito errado, curto ou repetido")
    void rejeitaCnpjInvalido() {
        assertFalse(CnpjValidator.valido("11.222.333/0001-80"));
        assertFalse(CnpjValidator.valido("123"));
        assertFalse(CnpjValidator.valido("00000000000000"));
        assertFalse(CnpjValidator.valido(null));
    }

    @Test
    @DisplayName("normaliza removendo máscara")
    void normaliza() {
        assertEquals("11222333000181", CnpjValidator.normalizar("11.222.333/0001-81"));
        assertEquals("", CnpjValidator.normalizar(null));
    }
}
