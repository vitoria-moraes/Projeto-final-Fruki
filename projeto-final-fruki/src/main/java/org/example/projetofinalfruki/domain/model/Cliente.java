package org.example.projetofinalfruki.domain.model;

public class Cliente {
    private Long codCliente;
    private String cnpj;
    private String nomeFantasia;
    private String cidade;

    public Cliente() {}

    public Cliente(Long codCliente, String cnpj, String nomeFantasia, String cidade) {
        this.codCliente = codCliente;
        this.cnpj = cnpj;
        this.nomeFantasia = nomeFantasia;
        this.cidade = cidade;
    }

    public Long getCodCliente() { return codCliente; }
    public void setCodCliente(Long codCliente) { this.codCliente = codCliente; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
}