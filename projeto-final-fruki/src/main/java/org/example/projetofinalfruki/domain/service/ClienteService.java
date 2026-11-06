package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ClienteRequestDTO;
import org.example.projetofinalfruki.application.dtos.ClienteResponseDTO;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.exception.ConflitoException;
import org.example.projetofinalfruki.domain.util.CnpjValidator;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/** Cadastro e consulta de lojistas no Supabase. */
@Service
public class ClienteService {

    private final ClienteJpaRepository repository;

    public ClienteService(ClienteJpaRepository repository) {
        this.repository = repository;
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
