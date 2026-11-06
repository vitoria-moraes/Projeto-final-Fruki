package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ClienteRequestDTO;
import org.example.projetofinalfruki.application.dtos.ClienteResponseDTO;
import org.example.projetofinalfruki.application.dtos.HistoricoCompraDTO;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.exception.ConflitoException;
import org.example.projetofinalfruki.domain.util.CnpjValidator;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.PedidoJpaRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.PedidoEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Locale;

/** Cadastro e consulta de lojistas no Supabase. */
@Service
public class ClienteService {

    private final ClienteJpaRepository repository;
    private final PedidoJpaRepository pedidoRepository;
    private final MockPedeFrukiService mockService;

    public ClienteService(ClienteJpaRepository repository, PedidoJpaRepository pedidoRepository,
                          MockPedeFrukiService mockService) {
        this.repository = repository;
        this.pedidoRepository = pedidoRepository;
        this.mockService = mockService;
    }

    @Transactional
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        String cnpj = CnpjValidator.normalizar(dto.cnpj());
        if (!CnpjValidator.valido(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido: " + dto.cnpj());
        }
        if (repository.findByCnpj(cnpj).isPresent()) {
            throw new ConflitoException("Já existe cliente com o CNPJ " + cnpj);
        }

        ClienteEntity entity = new ClienteEntity();
        entity.setCodCliente(dto.codCliente());
        entity.setCnpj(cnpj);
        entity.setNomeFantasia(dto.nomeFantasia().trim());
        entity.setCidade(dto.cidade().trim());
        entity.setUf(dto.uf().trim().toUpperCase(Locale.ROOT));
        entity.setCodHierarquia(dto.codHierarquia());
        repository.save(entity);
        return paraDTO(entity);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorCnpj(String cnpj) {
        String normalizado = CnpjValidator.normalizar(cnpj);
        return repository.findByCnpj(normalizado)
                .map(this::paraDTO)
                .orElseThrow(() -> new ClienteNaoEncontradoException(normalizado));
    }

    /** Lista os clientes cadastrados (para a tela de CNPJs do painel). */
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listar(int limite) {
        int seguro = Math.max(1, Math.min(limite, 500));
        return repository.findAll(PageRequest.of(0, seguro, Sort.by("codCliente").ascending()))
                .stream()
                .map(this::paraDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoCompraDTO> historico(String cnpj, int limite) {
        ClienteEntity cliente;
        try {
            cliente = repository.findByCnpj(CnpjValidator.normalizar(cnpj))
                    .orElseThrow(() -> new ClienteNaoEncontradoException(cnpj));
        } catch (DataAccessException e) {
            return mockService.historico(limite);
        }
        try {
            List<HistoricoCompraDTO> historico = pedidoRepository.findByCodClienteOrderByDataVendaDesc(
                            cliente.getCodCliente(), PageRequest.of(0, Math.max(1, Math.min(limite, 20))))
                    .stream().map(this::paraHistorico).toList();
            return historico.isEmpty() ? mockService.historico(limite) : historico;
        } catch (DataAccessException e) {
            return mockService.historico(limite);
        }
    }

    private HistoricoCompraDTO paraHistorico(PedidoEntity pedido) {
        return new HistoricoCompraDTO(pedido.getId(), pedido.getDataPedido(), pedido.getValorTotal(),
                "REGISTRADA", "A tabela transacional atual não armazena o detalhamento dos itens.", false);
    }

    private ClienteResponseDTO paraDTO(ClienteEntity entity) {
        return new ClienteResponseDTO(
                entity.getCodCliente(),
                entity.getCnpj(),
                entity.getNomeFantasia(),
                entity.getCidade(),
                entity.getUf(),
                entity.getCodHierarquia());
    }
}
