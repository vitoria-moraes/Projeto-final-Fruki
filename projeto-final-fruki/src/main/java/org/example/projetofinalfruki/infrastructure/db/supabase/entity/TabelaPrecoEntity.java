package org.example.projetofinalfruki.infrastructure.db.supabase.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tabela_preco")
public class TabelaPrecoEntity {

    @Id
    private String id; // Remova qualquer @GeneratedValue daqui

    private String codProduto;
    private String nomeProduto;
    private BigDecimal precoBase;

    public TabelaPrecoEntity() {}

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodProduto() { return codProduto; }
    public void setCodProduto(String codProduto) { this.codProduto = codProduto; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }
}