package org.example.projetofinalfruki.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Linha da tabela gold fruki.gold.gold_recomendacao_b2b (Databricks).
 * É um objeto simples (sem @Entity): a tabela é só lida, uma linha por (cliente, item).
 */
public class RecomendacaoB2B {

    private Long idCliente;
    private String codItem;
    private Long totalQuantidadeComprada;
    private BigDecimal valorTotalGasto;
    private LocalDate ultimaDataCompra;
    private Long frequenciaPedidos;
    private Double scoreRecomendacao;

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public String getCodItem() { return codItem; }
    public void setCodItem(String codItem) { this.codItem = codItem; }

    public Long getTotalQuantidadeComprada() { return totalQuantidadeComprada; }
    public void setTotalQuantidadeComprada(Long v) { this.totalQuantidadeComprada = v; }

    public BigDecimal getValorTotalGasto() { return valorTotalGasto; }
    public void setValorTotalGasto(BigDecimal v) { this.valorTotalGasto = v; }

    public LocalDate getUltimaDataCompra() { return ultimaDataCompra; }
    public void setUltimaDataCompra(LocalDate v) { this.ultimaDataCompra = v; }

    public Long getFrequenciaPedidos() { return frequenciaPedidos; }
    public void setFrequenciaPedidos(Long v) { this.frequenciaPedidos = v; }

    public Double getScoreRecomendacao() { return scoreRecomendacao; }
    public void setScoreRecomendacao(Double scoreRecomendacao) { this.scoreRecomendacao = scoreRecomendacao; }
}