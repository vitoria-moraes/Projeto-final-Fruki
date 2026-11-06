package org.example.projetofinalfruki.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.example.projetofinalfruki.domain.service.ContextoService;
import org.example.projetofinalfruki.domain.service.MotorSazonalidadeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MotorSazonalidadeTest {

    @Mock
    private ContextoService contextoService;

    @InjectMocks
    private MotorSazonalidadeService motor;

    @Test
    @DisplayName("destaca feriado/evento quando está próximo")
    void mensagemDeFeriado() {
        when(contextoService.obterContexto("Porto Alegre", null))
                .thenReturn(contexto(Estacao.VERAO, 30.0, true));

        String mensagem = motor.gerarRecomendacaoParaCidade("Porto Alegre");

        assertTrue(mensagem.contains("Feriado"));
    }

    @Test
    @DisplayName("destaca dia quente acima de 28 °C")
    void mensagemDeCalor() {
        when(contextoService.obterContexto("Porto Alegre", null))
                .thenReturn(contexto(Estacao.VERAO, 31.0, false));

        assertTrue(motor.gerarRecomendacaoParaCidade("Porto Alegre").contains("Dia quente"));
    }

    @Test
    @DisplayName("destaca inverno quando não há calor nem feriado")
    void mensagemDeInverno() {
        when(contextoService.obterContexto("Gramado", null))
                .thenReturn(contexto(Estacao.INVERNO, 10.0, false));

        assertTrue(motor.gerarRecomendacaoParaCidade("Gramado").contains("Inverno"));
    }

    private ContextoDTO contexto(Estacao estacao, double temperatura, boolean feriado) {
        return new ContextoDTO("Porto Alegre", "RS", LocalDate.now(), estacao,
                estacao.getDescricao(), temperatura, "TESTE", feriado, "teste", List.of());
    }
}
