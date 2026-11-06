package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.example.projetofinalfruki.infrastructure.external.weather.OpenWeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextoServiceTest {

    @Mock
    private OpenWeatherService clima;

    @Mock
    private FeriadoService feriados;

    @InjectMocks
    private ContextoService contextoService;

    @Test
    @DisplayName("monta contexto de verão com feriado próximo")
    void contextoDeVeraoComFeriado() {
        LocalDate data = LocalDate.of(2026, 1, 10);
        when(clima.obterClima("Porto Alegre"))
                .thenReturn(new OpenWeatherService.ClimaInfo(31.0, "OPENWEATHER", "clima real"));
        when(feriados.buscarFeriadosCompletos(2026, "Porto Alegre"))
                .thenReturn(List.of(new FeriadoDTO(data.plusDays(5), "Teste", "MUNICIPAL", "RS", "Porto Alegre")));

        ContextoDTO ctx = contextoService.obterContexto("Porto Alegre", "RS", data);

        assertEquals(Estacao.VERAO, ctx.estacao());
        assertEquals(31.0, ctx.temperatura());
        assertTrue(ctx.feriadoProximo());
        assertTrue(ctx.descricao().contains("Verão"));
    }

    @Test
    @DisplayName("estação do ano para uma data de inverno")
    void estacaoDeInverno() {
        assertEquals(Estacao.INVERNO, contextoService.estacao(LocalDate.of(2026, 7, 1)).estacao());
    }

    @Test
    @DisplayName("não considera feriado quando a lista está vazia")
    void semFeriado() {
        when(clima.obterClima(anyString()))
                .thenReturn(new OpenWeatherService.ClimaInfo(25.0, "FALLBACK", "climatologia"));
        when(feriados.buscarFeriadosCompletos(eq(2026), anyString())).thenReturn(List.of());

        ContextoDTO ctx = contextoService.obterContexto("Lajeado", "RS", LocalDate.of(2026, 5, 1));

        assertEquals(Estacao.OUTONO, ctx.estacao());
        assertTrue(!ctx.feriadoProximo());
    }
}
