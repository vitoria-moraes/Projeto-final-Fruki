package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.IndicadoresResponseDTO;
import org.example.projetofinalfruki.infrastructure.db.supabase.databricks.IndicadoresRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Calcula cobertura, variedade e taxa de recomendação. */
@Service
public class IndicadoresService {

    private final IndicadoresRepository indicadoresRepository;
    private final ClienteJpaRepository clienteRepository;

    public IndicadoresService(IndicadoresRepository indicadoresRepository,
                              ClienteJpaRepository clienteRepository) {
        this.indicadoresRepository = indicadoresRepository;
        this.clienteRepository = clienteRepository;
    }

    public IndicadoresResponseDTO calcular() {
        IndicadoresRepository.Resumo resumo = indicadoresRepository.resumo();
        long clientesTotal = clienteRepository.count();
        long clientesCom3 = indicadoresRepository.clientesComPeloMenos(3);

        double cobertura = clientesTotal == 0
                ? 0.0
                : 100.0 * resumo.clientesComRecomendacao() / clientesTotal;
        double taxa = resumo.clientesComRecomendacao() == 0
                ? 0.0
                : 100.0 * clientesCom3 / resumo.clientesComRecomendacao();
        double media = resumo.clientesComRecomendacao() == 0
                ? 0.0
                : (double) resumo.totalRecomendacoes() / resumo.clientesComRecomendacao();

        return new IndicadoresResponseDTO(
                clientesTotal,
                resumo.clientesComRecomendacao(),
                arredondar(cobertura),
                resumo.itensDistintos(),
                resumo.totalRecomendacoes(),
                arredondar(taxa),
                arredondar(media));
    }

    private static double arredondar(double valor) {
        return BigDecimal.valueOf(valor).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
