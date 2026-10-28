package org.example.projetofinalfruki.domain.model;

public class Cliente {
    private String codCliente;
    private String cnpj;
    private String nomeFantasia;
    private String cidade;

    public Cliente() {}

    public Cliente(String codCliente, String cnpj, String nomeFantasia, String cidade) {
        this.codCliente = codCliente;
        this.cnpj = cnpj;
        this.nomeFantasia = nomeFantasia;
        this.cidade = cidade;
    }

    public String getCodCliente() { return codCliente; }
    public void setCodCliente(String codCliente) { this.codCliente = codCliente; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
}