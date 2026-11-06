package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.PedidoRequestDTO;
import org.example.projetofinalfruki.application.dtos.PedidoResponseDTO;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.util.CnpjValidator;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.PedidoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.PedidoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido real: valida o cliente, calcula o total com a tabela de preços
 * e grava em fato_vendas. Substitui o retorno fixo de 150.00.
 */
@Service
public class PedidoService {

    private final ClienteJpaRepository clienteRepository;
    private final PedidoJpaRepository pedidoRepository;
    private final CatalogoService catalogo;

    public PedidoService(ClienteJpaRepository clienteRepository,
                         PedidoJpaRepository pedidoRepository,
                         CatalogoService catalogo) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.catalogo = catalogo;
    }

    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO dto) {
        String cnpj = CnpjValidator.normalizar(dto.getCnpjCliente());
        ClienteEntity cliente = clienteRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cnpj));

        BigDecimal total = BigDecimal.ZERO;
        List<String> resumo = new ArrayList<>();

        for (PedidoRequestDTO.ItemPedidoDTO item : dto.getItens()) {
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade inválida para o produto " + item.getCodigoProduto());
            }
            TabelaPrecoEntity produto = catalogo.buscarProduto(item.getCodigoProduto())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + item.getCodigoProduto()));
            if (produto.getPrecoBase() == null || produto.getPrecoBase().signum() <= 0) {
                throw new IllegalArgumentException("Preço não cadastrado para o produto " + item.getCodigoProduto());
            }
            BigDecimal subtotal = produto.getPrecoBase()
                    .multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotal);
            resumo.add(item.getQuantidade() + "x " + produto.getNomeProduto());
        }

        PedidoEntity pedido = PedidoEntity.builder()
                .codCliente(cliente.getCodCliente())
                .dataPedido(LocalDateTime.now())
                .valorTotal(total)
                .status("CRIADO")
                .resumoItens(String.join(", ", resumo))
                .build();
        pedidoRepository.save(pedido);

        return new PedidoResponseDTO(
                "FRK-" + pedido.getId() + "-" + System.currentTimeMillis(),
                "CRIADO",
                total,
                LocalDateTime.now(),
                "Pedido registrado com sucesso.");
    }
}
