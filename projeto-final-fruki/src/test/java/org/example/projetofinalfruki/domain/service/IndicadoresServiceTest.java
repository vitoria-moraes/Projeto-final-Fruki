package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.IndicadoresResponseDTO;
import org.example.projetofinalfruki.infrastructure.db.supabase.databricks.IndicadoresRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicadoresServiceTest {

    @Mock
    private IndicadoresRepository indicadoresRepository;
    @Mock
    private ClienteJpaRepository clienteRepository;

    @InjectMocks
    private IndicadoresService service;

    @Test
    @DisplayName("calcula cobertura, taxa e média sem divisão por zero")
    void calculaIndicadores() {
        when(indicadoresRepository.resumo())
                .thenReturn(new IndicadoresRepository.Resumo(2, 10, 5));
        when(indicadoresRepository.clientesComPeloMenos(3)).thenReturn(1L);
        when(clienteRepository.count()).thenReturn(10L);

        IndicadoresResponseDTO dto = service.calcular();

        assertEquals(10L, dto.clientesTotal());
        assertEquals(2L, dto.clientesComRecomendacao());
        assertEquals(20.0, dto.coberturaPercent());
        assertEquals(5L, dto.itensDistintos());
        assertEquals(50.0, dto.taxaRecomendacaoPercent());
        assertEquals(5.0, dto.mediaItensPorCliente());
    }

    @Test
    @DisplayName("base vazia devolve zeros")
    void baseVazia() {
        when(indicadoresRepository.resumo())
                .thenReturn(new IndicadoresRepository.Resumo(0, 0, 0));
        when(indicadoresRepository.clientesComPeloMenos(3)).thenReturn(0L);
        when(clienteRepository.count()).thenReturn(0L);

        IndicadoresResponseDTO dto = service.calcular();

        assertEquals(0.0, dto.coberturaPercent());
        assertEquals(0.0, dto.taxaRecomendacaoPercent());
        assertEquals(0.0, dto.mediaItensPorCliente());
    }
}
