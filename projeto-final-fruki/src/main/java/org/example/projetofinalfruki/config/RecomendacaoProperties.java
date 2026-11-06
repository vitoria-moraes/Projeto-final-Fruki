package org.example.projetofinalfruki.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Pesos e limites do recomendador V2.
 * Ficam em application.yml (app.recomendacao.*) para permitir ajuste sem recompilar.
 */
@Component
@ConfigurationProperties(prefix = "app.recomendacao")
@Getter
@Setter
public class RecomendacaoProperties {

    private int limitePadrao = 10;
    private int janelaCompraRecenteDias = 7;
    private Pesos pesos = new Pesos();
    private Penalidades penalidades = new Penalidades();
    private Diversidade diversidade = new Diversidade();

    @Getter
    @Setter
    public static class Pesos {
        private double rfm = 0.50;
        private double regional = 0.20;
        private double sazonalidade = 0.15;
        private double margem = 0.15;
    }

    @Getter
    @Setter
    public static class Penalidades {
        private double compradoRecente = 15.0;
        private double precoOutlier = 20.0;
        private double dadoIncompleto = 5.0;
    }

    @Getter
    @Setter
    public static class Diversidade {
        private int maxPorCategoria = 2;
    }
}
