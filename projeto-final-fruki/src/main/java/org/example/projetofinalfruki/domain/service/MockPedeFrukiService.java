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

    private final FeriadoService feriadoService;

    public MockPedeFrukiService() { this(new FeriadoService()); }

    @org.springframework.beans.factory.annotation.Autowired
    public MockPedeFrukiService(FeriadoService feriadoService) { this.feriadoService = feriadoService; }

    private static final List<ProdutoDTO> PRODUTOS_DEMO = List.of(
            new ProdutoDTO("MOCK-00001", "Refrigerante Fruki Guaran\u00e1 2 L", "Refrigerantes", new BigDecimal("7.49")),
            new ProdutoDTO("MOCK-00007", "Refrigerante Fruki Guaran\u00e1 Zero 2 L", "Refrigerantes", new BigDecimal("7.49")),
            new ProdutoDTO("MOCK-00011", "Refrigerante Fruki Guaran\u00e1 1,5 L", "Refrigerantes", new BigDecimal("6.49")),
            new ProdutoDTO("MOCK-00012", "Refrigerante Fruki Guaran\u00e1 600 ml", "Refrigerantes", new BigDecimal("4.29")),
            new ProdutoDTO("MOCK-00013", "Refrigerante Fruki Guaran\u00e1 Zero 600 ml", "Refrigerantes", new BigDecimal("4.29")),
            new ProdutoDTO("MOCK-00014", "Refrigerante Fruki Guaran\u00e1 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00015", "Refrigerante Fruki Guaran\u00e1 Zero 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00016", "Refrigerante Fruki Guaran\u00e1 3 L", "Refrigerantes", new BigDecimal("9.99")),
            new ProdutoDTO("MOCK-00017", "Refrigerante Fruki Guaran\u00e1 Zero 3 L", "Refrigerantes", new BigDecimal("9.99")),
            new ProdutoDTO("MOCK-00018", "Refrigerante Fruki Guaran\u00e1 269 ml", "Refrigerantes", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00019", "Refrigerante Fruki Guaran\u00e1 com Bergamota 2 L", "Refrigerantes", new BigDecimal("7.99")),
            new ProdutoDTO("MOCK-00020", "Refrigerante Fruki Guaran\u00e1 com Bergamota Zero 2 L", "Refrigerantes", new BigDecimal("7.99")),
            new ProdutoDTO("MOCK-00021", "Refrigerante Fruki Guaran\u00e1 com Bergamota 350 ml", "Refrigerantes", new BigDecimal("3.99")),
            new ProdutoDTO("MOCK-00022", "Refrigerante Fruki Guaran\u00e1 com Bergamota Zero 350 ml", "Refrigerantes", new BigDecimal("3.99")),
            new ProdutoDTO("MOCK-00023", "Refrigerante Frukaxi 350 ml", "Refrigerantes", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00024", "Refrigerante Frukaxi Zero 350 ml", "Refrigerantes", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00006", "Refrigerante Fruki Cola 2 L", "Refrigerantes", new BigDecimal("7.29")),
            new ProdutoDTO("MOCK-00025", "Refrigerante Fruki Cola Zero 2 L", "Refrigerantes", new BigDecimal("7.29")),
            new ProdutoDTO("MOCK-00026", "Refrigerante Fruki Cola 1,5 L", "Refrigerantes", new BigDecimal("6.29")),
            new ProdutoDTO("MOCK-00027", "Refrigerante Fruki Cola 600 ml", "Refrigerantes", new BigDecimal("4.19")),
            new ProdutoDTO("MOCK-00028", "Refrigerante Fruki Cola 350 ml", "Refrigerantes", new BigDecimal("3.89")),
            new ProdutoDTO("MOCK-00029", "Refrigerante Fruki Cola Zero 350 ml", "Refrigerantes", new BigDecimal("3.89")),
            new ProdutoDTO("MOCK-00030", "Refrigerante Fruki Laranja 2 L", "Refrigerantes", new BigDecimal("7.29")),
            new ProdutoDTO("MOCK-00031", "Refrigerante Fruki Laranja 3 L", "Refrigerantes", new BigDecimal("9.79")),
            new ProdutoDTO("MOCK-00032", "Refrigerante Fruki Laranja 600 ml", "Refrigerantes", new BigDecimal("4.19")),
            new ProdutoDTO("MOCK-00033", "Refrigerante Fruki Laranja 350 ml", "Refrigerantes", new BigDecimal("3.89")),
            new ProdutoDTO("MOCK-00034", "Refrigerante Fruki Laranja 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00035", "Refrigerante Fruki Lim\u00e3o 2 L", "Refrigerantes", new BigDecimal("7.29")),
            new ProdutoDTO("MOCK-00036", "Refrigerante Fruki Lim\u00e3o 600 ml", "Refrigerantes", new BigDecimal("4.19")),
            new ProdutoDTO("MOCK-00037", "Refrigerante Fruki Lim\u00e3o 600 ml vidro", "Refrigerantes", new BigDecimal("5.49")),
            new ProdutoDTO("MOCK-00038", "Refrigerante Fruki Lim\u00e3o 350 ml", "Refrigerantes", new BigDecimal("3.89")),
            new ProdutoDTO("MOCK-00039", "Refrigerante Fruki Lim\u00e3o 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00040", "Refrigerante Fruki Laranjinha 2 L", "Refrigerantes", new BigDecimal("7.29")),
            new ProdutoDTO("MOCK-00041", "Refrigerante Fruki Laranjinha 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00042", "Refrigerante Fruki Uva 2 L", "Refrigerantes", new BigDecimal("7.29")),
            new ProdutoDTO("MOCK-00043", "Refrigerante Fruki Uva 600 ml", "Refrigerantes", new BigDecimal("4.19")),
            new ProdutoDTO("MOCK-00044", "Refrigerante Fruki Uva 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00045", "Refrigerante Fruki Framboesa 2 L", "Refrigerantes", new BigDecimal("7.29")),
            new ProdutoDTO("MOCK-00046", "Refrigerante Fruki Framboesa 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00047", "Refrigerante Fruki Passion Fruit Zero 350 ml", "Refrigerantes", new BigDecimal("3.99")),
            new ProdutoDTO("MOCK-00048", "Refrigerante Fruki Guaran\u00e1 Zero 600 ml vidro", "Refrigerantes", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00049", "Refrigerante Fruki Cola 200 ml", "Refrigerantes", new BigDecimal("2.19")),
            new ProdutoDTO("MOCK-00002", "\u00c1gua da Pedra sem g\u00e1s 500 ml", "\u00c1guas", new BigDecimal("2.29")),
            new ProdutoDTO("MOCK-00008", "\u00c1gua da Pedra com g\u00e1s 500 ml", "\u00c1guas", new BigDecimal("2.49")),
            new ProdutoDTO("MOCK-00050", "\u00c1gua da Pedra sem g\u00e1s 1,5 L", "\u00c1guas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00051", "\u00c1gua da Pedra sem g\u00e1s 2 L", "\u00c1guas", new BigDecimal("4.29")),
            new ProdutoDTO("MOCK-00052", "\u00c1gua da Pedra sem g\u00e1s 5 L", "\u00c1guas", new BigDecimal("7.49")),
            new ProdutoDTO("MOCK-00053", "\u00c1gua da Pedra com g\u00e1s 2 L", "\u00c1guas", new BigDecimal("4.49")),
            new ProdutoDTO("MOCK-00054", "\u00c1gua da Pedra sem g\u00e1s lata 350 ml", "\u00c1guas", new BigDecimal("2.99")),
            new ProdutoDTO("MOCK-00055", "\u00c1gua da Pedra com g\u00e1s lata 350 ml", "\u00c1guas", new BigDecimal("2.99")),
            new ProdutoDTO("MOCK-00056", "\u00c1gua da Pedra Blueberry com Lim\u00e3o Siciliano 350 ml", "\u00c1guas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00057", "\u00c1gua da Pedra Abacaxi com Hortel\u00e3 350 ml", "\u00c1guas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00058", "\u00c1gua da Pedra Frutas Vermelhas 350 ml", "\u00c1guas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00059", "\u00c1gua da Pedra Lim\u00e3o com Alecrim 350 ml", "\u00c1guas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00060", "\u00c1gua da Pedra Bergamota com Capim-Lim\u00e3o 350 ml", "\u00c1guas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00061", "\u00c1gua da Pedra P\u00eassego 350 ml", "\u00c1guas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00003", "Energ\u00e9tico Elev Original 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00070", "Energ\u00e9tico Elev Original 269 ml", "Energ\u00e9ticos", new BigDecimal("4.49")),
            new ProdutoDTO("MOCK-00071", "Energ\u00e9tico Elev Zero 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00072", "Energ\u00e9tico Elev Zero 269 ml", "Energ\u00e9ticos", new BigDecimal("4.49")),
            new ProdutoDTO("MOCK-00073", "Energ\u00e9tico Elev Frutas Tropicais 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00074", "Energ\u00e9tico Elev Frutas Tropicais 269 ml", "Energ\u00e9ticos", new BigDecimal("4.49")),
            new ProdutoDTO("MOCK-00075", "Energ\u00e9tico Elev Ma\u00e7\u00e3 Verde 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00076", "Energ\u00e9tico Elev Ma\u00e7\u00e3 Verde 269 ml", "Energ\u00e9ticos", new BigDecimal("4.49")),
            new ProdutoDTO("MOCK-00077", "Energ\u00e9tico Elev Guaran\u00e1 com Maracuj\u00e1 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00078", "Energ\u00e9tico Elev Guaran\u00e1 com Maracuj\u00e1 269 ml", "Energ\u00e9ticos", new BigDecimal("4.49")),
            new ProdutoDTO("MOCK-00079", "Energ\u00e9tico Elev Coco com A\u00e7a\u00ed 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00080", "Energ\u00e9tico Elev Coco com A\u00e7a\u00ed 269 ml", "Energ\u00e9ticos", new BigDecimal("4.49")),
            new ProdutoDTO("MOCK-00081", "Energ\u00e9tico Elev Morango com Melancia 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00082", "Energ\u00e9tico Elev Pink Lemonade 473 ml", "Energ\u00e9ticos", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00083", "Energ\u00e9tico Elev Pink Lemonade 2 L", "Energ\u00e9ticos", new BigDecimal("13.99")),
            new ProdutoDTO("MOCK-00084", "Energ\u00e9tico Elev Coco com A\u00e7a\u00ed 2 L", "Energ\u00e9ticos", new BigDecimal("13.99")),
            new ProdutoDTO("MOCK-00085", "Energ\u00e9tico Elev Morango com Melancia 2 L", "Energ\u00e9ticos", new BigDecimal("13.99")),
            new ProdutoDTO("MOCK-00086", "Energ\u00e9tico Elev Ma\u00e7\u00e3 Verde 2 L", "Energ\u00e9ticos", new BigDecimal("13.99")),
            new ProdutoDTO("MOCK-00004", "Cerveja Bellavista Pilsen 473 ml", "Cervejas", new BigDecimal("5.90")),
            new ProdutoDTO("MOCK-00090", "Cerveja Bellavista Premium Lager 473 ml", "Cervejas", new BigDecimal("6.49")),
            new ProdutoDTO("MOCK-00091", "Cerveja Bellavista Premium Lager 355 ml", "Cervejas", new BigDecimal("5.49")),
            new ProdutoDTO("MOCK-00092", "Cerveja Bellavista IPA 350 ml", "Cervejas", new BigDecimal("6.99")),
            new ProdutoDTO("MOCK-00093", "Cerveja Bellavista Session IPA 350 ml", "Cervejas", new BigDecimal("5.99")),
            new ProdutoDTO("MOCK-00094", "Cerveja Bellavista Zero 350 ml", "Cervejas", new BigDecimal("5.49")),
            new ProdutoDTO("MOCK-00010", "Refresco Frukito Uva 2 L", "Refrescos", new BigDecimal("5.49")),
            new ProdutoDTO("MOCK-00100", "Refresco Frukito Uva 500 ml", "Refrescos", new BigDecimal("3.29")),
            new ProdutoDTO("MOCK-00101", "Refresco Frukito Uva 200 ml", "Refrescos", new BigDecimal("1.99")),
            new ProdutoDTO("MOCK-00102", "Refresco Frukito Frutas C\u00edtricas 2 L", "Refrescos", new BigDecimal("5.49")),
            new ProdutoDTO("MOCK-00103", "Refresco Frukito Frutas C\u00edtricas 500 ml", "Refrescos", new BigDecimal("3.29")),
            new ProdutoDTO("MOCK-00104", "Refresco Frukito Frutas C\u00edtricas 200 ml", "Refrescos", new BigDecimal("1.99")),
            new ProdutoDTO("MOCK-00105", "Refresco Frukito Lim\u00e3o 2 L", "Refrescos", new BigDecimal("5.49")),
            new ProdutoDTO("MOCK-00106", "Refresco Frukito Lim\u00e3o 500 ml", "Refrescos", new BigDecimal("3.29")),
            new ProdutoDTO("MOCK-00107", "Refresco Frukito Lim\u00e3o 200 ml", "Refrescos", new BigDecimal("1.99")),
            new ProdutoDTO("MOCK-00005", "Suco COM/TEM Laranja 1 L", "Sucos", new BigDecimal("12.90")),
            new ProdutoDTO("MOCK-00110", "Suco COM/TEM Laranja 1 L vidro", "Sucos", new BigDecimal("14.90")),
            new ProdutoDTO("MOCK-00111", "Suco COM/TEM Laranja 200 ml", "Sucos", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00112", "Suco COM/TEM Uva 1 L vidro", "Sucos", new BigDecimal("14.90")),
            new ProdutoDTO("MOCK-00113", "Suco COM/TEM Uva 200 ml", "Sucos", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00114", "Suco COM/TEM Ma\u00e7\u00e3 1 L", "Sucos", new BigDecimal("12.90")),
            new ProdutoDTO("MOCK-00115", "Suco COM/TEM Verde 1 L vidro", "Sucos", new BigDecimal("14.90")),
            new ProdutoDTO("MOCK-00009", "\u00c1gua T\u00f4nica Fruki 350 ml", "T\u00f4nicas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00120", "\u00c1gua T\u00f4nica Fruki 1,5 L", "T\u00f4nicas", new BigDecimal("6.49")),
            new ProdutoDTO("MOCK-00121", "\u00c1gua T\u00f4nica Fruki Zero 350 ml", "T\u00f4nicas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00122", "\u00c1gua T\u00f4nica Fruki Zero 1,5 L", "T\u00f4nicas", new BigDecimal("6.49")),
            new ProdutoDTO("MOCK-00123", "Fruki Citrus 350 ml", "T\u00f4nicas", new BigDecimal("3.49")),
            new ProdutoDTO("MOCK-00124", "Fruki Citrus 1,5 L", "T\u00f4nicas", new BigDecimal("6.49"))
    );

    public List<ProdutoDTO> produtosDemo(int limite) {
        return PRODUTOS_DEMO.subList(0, Math.min(Math.max(limite, 1), PRODUTOS_DEMO.size()));
    }

    public List<HistoricoCompraDTO> historico(int limite) {
        LocalDateTime agora = LocalDateTime.now();
        List<HistoricoCompraDTO> demo = List.of(
                new HistoricoCompraDTO(-1L, agora.minusDays(7), new BigDecimal("186.40"), "Pedido concluído",
                        "12x Refrigerante Fruki Guaraná 2 L · 8x Água Mineral Fruki 500 ml", true),
                new HistoricoCompraDTO(-2L, agora.minusDays(21), new BigDecimal("124.80"), "Pedido concluído",
                        "8x Refrigerante Fruki Guaraná 2 L · 8x Energético Elev 473 ml", true),
                new HistoricoCompraDTO(-3L, agora.minusDays(35), new BigDecimal("98.70"), "Pedido concluído",
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
        List<FeriadoDTO> proximos = feriadoService.proximos(cidade, 30);
        FeriadoDTO proximo = proximos.isEmpty() ? null : proximos.get(0);
        return new ContextoDTO(cidade == null ? "Porto Alegre" : cidade, uf == null ? "RS" : uf,
                hoje, estacao, estacao.getDescricao(), 21.0, "DEMONSTRAÇÃO", proximo != null,
                "Contexto demonstrativo · Sazonalidade: " + estacao.getDescricao()
                        + (proximo == null ? "" : " · Próximo feriado: " + proximo.name()), proximos);
    }

    private ItemRecomendadoDTO item(ProdutoDTO produto, int posicao, double score, boolean demo) {
        String motivoSazonal = "Sazonalidade: " + Estacao.de(LocalDate.now()).getDescricao();
        return new ItemRecomendadoDTO(posicao, produto.codItem(), produto.nome(), produto.categoria(), produto.preco(),
                Math.max(70.0, score), List.of("Alta saída no seu perfil", motivoSazonal),
                Map.of("rfm", .82, "regional", .78, "sazonalidade", .86, "margem", .72, "penalidade", 0.0), demo);
    }
}
