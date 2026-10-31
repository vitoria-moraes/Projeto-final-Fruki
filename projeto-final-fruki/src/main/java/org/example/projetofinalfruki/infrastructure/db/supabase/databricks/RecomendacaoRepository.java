package org.example.projetofinalfruki.infrastructure.db.supabase.databricks;

import org.example.projetofinalfruki.domain.model.RecomendacaoB2B;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Leitura da tabela gold de recomendações no Databricks (JDBC puro, somente SELECT).
 * Mantém o mesmo nome de método do repositório antigo, então o controller não muda.
 */
@Repository
public class RecomendacaoRepository {

    /** Falha de comunicação com o Databricks (warehouse parado, token inválido, rede...). */
    public static class DatabricksIndisponivelException extends RuntimeException {
        public DatabricksIndisponivelException(String mensagem, Throwable causa) { super(mensagem, causa); }
    }

    // catalog.schema.tabela: nome de tabela não pode ser parâmetro SQL, então validamos o formato
    private static final Pattern TABELA_VALIDA =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*){0,2}$");

    private static final int LIMITE_PADRAO = 50;

    private final NamedParameterJdbcTemplate jdbc;
    private final String sql;

    public RecomendacaoRepository(@Qualifier("databricksJdbcTemplate") NamedParameterJdbcTemplate jdbc,
                                  @Value("${app.databricks.recomendacao-table:fruki.gold.gold_recomendacao_b2b}") String tabela) {
        if (!TABELA_VALIDA.matcher(tabela).matches()) {
            throw new IllegalArgumentException("Nome de tabela inválido em app.databricks.recomendacao-table: " + tabela);
        }
        this.jdbc = jdbc;
        this.sql = "SELECT id_cliente, cod_item, total_quantidade_comprada, valor_total_gasto, "
                + "ultima_data_compra, frequencia_pedidos, score_recomendacao "
                + "FROM " + tabela + " WHERE id_cliente = :idCliente "
                + "ORDER BY score_recomendacao DESC LIMIT " + LIMITE_PADRAO;
    }

    public List<RecomendacaoB2B> findByIdClienteOrderByScoreRecomendacaoDesc(Long idCliente) {
        try {
            return jdbc.query(sql, new MapSqlParameterSource("idCliente", idCliente), MAPPER);
        } catch (DataAccessException e) {
            throw new DatabricksIndisponivelException(
                    "Não foi possível consultar as recomendações no Databricks agora.", e);
        }
    }

    private static final RowMapper<RecomendacaoB2B> MAPPER = (ResultSet rs, int linha) -> {
        RecomendacaoB2B r = new RecomendacaoB2B();
        r.setIdCliente(longOuNulo(rs, "id_cliente"));
        r.setCodItem(rs.getString("cod_item"));
        r.setTotalQuantidadeComprada(longOuNulo(rs, "total_quantidade_comprada"));
        r.setValorTotalGasto(rs.getBigDecimal("valor_total_gasto"));
        Date ultima = rs.getDate("ultima_data_compra");
        r.setUltimaDataCompra(ultima == null ? null : ultima.toLocalDate());
        r.setFrequenciaPedidos(longOuNulo(rs, "frequencia_pedidos"));
        double score = rs.getDouble("score_recomendacao");
        r.setScoreRecomendacao(rs.wasNull() ? null : score);
        return r;
    };

    private static Long longOuNulo(ResultSet rs, String coluna) throws SQLException {
        long v = rs.getLong(coluna);
        return rs.wasNull() ? null : v;
    }
}