package org.example.projetofinalfruki.domain.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PrecificacaoService {

    public BigDecimal calcularPrecoComDesconto(BigDecimal precoBase, BigDecimal percentualDesconto) {
        if (precoBase == null || percentualDesconto == null) {
            return precoBase != null ? precoBase : BigDecimal.ZERO;
        }
        BigDecimal fatorDesconto = percentualDesconto.divide(new BigDecimal("100"));
        BigDecimal valorDesconto = precoBase.multiply(fatorDesconto);
        return precoBase.subtract(valorDesconto);
    }
}