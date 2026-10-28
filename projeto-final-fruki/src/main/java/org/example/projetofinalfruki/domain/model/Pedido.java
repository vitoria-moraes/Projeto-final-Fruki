package org.example.projetofinalfruki.domain.model;

import java.math.BigDecimal;

public class Pedido {
    private String codigoPedido;
    private String codCliente;
    private String produto;
    private Integer quantidade;
    private BigDecimal valorTotal;

    public Pedido() {}

    public Pedido(String codigoPedido, String codCliente, String produto, Integer quantidade, BigDecimal valorTotal) {
        this.codigoPedido = codigoPedido;
        this.codCliente = codCliente;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
    }

    public String getCodigoPedido() { return codigoPedido; }
    public void setCodigoPedido(String codigoPedido) { this.codigoPedido = codigoPedido; }

    public String getCodCliente() { return codCliente; }
    public void setCodCliente(String codCliente) { this.codCliente = codCliente; }

    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}