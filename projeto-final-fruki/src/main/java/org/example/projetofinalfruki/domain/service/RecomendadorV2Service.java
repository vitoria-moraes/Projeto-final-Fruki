package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.ItemRecomendadoDTO;
import org.example.projetofinalfruki.config.RecomendacaoProperties;
import org.example.projetofinalfruki.domain.model.CategoriaProduto;
import org.example.projetofinalfruki.domain.model.PerfilRegional;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.example.projetofinalfruki.domain.model.RecomendacaoB2B;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Recomendador V2 — híbrido e explicável.
 *
 * <p>Fórmula (0..100):
 * <pre>
 * score = 100 * ( wRFM * RFM + wREG * regional + wSAZ * sazonalidade + wMAR * margem )
 *         - penalidades
 * </pre>
 *
 * <p>Pilares inspirados em motores B2B como o da Coca-Cola: histórico de compra (RFM),
 * aderência regional/segmento, sazonalidade/contexto, valor do mix (margem/giro) e
 * regras de negócio (diversidade, compra recente, preço fora da curva).
 * O que não existe na base (matriz usuário-item densa, margem real, estoque) é
 * substituído por heurísticas documentadas em docs/RECOMENDADOR.md.
 */
@Service
public class RecomendadorV2Service {

    private final CatalogoService catalogo;
    private final RecomendacaoProperties props;

    public RecomendadorV2Service(CatalogoService catalogo, RecomendacaoProperties props) {
        this.catalogo = catalogo;
        this.props = props;
    }

    public List<ItemRecomendadoDTO> recomendar(List<RecomendacaoB2B> historico,
                                               ClienteEntity cliente,
                                               ContextoDTO contexto,
                                               int limite) {
        Objects.requireNonNull(contexto, "contexto é obrigatório");
        int limiteSeguro = Math.max(1, Math.min(limite, 50));
        boolean coldStart = historico == null || historico.isEmpty();

        PerfilRegional perfil = PerfilRegional.fromCidade(
                cliente != null ? cliente.getCidade() : null,
                cliente != null ? cliente.getUf() : null);

        List<Candidato> candidatos = montarCandidatos(historico, coldStart);
        if (candidatos.isEmpty()) {
            return List.of();
        }

        double maxValor = candidatos.stream()
                .map(Candidato::dados)
                .filter(Objects::nonNull)
                .map(RecomendacaoB2B::getValorTotalGasto)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .max().orElse(0.0);
        double maxQtd = candidatos.stream()
                .map(Candidato::dados)
                .filter(Objects::nonNull)
                .map(RecomendacaoB2B::getTotalQuantidadeComprada)
                .filter(Objects::nonNull)
                .mapToDouble(Long::doubleValue)
                .max().orElse(0.0);

        Map<CategoriaProduto, Double> medianaPorCategoria = medianas(candidatos);

        List<Avaliado> avaliados = new ArrayList<>(candidatos.size());
        for (Candidato c : candidatos) {
            avaliados.add(avaliar(c, perfil, contexto, maxValor, maxQtd,
                    medianaPorCategoria.get(c.categoria()), coldStart));
        }

        avaliados.sort(Comparator
                .comparingDouble(Avaliado::score).reversed()
                .thenComparing(Comparator.comparingDouble(Avaliado::rfm).reversed())
                .thenComparing(a -> a.candidato().codItem()));

        Map<CategoriaProduto, Integer> usados = new EnumMap<>(CategoriaProduto.class);
        List<ItemRecomendadoDTO> saida = new ArrayList<>();
        for (Avaliado a : avaliados) {
            if (saida.size() >= limiteSeguro) {
                break;
            }
            int qtd = usados.getOrDefault(a.candidato().categoria(), 0);
            if (qtd >= props.getDiversidade().getMaxPorCategoria()) {
                continue;
            }
            usados.put(a.candidato().categoria(), qtd + 1);
            Candidato c = a.candidato();
            saida.add(new ItemRecomendadoDTO(
                    saida.size() + 1,
                    c.codItem(),
                    c.produto() != null && c.produto().getNomeProduto() != null
                            ? c.produto().getNomeProduto()
                            : "Item " + c.codItem(),
                    c.categoria().getDescricao(),
                    c.produto() != null ? c.produto().getPrecoBase() : null,
                    arredondar(a.score(), 1),
                    a.motivos(),
                    detalhe(a),
                    false
            ));
        }
        return saida;
    }

    private List<Candidato> montarCandidatos(List<RecomendacaoB2B> historico, boolean coldStart) {
        Map<String, Candidato> unicos = new LinkedHashMap<>();
        if (coldStart) {
            for (TabelaPrecoEntity produto : catalogo.listarProdutos(60)) {
                if (produto == null || produto.getCodProduto() == null) {
                    continue;
                }
                CategoriaProduto categoria = catalogo.categoriaDe(produto);
                unicos.putIfAbsent(produto.getCodProduto(),
                        new Candidato(produto.getCodProduto(), null, produto, categoria));
            }
            return new ArrayList<>(unicos.values());
        }

        for (RecomendacaoB2B linha : historico) {
            if (linha == null || linha.getCodItem() == null) {
                continue;
            }
            Optional<TabelaPrecoEntity> produto = catalogo.buscarProduto(linha.getCodItem());
            CategoriaProduto categoria = catalogo.categoriaDe(produto.orElse(null));
            unicos.putIfAbsent(linha.getCodItem(),
                    new Candidato(linha.getCodItem(), linha, produto.orElse(null), categoria));
        }
        return new ArrayList<>(unicos.values());
    }

    private Avaliado avaliar(Candidato c, PerfilRegional perfil, ContextoDTO contexto,
                             double maxValor, double maxQtd, Double mediana, boolean coldStart) {
        RecomendacaoB2B dados = c.dados();
        double rfm = coldStart ? 0.50 : calcularRfm(dados, maxValor, maxQtd);
        double regional = perfil.aderencia(c.categoria());
        double sazonalidade = ajustarSazonalidade(contexto, c.categoria());
        double margem = c.categoria().getMargemProxy();

        double penalidade = 0.0;
        List<String> motivos = new ArrayList<>();

        Integer dias = diasDesdeUltimaCompra(dados);
        if (dados != null && dias != null && dias <= props.getJanelaCompraRecenteDias()) {
            penalidade += props.getPenalidades().getCompradoRecente();
            motivos.add("penalizado: comprado há " + dias + " dia(s)");
        }
        BigDecimal preco = c.produto() != null ? c.produto().getPrecoBase() : null;
        if (preco == null) {
            penalidade += props.getPenalidades().getDadoIncompleto();
            motivos.add("preço não encontrado na tabela");
        } else if (mediana != null && mediana > 0 && preco.doubleValue() > mediana * 2.0) {
            penalidade += props.getPenalidades().getPrecoOutlier();
            motivos.add("preço acima da média da categoria");
        }

        RecomendacaoProperties.Pesos pesos = props.getPesos();
        double bruto = pesos.getRfm() * rfm
                + pesos.getRegional() * regional
                + pesos.getSazonalidade() * sazonalidade
                + pesos.getMargem() * margem;
        double score = limitar(bruto * 100.0 - penalidade, 0.0, 100.0);

        if (coldStart) {
            motivos.add("sem histórico: seleção por perfil regional");
        } else if (dados != null) {
            Long frequencia = dados.getFrequenciaPedidos();
            if (frequencia != null && frequencia >= 5) {
                motivos.add("compra recorrente (" + frequencia + " pedidos)");
            }
            if (dias != null && dias <= 30) {
                motivos.add("compra recente (há " + dias + " dias)");
            }
            if (dados.getValorTotalGasto() != null
                    && dados.getValorTotalGasto().compareTo(new BigDecimal("500")) >= 0) {
                motivos.add("alto valor histórico");
            }
        }
        motivos.add("perfil " + perfil.getDescricao());
        motivos.add("estação: " + contexto.estacao().getDescricao());
        if (contexto.feriadoProximo()) {
            motivos.add("feriado próximo: antecipe o pedido");
        }
        if (margem >= 0.80) {
            motivos.add("boa margem/giro");
        }

        List<String> motivosFinal = new ArrayList<>(new LinkedHashSet<>(motivos));
        if (motivosFinal.size() > 4) {
            motivosFinal = motivosFinal.subList(0, 4);
        }
        return new Avaliado(c, score, rfm, regional, sazonalidade, margem, penalidade, motivosFinal);
    }

    private static double calcularRfm(RecomendacaoB2B dados, double maxValor, double maxQtd) {
        if (dados == null) {
            return 0.5;
        }
        Integer dias = diasDesdeUltimaCompra(dados);
        double recencia = dias == null ? 0.0 : 1.0 / (1.0 + dias / 30.0);
        double frequencia = Math.min(1.0, valorOuZero(dados.getFrequenciaPedidos()) / 10.0);
        double monetario = maxValor <= 0 ? 0.0
                : Math.log1p(valorOuZero(dados.getValorTotalGasto())) / Math.log1p(maxValor);
        double quantidade = maxQtd <= 0 ? 0.0
                : Math.log1p(valorOuZero(dados.getTotalQuantidadeComprada())) / Math.log1p(maxQtd);
        return 0.35 * recencia + 0.30 * frequencia + 0.20 * monetario + 0.15 * quantidade;
    }

    private static double ajustarSazonalidade(ContextoDTO contexto, CategoriaProduto categoria) {
        double fator = contexto.estacao().fator(categoria);
        if (contexto.temperatura() >= 28.0
                && (categoria == CategoriaProduto.AGUAS
                || categoria == CategoriaProduto.ENERGETICOS
                || categoria == CategoriaProduto.CERVEJAS)) {
            fator += 0.08;
        }
        if (contexto.temperatura() <= 12.0
                && (categoria == CategoriaProduto.SUCOS || categoria == CategoriaProduto.REFRIGERANTES)) {
            fator += 0.08;
        }
        if (contexto.feriadoProximo()
                && (categoria == CategoriaProduto.REFRIGERANTES
                || categoria == CategoriaProduto.CERVEJAS
                || categoria == CategoriaProduto.AGUAS)) {
            fator += 0.06;
        }
        return limitar(fator, 0.0, 1.0);
    }

    private static Map<CategoriaProduto, Double> medianas(List<Candidato> candidatos) {
        Map<CategoriaProduto, List<Double>> valores = new EnumMap<>(CategoriaProduto.class);
        for (Candidato c : candidatos) {
            if (c.produto() != null && c.produto().getPrecoBase() != null) {
                valores.computeIfAbsent(c.categoria(), k -> new ArrayList<>())
                        .add(c.produto().getPrecoBase().doubleValue());
            }
        }
        Map<CategoriaProduto, Double> medianas = new EnumMap<>(CategoriaProduto.class);
        valores.forEach((categoria, lista) -> {
            lista.sort(Double::compareTo);
            int meio = lista.size() / 2;
            double mediana = lista.size() % 2 == 0
                    ? (lista.get(meio - 1) + lista.get(meio)) / 2.0
                    : lista.get(meio);
            medianas.put(categoria, mediana);
        });
        return medianas;
    }

    private static Integer diasDesdeUltimaCompra(RecomendacaoB2B dados) {
        if (dados == null || dados.getUltimaDataCompra() == null) {
            return null;
        }
        long dias = ChronoUnit.DAYS.between(dados.getUltimaDataCompra(), LocalDate.now());
        return (int) Math.max(0, dias);
    }

    private static double valorOuZero(Number numero) {
        return numero == null ? 0.0 : numero.doubleValue();
    }

    private static double limitar(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }

    private static double arredondar(double valor, int casas) {
        return BigDecimal.valueOf(valor).setScale(casas, RoundingMode.HALF_UP).doubleValue();
    }

    private static Map<String, Double> detalhe(Avaliado a) {
        Map<String, Double> detalhe = new LinkedHashMap<>();
        detalhe.put("rfm", arredondar(a.rfm(), 3));
        detalhe.put("regional", arredondar(a.regional(), 3));
        detalhe.put("sazonalidade", arredondar(a.sazonalidade(), 3));
        detalhe.put("margem", arredondar(a.margem(), 3));
        detalhe.put("penalidade", arredondar(a.penalidade(), 3));
        return detalhe;
    }

    private record Candidato(String codItem, RecomendacaoB2B dados,
                             TabelaPrecoEntity produto, CategoriaProduto categoria) {
    }

    private record Avaliado(Candidato candidato, double score, double rfm, double regional,
                            double sazonalidade, double margem, double penalidade,
                            List<String> motivos) {
    }
}
