package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ProdutoDTO;
import org.example.projetofinalfruki.domain.model.CategoriaProduto;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.TabelaPrecoJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolve produto/preço a partir do código do item.
 * A tabela do Supabase pode usar "1", "00001" ou "0001": tentamos as três formas.
 * O cache em memória evita repetir a consulta dentro de uma mesma execução.
 */
@Service
public class CatalogoService {

    private final TabelaPrecoJpaRepository repository;
    private final Map<String, Optional<TabelaPrecoEntity>> cache = new ConcurrentHashMap<>();

    public CatalogoService(TabelaPrecoJpaRepository repository) {
        this.repository = repository;
    }

    public Optional<TabelaPrecoEntity> buscarProduto(String codItem) {
        if (codItem == null || codItem.isBlank()) {
            return Optional.empty();
        }
        String chave = codItem.trim();
        return cache.computeIfAbsent(chave, this::buscarNoBanco);
    }

    private Optional<TabelaPrecoEntity> buscarNoBanco(String codItem) {
        String digitos = codItem.replaceAll("\\D", "");
        Optional<TabelaPrecoEntity> achado = repository.findByCodProduto(codItem);
        if (achado.isEmpty() && !digitos.isEmpty()) {
            achado = repository.findByCodProduto(comZeros(digitos, 5));
        }
        if (achado.isEmpty() && !digitos.isEmpty()) {
            achado = repository.findByCodProduto(semZeros(digitos));
        }
        return achado;
    }

    public List<TabelaPrecoEntity> listarProdutos(int limite) {
        int seguro = Math.max(1, Math.min(limite, 200));
        return repository.findAll(PageRequest.of(0, seguro)).getContent();
    }

    public CategoriaProduto categoriaDe(TabelaPrecoEntity produto) {
        return produto == null ? CategoriaProduto.OUTROS : CategoriaProduto.fromNome(produto.getNomeProduto());
    }

    public ProdutoDTO paraDTO(TabelaPrecoEntity produto) {
        return new ProdutoDTO(
                produto.getCodProduto(),
                produto.getNomeProduto(),
                categoriaDe(produto).getDescricao(),
                produto.getPrecoBase()
        );
    }

    public void limparCache() {
        cache.clear();
    }

    private static String comZeros(String digitos, int tamanho) {
        if (digitos.length() >= tamanho) {
            return digitos;
        }
        return "0".repeat(tamanho - digitos.length()) + digitos;
    }

    private static String semZeros(String digitos) {
        String sem = digitos.replaceFirst("^0+(?!$)", "");
        return sem.isEmpty() ? "0" : sem;
    }
}
