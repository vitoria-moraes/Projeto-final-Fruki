package org.example.projetofinalfruki.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PedidoRequestDTO {

    @NotBlank(message = "O CNPJ do cliente é obrigatório")
    private String cnpjCliente;

    @NotEmpty(message = "O pedido deve conter pelo menos um item")
    private List<ItemPedidoDTO> itens;

    // Getters e Setters
    public String getCnpjCliente() {
        return cnpjCliente;
    }

    public void setCnpjCliente(String cnpjCliente) {
        this.cnpjCliente = cnpjCliente;
    }

    public List<ItemPedidoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoDTO> itens) {
        this.itens = itens;
    }

    // Sub-DTO para os itens do pedido
    public static class ItemPedidoDTO {
        @NotBlank(message = "O código do produto é obrigatório")
        private String codigoProduto;

        @NotNull(message = "A quantidade é obrigatória")
        private Integer quantidade;

        public String getCodigoProduto() {
            return codigoProduto;
        }

        public void setCodigoProduto(String codigoProduto) {
            this.codigoProduto = codigoProduto;
        }

        public Integer getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }
    }
}