package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.ItemRecomendadoDTO;
import org.example.projetofinalfruki.application.dtos.RecomendacaoResponseDTO;
import org.example.projetofinalfruki.config.RecomendacaoProperties;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.exception.DatabricksIndisponivelException;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.example.projetofinalfruki.domain.model.RecomendacaoB2B;
import org.example.projetofinalfruki.infrastructure.db.supabase.databricks.RecomendacaoRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecomendacaoServiceTest {

    @Mock
    private RecomendacaoRepository recomendacaoRepository;
    @Mock
    private ClienteJpaRepository clienteRepository;
    @Mock
    private RecomendadorV2Service recomendador;
    @Mock
    private ContextoService contextoService;

    private RecomendacaoService service;
    private ContextoDTO contexto;

    @BeforeEach
    void setUp() {
        service = new RecomendacaoService(recomendacaoRepository, clienteRepository,
                recomendador, contextoService, new RecomendacaoProperties());
        contexto = new ContextoDTO("Porto Alegre", "RS", LocalDate.now(), Estacao.VERAO,
                "Verão", 30.0, "TESTE", false, "teste", List.of());
    }

    @Test
    @DisplayName("monta resposta quando a Gold tem histórico")
    void recomendaComHistorico() {
        RecomendacaoB2B linha = new RecomendacaoB2B();
        linha.setCodItem("00001");
        when(recomendacaoRepository.findByIdClienteOrderByScoreRecomendacaoDesc(41L)).thenReturn(List.of(linha));
        when(clienteRepository.findById("41")).thenReturn(Optional.empty());
        when(contextoService.obterContexto(null, null)).thenReturn(contexto);
        when(recomendador.recomendar(any(), any(), any(), anyInt()))
                .thenReturn(List.of(new ItemRecomendadoDTO(1, "00001", "Fruki Guaraná 2L",
                        "Refrigerantes", null, 90.0, List.of("teste"), java.util.Map.of())));

        RecomendacaoResponseDTO resposta = service.recomendar(41L, 10);

        assertEquals(41L, resposta.idCliente());
        assertNull(resposta.nomeCliente());
        assertEquals(1, resposta.itens().size());
        assertEquals("Mercado Central", resposta.perfil());
    }

    @Test
    @DisplayName("usa o nome do cliente quando ele existe no Supabase")
    void enriqueceComNomeDoCliente() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setCodCliente("41");
        cliente.setNomeFantasia("Mercado Central");
        cliente.setCidade("Porto Alegre");
        cliente.setUf("RS");

        when(recomendacaoRepository.findByIdClienteOrderByScoreRecomendacaoDesc(41L)).thenReturn(List.of(linha()));
        when(clienteRepository.findById("41")).thenReturn(Optional.of(cliente));
        when(contextoService.obterContexto("Porto Alegre", "RS")).thenReturn(contexto);
        when(recomendador.recomendar(any(), any(), any(), anyInt())).thenReturn(List.of());

        RecomendacaoResponseDTO resposta = service.recomendar(41L, 10);

        assertEquals("Mercado Central", resposta.nomeCliente());
        assertEquals("Mercado Central", resposta.perfil());
    }

    @Test
    @DisplayName("404 quando não há histórico nem cliente")
    void lanca404() {
        when(recomendacaoRepository.findByIdClienteOrderByScoreRecomendacaoDesc(999L)).thenReturn(List.of());
        when(clienteRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> service.recomendar(999L, 10));
    }

    @Test
    @DisplayName("propaga 503 do Databricks")
    void propaga503() {
        when(recomendacaoRepository.findByIdClienteOrderByScoreRecomendacaoDesc(41L))
                .thenThrow(new DatabricksIndisponivelException("fora", new RuntimeException()));

        assertThrows(DatabricksIndisponivelException.class, () -> service.recomendar(41L, 10));
    }

    private RecomendacaoB2B linha() {
        RecomendacaoB2B r = new RecomendacaoB2B();
        r.setCodItem("00001");
        return r;
    }
}
