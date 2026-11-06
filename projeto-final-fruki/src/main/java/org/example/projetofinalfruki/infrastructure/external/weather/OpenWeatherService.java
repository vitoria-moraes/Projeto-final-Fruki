package org.example.projetofinalfruki.infrastructure.external.weather;

import org.example.projetofinalfruki.domain.util.Texto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clima com cache curto e fallback climático.
 * Se a chave estiver ausente/inválida ou a API falhar, devolve uma temperatura
 * média da região/mês em vez de fixar 25 °C (o que matava a regra de "dia quente").
 */
@Service
public class OpenWeatherService {

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherService.class);

    /** Médias climatológicas aproximadas para o RS (jan..dez). */
    private static final double[] MEDIA_MENSAL_RS = {
            25, 25, 23, 20, 17, 14, 13, 15, 17, 19, 22, 24
    };

    private final RestTemplate restTemplate = comTimeout();
    private final Map<String, CacheClima> cache = new ConcurrentHashMap<>();

    /** Evita que a API de clima lenta trave a recomendação. */
    private static RestTemplate comTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    @Value("${api.weather.key:}")
    private String apiKey;

    @Value("${api.weather.url:https://api.openweathermap.org/data/2.5}")
    private String apiUrl;

    public record ClimaInfo(double temperatura, String fonte, String descricao) {
    }

    public ClimaInfo obterClima(String cidade) {
        String cidadeFinal = (cidade == null || cidade.isBlank()) ? "Porto Alegre" : cidade.trim();
        CacheClima emCache = cache.get(cidadeFinal);
        if (emCache != null && emCache.expiraEm().isAfter(Instant.now())) {
            return emCache.info();
        }

        ClimaInfo info = consultarApi(cidadeFinal);
        cache.put(cidadeFinal, new CacheClima(info, Instant.now().plus(30, ChronoUnit.MINUTES)));
        return info;
    }

    public double obterTemperaturaAtual(String cidade) {
        return obterClima(cidade).temperatura();
    }

    private ClimaInfo consultarApi(String cidade) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback(cidade);
        }
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/weather")
                    .queryParam("q", cidade + ",BR")
                    .queryParam("units", "metric")
                    .queryParam("appid", apiKey)
                    .toUriString();

            Map<?, ?> resposta = restTemplate.getForObject(url, Map.class);
            if (resposta != null && resposta.get("main") instanceof Map<?, ?> main) {
                Object temp = main.get("temp");
                if (temp != null) {
                    return new ClimaInfo(Double.parseDouble(temp.toString()), "OPENWEATHER", "clima real");
                }
            }
        } catch (Exception e) {
            log.warn("Clima indisponível para {} ({}). Usando climatologia.", cidade, e.getClass().getSimpleName());
        }
        return fallback(cidade);
    }

    private ClimaInfo fallback(String cidade) {
        int mes = LocalDate.now().getMonthValue() - 1;
        double temperatura = MEDIA_MENSAL_RS[mes];
        String normalizada = Texto.normalizar(cidade);
        if (normalizada.contains("gramado") || normalizada.contains("canela")
                || normalizada.contains("caxias") || normalizada.contains("bento")) {
            temperatura -= 2.0;
        } else if (normalizada.contains("capao") || normalizada.contains("tramandai")
                || normalizada.contains("torres") || normalizada.contains("balneario")) {
            temperatura += 1.0;
        }
        return new ClimaInfo(temperatura, "FALLBACK", "climatologia da região");
    }

    private record CacheClima(ClimaInfo info, Instant expiraEm) {
    }
}
