package org.example.projetofinalfruki.infrastructure.db.supabase.databricks;

import org.example.projetofinalfruki.domain.exception.DatabricksIndisponivelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.regex.Pattern;

/** Consultas agregadas na Gold para os indicadores da solução. */
@Repository
public class IndicadoresRepository {

    private static final Logger log = LoggerFactory.getLogger(IndicadoresRepository.class);
    private static final Pattern TABELA_VALIDA =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*){0,2}$");

    private final NamedParameterJdbcTemplate jdbc;
    private final String tabela;

    public IndicadoresRepository(@Qualifier("databricksJdbcTemplate") NamedParameterJdbcTemplate jdbc,
                                 @Value("${app.databricks.recomendacao-table:fruki.gold.gold_recomendacao_b2b}") String tabela) {
        if (!TABELA_VALIDA.matcher(tabela).matches()) {
            throw new IllegalArgumentException("Nome de tabela inválido em app.databricks.recomendacao-table: " + tabela);
        }
        this.jdbc = jdbc;
        this.tabela = tabela;
    }

    public Resumo resumo() {
        String sql = "SELECT COUNT(DISTINCT id_cliente) AS clientes, "
                + "COUNT(*) AS total, "
                + "COUNT(DISTINCT cod_item) AS itens "
                + "FROM " + tabela;
        try {
            return jdbc.queryForObject(sql, Map.of(), (rs, i) -> new Resumo(
                    rs.getLong("clientes"), rs.getLong("total"), rs.getLong("itens")));
        } catch (DataAccessException e) {
            log.error("Falha ao calcular indicadores na tabela {}", tabela, e);
            throw new DatabricksIndisponivelException(
                    "Não foi possível calcular os indicadores no Databricks agora.", e);
        }
    }

    /** Quantos clientes têm pelo menos {@code minimo} itens recomendados. */
    public long clientesComPeloMenos(int minimo) {
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT id_cliente FROM " + tabela
                + " GROUP BY id_cliente HAVING COUNT(*) >= :minimo) t";
        try {
            Long valor = jdbc.queryForObject(sql,
                    new MapSqlParameterSource("minimo", Math.max(1, minimo)),
                    Long.class);
            return valor == null ? 0L : valor;
        } catch (DataAccessException e) {
            log.error("Falha ao calcular clientes com pelo menos {} itens", minimo, e);
            throw new DatabricksIndisponivelException(
                    "Não foi possível calcular os indicadores no Databricks agora.", e);
        }
    }

    public record Resumo(long clientesComRecomendacao, long totalRecomendacoes, long itensDistintos) {
    }
}
