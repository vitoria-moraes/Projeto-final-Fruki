package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.infrastructure.external.weather.OpenWeatherService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MotorSazonalidadeService {

    private final OpenWeatherService weatherService;

    public MotorSazonalidadeService(OpenWeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Gera recomendações dinâmicas de produtos Fruki com base no clima da cidade do cliente
     * e no calendário de eventos do Rio Grande do Sul.
     */
    public String gerarRecomendacaoParaCidade(String cidade) {
        String cidadeFinal = (cidade != null && !cidade.isBlank()) ? cidade : "Porto Alegre";
        double temperaturaAtual = weatherService.obterTemperaturaAtual(cidadeFinal);

        LocalDate hoje = LocalDate.now();
        int mes = hoje.getMonthValue();

        // 1. Regra para dias quentes
        if (temperaturaAtual > 28.0) {
            return String.format(
                    "🔥 *Dia quente em %s (%.1f°C)!*\nQue tal reforçar o estoque com **Água Mineral Pedra** gelada e **Fruki Guaraná 2L**? Alta saída garantida!",
                    cidadeFinal, temperaturaAtual
            );
        }

        // 2. Regra sazonal para o Mês Farroupilha (Setembro no RS)
        if (mes == 9) {
            return "🐎 *É tempo de Revolução Farroupilha no RS!*\nO churrasco dos seus clientes vai pedir os sabores tradicionais da **Fruki** e **H2OH! / Citrus**. Garanta o seu mix!";
        }

        // 3. Recomendação padrão equilibrada com temperatura atual
        return String.format(
                "💡 *Dica do Dia (%s - %.1f°C):*\nMantenha o mix equilibrado entre **Fruki Cola** e os sabores frutas para garantir giro rápido na prateleira.",
                cidadeFinal, temperaturaAtual
        );
    }
}