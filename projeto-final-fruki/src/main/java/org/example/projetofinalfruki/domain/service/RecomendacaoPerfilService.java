package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.domain.exception.DatabricksIndisponivelException;
import org.example.projetofinalfruki.infrastructure.db.supabase.databricks.RecomendacaoRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.PedidoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.PedidoJpaRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.TabelaPrecoJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Motor de recomendação por perfil, em 3 camadas (da mais para a menos personalizada):
 *  1. Databricks (score por cliente/item), se disponível;
 *  2. venda cruzada por regra, a partir do histórico real (fato_vendas -> resumo_itens);
 *  3. mix-base do perfil (pequeno varejo x grande varejo).
 * O contexto sazonal (clima/feriado) vem do MotorSazonalidadeService.
 */
@Service
public class RecomendacaoPerfilService {

    private static final Logger log = LoggerFactory.getLogger(RecomendacaoPerfilService.class);

    public enum Perfil {
        PEQUENO_VAREJO("pequeno varejo"),
        GRANDE_VAREJO("grande varejo / supermercado");

        private final String rotulo;
        Perfil(String rotulo) { this.rotulo = rotulo; }
        public String getRotulo() { return rotulo; }
    }

    public record Resultado(Perfil perfil, List<String> sugestoes, String contexto) {}

    /** Ticket médio a partir do qual o cliente é tratado como grande varejo (ajustar com a área comercial). */
    private static final BigDecimal TICKET_GRANDE_VAREJO = new BigDecimal("5000");

    private final PedidoJpaRepository pedidoRepository;
    private final TabelaPrecoJpaRepository tabelaPrecoRepository;
    private final RecomendacaoRepository databricksRepository;
    private final MotorSazonalidadeService sazonalidade;

    public RecomendacaoPerfilService(PedidoJpaRepository pedidoRepository,
                                     TabelaPrecoJpaRepository tabelaPrecoRepository,
                                     RecomendacaoRepository databricksRepository,
                                     MotorSazonalidadeService sazonalidade) {
        this.pedidoRepository = pedidoRepository;
        this.tabelaPrecoRepository = tabelaPrecoRepository;
        this.databricksRepository = databricksRepository;
        this.sazonalidade = sazonalidade;
    }

    public Resultado recomendar(ClienteEntity cliente) {
        List<PedidoEntity> historico;
        try {
            historico = pedidoRepository.findByCodCliente(String.valueOf(cliente.getCodCliente()));
        } catch (DataAccessException e) {
            // Se fato_vendas estiver com schema divergente/fora, o bot continua com as regras.
            log.warn("Historico de fato_vendas indisponivel ({}). Seguindo com as regras.",
                    e.getClass().getSimpleName());
            historico = List.of();
        }
        Perfil perfil = classificar(historico);

        Set<String> sugestoes = new LinkedHashSet<>();
        sugestoes.addAll(topDoDatabricks(cliente));
        sugestoes.addAll(vendaCruzada(historico));
        sugestoes.addAll(mixBase(perfil));

        return new Resultado(perfil,
                sugestoes.stream().limit(4).toList(),
                sazonalidade.gerarRecomendacaoParaCidade(cliente.getCidade()));
    }

    public String formatarWhatsApp(Resultado r) {
        StringBuilder sb = new StringBuilder("🎯 *Recomendações para o seu perfil* (")
                .append(r.perfil().getRotulo()).append(")\n");
        int i = 1;
        for (String s : r.sugestoes()) sb.append(i++).append(". ").append(s).append("\n");
        return sb.append("\n").append(r.contexto()).append("\n\nDigite *menu* para voltar.").toString();
    }

    Perfil classificar(List<PedidoEntity> historico) {
        List<BigDecimal> valores = historico.stream().map(PedidoEntity::getValorTotal).filter(Objects::nonNull).toList();
        if (valores.isEmpty()) return Perfil.PEQUENO_VAREJO;
        BigDecimal soma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal media = soma.divide(BigDecimal.valueOf(valores.size()), 2, java.math.RoundingMode.HALF_UP);
        return media.compareTo(TICKET_GRANDE_VAREJO) >= 0 ? Perfil.GRANDE_VAREJO : Perfil.PEQUENO_VAREJO;
    }

    List<String> vendaCruzada(List<PedidoEntity> historico) {
        String texto = historico.stream().map(PedidoEntity::getResumoItens).filter(Objects::nonNull)
                .collect(Collectors.joining(" ")).toLowerCase(Locale.ROOT);
        boolean refri = texto.contains("fruki") || texto.contains("guaran");
        boolean agua = texto.contains("pedra") || texto.contains("água") || texto.contains("agua");

        List<String> regras = new ArrayList<>();
        if (refri && !agua) regras.add("Água da Pedra 500ml — complementa os refrigerantes Fruki e gira rápido");
        if (agua && !refri) regras.add("Fruki Guaraná 2L — âncora do mix junto com a Água da Pedra");
        if (!texto.contains("elev")) regras.add("Elev Energy Drink 473ml — boa margem no PDV");
        return regras;
    }

    private List<String> mixBase(Perfil perfil) {
        return switch (perfil) {
            case PEQUENO_VAREJO -> List.of("Fruki Guaraná 2L", "Água da Pedra 500ml", "Elev Energy Drink 473ml");
            case GRANDE_VAREJO -> List.of("Fruki Guaraná 2L (volume por pallet)", "Fruki Guaraná com Bergamota 2L",
                    "Água da Pedra 500ml (c/ 12)", "Cerveja Bellavista 473ml");
        };
    }

    /** Databricks fora do ar nunca derruba o bot: cai nas regras. */
    private List<String> topDoDatabricks(ClienteEntity cliente) {
        Long id = cliente.getCodCliente();
        if (id == null) {
            return List.of();
        }
        try {
            return databricksRepository.findByIdClienteOrderByScoreRecomendacaoDesc(id).stream()
                    .limit(3)
                    .map(r -> tabelaPrecoRepository.findByCodProduto(r.getCodItem())
                            .map(TabelaPrecoEntity::getNomeProduto).orElse("Item " + r.getCodItem()))
                    .toList();
        } catch (DatabricksIndisponivelException | DataAccessException e) {
            return List.of();
        }
    }
}
