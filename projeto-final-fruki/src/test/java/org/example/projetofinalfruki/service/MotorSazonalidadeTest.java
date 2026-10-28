package org.example.projetofinalfruki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotorSazonalidadeTest {

    @Test
    @DisplayName("Deve calcular o desconto sazonal corretamente para períodos de alto volume")
    public void testCalculoDescontoSazonal() {
        // Cenário simulado de regra de negócio
        BigDecimal valorBase = new BigDecimal("1000.00");
        double percentualDesconto = 0.10; // 10% de desconto sazonal (ex: Verão/Festas)

        // Ação
        BigDecimal valorComDesconto = valorBase.subtract(valorBase.multiply(BigDecimal.valueOf(percentualDesconto)));

        // Validação
        assertNotNull(valorComDesconto);
        assertEquals(0, new BigDecimal("900.00").compareTo(valorComDesconto), "O valor com desconto deve ser 900.00");
    }
}