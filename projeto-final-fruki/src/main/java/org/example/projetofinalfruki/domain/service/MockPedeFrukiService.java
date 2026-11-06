package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.example.projetofinalfruki.application.dtos.HistoricoCompraDTO;
import org.example.projetofinalfruki.application.dtos.ItemRecomendadoDTO;
import org.example.projetofinalfruki.application.dtos.ProdutoDTO;
import org.example.projetofinalfruki.application.dtos.RecomendacaoResponseDTO;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.example.projetofinalfruki.domain.model.PerfilRegional;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Dados de demonstração usados quando os serviços de origem não devolvem dados utilizáveis. */
@Service
public class MockPedeFrukiService {

    private static final List<ProdutoDTO> PRODUTOS_DEMO = List.of(
            new ProdutoDTO("MOCK-GUARANA-2L", "Refrigerante Fruki Guaraná 2 L", "Refrigerantes", new BigDecimal("8.99")),
            new ProdutoDTO("MOCK-AGUA-500", "Água Mineral Fruki com gás 500 ml", "Águas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-ELEV-473", "Energético Elev Maçã Verde 473 ml", "Energéticos", new BigDecimal("9.90")),
            new ProdutoDTO("MOCK-BELLAVISTA-350", "Cerveja Bellavista Pilsen 350 ml", "Cervejas", new BigDecimal("5.49")),
            new ProdutoDTO("MOCK-CITRUS-1L", "Refresco Fruki Citrus 1 L", "Refrescos", new BigDecimal("6.79"))
    );

    public List<HistoricoCompraDTO> historico(int limite) {
        LocalDateTime agora = LocalDateTime.now();
        List<HistoricoCompraDTO> demo = List.of(
                new HistoricoCompraDTO(-1L, agora.minusDays(7), new BigDecimal("186.40"), "DEMONSTRAÇÃO",
                        "12x Refrigerante Fruki Guaraná 2 L · 8x Água Mineral Fruki 500 ml", true),
                new HistoricoCompraDTO(-2L, agora.minusDays(21), new BigDecimal("124.80"), "DEMONSTRAÇÃO",
                        "8x Refrigerante Fruki Guaraná 2 L · 8x Energético Elev 473 ml", true),
                new HistoricoCompraDTO(-3L, agora.minusDays(35), new BigDecimal("98.70"), "DEMONSTRAÇÃO",
                        "10x Água Mineral Fruki com gás 500 ml · 6x Bellavista Pilsen 350 ml", true)
        );
        return demo.subList(0, Math.min(Math.max(limite, 1), demo.size()));
    }

    public RecomendacaoResponseDTO recomendacoes(Long idCliente, ClienteEntity cliente,
                                                  List<ItemRecomendadoDTO> atuais, int limite,
                                                  CatalogoService catalogo) {
        List<ItemRecomendadoDTO> itens = new ArrayList<>(atuais == null ? List.of() : atuais);
        if (itens.size() < 4 && catalogo != null) {
            try {
                for (ProdutoDTO produto : catalogo.listarProdutos(200).stream().map(catalogo::paraDTO).toList()) {
                    if (itens.size() >= 5) break;
                    if (itens.stream().noneMatch(item -> item.codItem().equals(produto.codItem()))) {
                        itens.add(item(produto, itens.size() + 1, 94.0 - itens.size() * 2.3, false));
                    }
                }
            } catch (DataAccessException ignored) {
                // A demonstração continua funcionando sem conexão ao catálogo transacional.
            }
        }

        int alvo = Math.min(5, Math.max(4, limite));
        for (ProdutoDTO produto : PRODUTOS_DEMO) {
            if (itens.size() >= alvo) break;
            if (itens.stream().noneMatch(item -> item.codItem().equals(produto.codItem()))) {
                itens.add(item(produto, itens.size() + 1, 91.5 - itens.size() * 2.1, true));
            }
        }
        if (itens.size() > limite) itens = new ArrayList<>(itens.subList(0, limite));

        String cidade = cliente == null ? "Porto Alegre" : cliente.getCidade();
        String uf = cliente == null ? "RS" : cliente.getUf();
        String nome = cliente == null ? "Loja de Demonstração" : cliente.getNomeFantasia();
        PerfilRegional perfil = PerfilRegional.fromCidade(cidade, uf);
        return new RecomendacaoResponseDTO(idCliente, nome, cidade, uf, perfil.getDescricao(), contexto(cidade, uf), itens);
    }

    public ContextoDTO contexto(String cidade, String uf) {
        LocalDate hoje = LocalDate.now();
        Estacao estacao = Estacao.de(hoje);
        return new ContextoDTO(cidade == null ? "Porto Alegre" : cidade, uf == null ? "RS" : uf,
                hoje, estacao, estacao.getDescricao(), 21.0, "DEMONSTRAÇÃO", false,
                "Contexto demonstrativo · Sazonalidade: " + estacao.getDescricao(), List.<FeriadoDTO>of());
    }

    private ItemRecomendadoDTO item(ProdutoDTO produto, int posicao, double score, boolean demo) {
        String motivoSazonal = "Sazonalidade: " + Estacao.de(LocalDate.now()).getDescricao();
        return new ItemRecomendadoDTO(posicao, produto.codItem(), produto.nome(), produto.categoria(), produto.preco(),
                Math.max(70.0, score), List.of("Alta saída no seu perfil", motivoSazonal),
                Map.of("rfm", .82, "regional", .78, "sazonalidade", .86, "margem", .72, "penalidade", 0.0), demo);
    }
}
