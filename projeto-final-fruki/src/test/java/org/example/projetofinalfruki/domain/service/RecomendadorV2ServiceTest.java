package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.ItemRecomendadoDTO;
import org.example.projetofinalfruki.config.RecomendacaoProperties;
import org.example.projetofinalfruki.domain.model.CategoriaProduto;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.example.projetofinalfruki.domain.model.RecomendacaoB2B;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecomendadorV2ServiceTest {

    @Mock
    private CatalogoService catalogo;

    private RecomendadorV2Service recomendador;
    private RecomendacaoProperties props;
    private ClienteEntity cliente;

    @BeforeEach
    void setUp() {
        props = new RecomendacaoProperties();
        recomendador = new RecomendadorV2Service(catalogo, props);
        cliente = new ClienteEntity();
        cliente.setCodCliente("41");
        cliente.setNomeFantasia("Mercado Central");
        cliente.setCidade("Porto Alegre");
        cliente.setUf("RS");

        lenient().when(catalogo.categoriaDe(any(TabelaPrecoEntity.class)))
                .thenAnswer(inv -> CategoriaProduto.fromNome(
                        ((TabelaPrecoEntity) inv.getArgument(0)).getNomeProduto()));
    }

    @Test
    @DisplayName("devolve itens ordenados, com score 0..100 e explicação")
    void recomendaComScoreEMotivos() {
        when(catalogo.buscarProduto("00001")).thenReturn(java.util.Optional.of(produto("00001", "Fruki Guaraná 2L", "7.49")));
        when(catalogo.buscarProduto("00002")).thenReturn(java.util.Optional.of(produto("00002", "Água da Pedra 500ml", "22.90")));

        List<RecomendacaoB2B> historico = List.of(
                linha("00001", 120, "950.50", LocalDate.now().minusDays(10), 8, 0.92),
                linha("00002", 60, "480.00", LocalDate.now().minusDays(80), 5, 0.75));

        List<ItemRecomendadoDTO> itens = recomendador.recomendar(historico, cliente, contexto(Estacao.VERAO, 31.0, false), 10);

        assertEquals(2, itens.size());
        assertEquals(1, itens.get(0).posicao());
        assertEquals(2, itens.get(1).posicao());
        assertTrue(itens.stream().allMatch(i -> i.score() >= 0 && i.score() <= 100));
        assertFalse(itens.get(0).motivos().isEmpty());
        assertTrue(itens.get(0).detalhe().containsKey("rfm"));
    }

    @Test
    @DisplayName("aplica diversidade: no máximo 2 itens por categoria")
    void respeitaDiversidade() {
        when(catalogo.buscarProduto(anyString())).thenAnswer(inv ->
                java.util.Optional.of(produto(inv.getArgument(0), "Fruki Guaraná " + inv.getArgument(0), "7.49")));

        List<RecomendacaoB2B> historico = List.of(
                linha("00001", 10, "100", LocalDate.now().minusDays(5), 3, 0.5),
                linha("00002", 10, "100", LocalDate.now().minusDays(6), 3, 0.5),
                linha("00003", 10, "100", LocalDate.now().minusDays(7), 3, 0.5));

        List<ItemRecomendadoDTO> itens = recomendador.recomendar(historico, cliente, contexto(Estacao.VERAO, 30.0, false), 10);

        assertEquals(2, itens.size());
        assertTrue(itens.stream().allMatch(i -> i.categoria().equals("Refrigerantes")));
    }

    @Test
    @DisplayName("penaliza produto comprado nos últimos dias")
    void penalizaCompraRecente() {
        when(catalogo.buscarProduto("00001")).thenReturn(java.util.Optional.of(produto("00001", "Fruki Guaraná 2L", "7.49")));

        List<RecomendacaoB2B> historico = List.of(
                linha("00001", 10, "100", LocalDate.now().minusDays(1), 5, 0.9));

        List<ItemRecomendadoDTO> itens = recomendador.recomendar(historico, cliente, contexto(Estacao.VERAO, 30.0, false), 10);

        assertEquals(1, itens.size());
        assertTrue(itens.get(0).detalhe().get("penalidade") >= 15.0);
        assertTrue(itens.get(0).motivos().stream().anyMatch(m -> m.contains("comprado há")));
    }

    @Test
    @DisplayName("cold start: sem histórico usa o catálogo e explicita o motivo")
    void coldStartUsaCatalogo() {
        when(catalogo.listarProdutos(anyInt())).thenReturn(List.of(
                produto("00001", "Fruki Guaraná 2L", "7.49"),
                produto("00002", "Água da Pedra 500ml", "22.90"),
                produto("00003", "Elev Energy Drink 473ml", "5.99")));

        List<ItemRecomendadoDTO> itens = recomendador.recomendar(List.of(), cliente, contexto(Estacao.VERAO, 31.0, true), 10);

        assertEquals(3, itens.size());
        assertTrue(itens.stream().anyMatch(i -> i.motivos().stream().anyMatch(m -> m.contains("sem histórico"))));
    }

    private ContextoDTO contexto(Estacao estacao, double temperatura, boolean feriado) {
        return new ContextoDTO("Porto Alegre", "RS", LocalDate.now(), estacao,
                estacao.getDescricao(), temperatura, "TESTE", feriado, "teste", List.of());
    }

    private TabelaPrecoEntity produto(String cod, String nome, String preco) {
        TabelaPrecoEntity p = new TabelaPrecoEntity();
        p.setId("id-" + cod);
        p.setCodProduto(cod);
        p.setNomeProduto(nome);
        p.setPrecoBase(new BigDecimal(preco));
        return p;
    }

    private RecomendacaoB2B linha(String cod, long qtd, String valor, LocalDate ultima, long freq, double score) {
        RecomendacaoB2B r = new RecomendacaoB2B();
        r.setIdCliente(41L);
        r.setCodItem(cod);
        r.setTotalQuantidadeComprada(qtd);
        r.setValorTotalGasto(new BigDecimal(valor));
        r.setUltimaDataCompra(ultima);
        r.setFrequenciaPedidos(freq);
        r.setScoreRecomendacao(score);
        return r;
    }
}
