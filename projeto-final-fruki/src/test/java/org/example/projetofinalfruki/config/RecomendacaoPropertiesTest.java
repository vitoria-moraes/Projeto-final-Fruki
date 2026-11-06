package org.example.projetofinalfruki.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Garante que os pesos do application.yml chegam ao objeto de configuração. */
class RecomendacaoPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues(
                    "app.recomendacao.pesos.rfm=0.60",
                    "app.recomendacao.diversidade.max-por-categoria=3",
                    "app.recomendacao.janela-compra-recente-dias=5");

    @Test
    @DisplayName("faz binding dos pesos e limites")
    void fazBinding() {
        runner.run(contexto -> {
            RecomendacaoProperties props = contexto.getBean(RecomendacaoProperties.class);
            assertEquals(0.60, props.getPesos().getRfm());
            assertEquals(3, props.getDiversidade().getMaxPorCategoria());
            assertEquals(5, props.getJanelaCompraRecenteDias());
        });
    }

    @EnableConfigurationProperties(RecomendacaoProperties.class)
    static class Config {
    }
}
