package org.example.projetofinalfruki.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoResponseDTO {

    private String codigoPedido;
    private String status;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;
    private String mensagem;

    public PedidoResponseDTO(String codigoPedido, String status, BigDecimal valorTotal, LocalDateTime dataCriacao, String mensagem) {
        this.codigoPedido = codigoPedido;
        this.status = status;
        this.valorTotal = valorTotal;
        this.dataCriacao = dataCriacao;
        this.mensagem = mensagem;
    }

    // Getters e Setters
    public String getCodigoPedido() {
        return codigoPedido;
    }

    public void setCodigoPedido(String codigoPedido) {
        this.codigoPedido = codigoPedido;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}