package org.example.projetofinalfruki.domain.model;

import java.math.BigDecimal;

public class TabelaPreco {
    private String id;
    private String codProduto;
    private String nomeProduto;
    private BigDecimal precoBase;

    public TabelaPreco() {}

    public TabelaPreco(String id, String codProduto, String nomeProduto, BigDecimal precoBase) {
        this.id = id;
        this.codProduto = codProduto;
        this.nomeProduto = nomeProduto;
        this.precoBase = precoBase;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodProduto() { return codProduto; }
    public void setCodProduto(String codProduto) { this.codProduto = codProduto; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }
}