package org.example.projetofinalfruki.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Databricks = camada ANALÍTICA, somente leitura. Por isso usa JDBC puro (JdbcTemplate), SEM JPA:
 *
 *  - Hibernate precisa descobrir o dialeto conectando no boot (acordaria o warehouse e falharia no timeout);
 *  - o JpaTransactionManager chama setAutoCommit(false)/setReadOnly(true), que o driver do Databricks
 *    não suporta (erro ou comportamento indefinido, conforme a versão do driver);
 *  - a tabela gold tem várias linhas por cliente: com @Id só em id_cliente o Hibernate devolve a MESMA
 *    linha repetida (cache de 1º nível por id) e esconde o resto dos dados.
 *
 * Sem EntityManagerFactory nem TransactionManager aqui, também desaparece a ambiguidade de beans.
 */
@Configuration
public class DatabricksConfig {

    @Bean(name = "databricksDataSource", destroyMethod = "close")
    @ConfigurationProperties(prefix = "databricks.datasource")
    public HikariDataSource databricksDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "databricksJdbcTemplate")
    public NamedParameterJdbcTemplate databricksJdbcTemplate(@Qualifier("databricksDataSource") DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setQueryTimeout(60);   // segundos: não deixa uma consulta travar a thread do Tomcat
        template.setFetchSize(1000);
        return new NamedParameterJdbcTemplate(template);
    }
}