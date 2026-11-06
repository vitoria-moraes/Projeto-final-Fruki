package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.CompraRequestDTO;
import org.example.projetofinalfruki.application.dtos.CompraResponseDTO;
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

/**
 * Registra uma compra (item vendido) na fato_vendas, calculando o total
 * com o preço da tabela. É a porta de entrada de dados para o recomendador.
 */
@Service
public class CompraService {

    private final ClienteJpaRepository clienteRepository;
    private final PedidoJpaRepository pedidoRepository;
    private final CatalogoService catalogo;

    public CompraService(ClienteJpaRepository clienteRepository,
                         PedidoJpaRepository pedidoRepository,
                         CatalogoService catalogo) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.catalogo = catalogo;
    }

    @Transactional
    public CompraResponseDTO registrar(CompraRequestDTO dto) {
        String cnpj = CnpjValidator.normalizar(dto.cnpj());
        ClienteEntity cliente = clienteRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cnpj));

        TabelaPrecoEntity produto = catalogo.buscarProduto(dto.codItem())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + dto.codItem()));

        BigDecimal preco = dto.valorUnitario() != null ? dto.valorUnitario() : produto.getPrecoBase();
        if (preco == null || preco.signum() <= 0) {
            throw new IllegalArgumentException("Preço não cadastrado para o produto " + dto.codItem());
        }
        BigDecimal total = preco.multiply(BigDecimal.valueOf(dto.quantidade()));

        PedidoEntity pedido = PedidoEntity.builder()
                .codCliente(String.valueOf(cliente.getCodCliente()))
                .dataPedido(dto.dataCompra().atStartOfDay())
                .valorTotal(total)
                .status("REGISTRADA")
                .resumoItens(dto.quantidade() + "x " + produto.getNomeProduto())
                .build();
        pedidoRepository.save(pedido);

        return new CompraResponseDTO(
                pedido.getId(),
                cnpj,
                cliente.getNomeFantasia(),
                dto.codItem(),
                dto.quantidade(),
                preco,
                total,
                dto.dataCompra());
    }
}
