package org.example.projetofinalfruki.infrastructure.external.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OpenWeatherService {

    @Value("${api.weather.key:sua_chave_aqui}")
    private String apiKey;

    @Value("${api.weather.url:https://api.openweathermap.org/data/2.5}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public double obterTemperaturaAtual(String cidade) {
        try {
            String url = String.format("%s/weather?q=%s,BR&units=metric&appid=%s", apiUrl, cidade, apiKey);
            Map<String, Object> resposta = restTemplate.getForObject(url, Map.class);

            if (resposta != null && resposta.containsKey("main")) {
                Map<String, Object> main = (Map<String, Object>) resposta.get("main");
                if (main.containsKey("temp")) {
                    return Double.parseDouble(main.get("temp").toString());
                }
            }
        } catch (Exception e) {
            // Fallback silencioso para 25.0°C quando a API estiver sem chave ou fora do ar
        }
        return 25.0;
    }
}