package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Gera a mensagem sazonal do bot a partir do contexto unificado
 * (clima + estação + feriados). Não acessa APIs diretamente.
 */
@Service
public class MotorSazonalidadeService {

    private final ContextoService contextoService;

    public MotorSazonalidadeService(ContextoService contextoService) {
        this.contextoService = contextoService;
    }

    public List<FeriadoDTO> listarFeriadosDoAno(String cidade) {
        return contextoService.feriados(LocalDate.now().getYear(), cidade);
    }

    public String gerarRecomendacaoParaCidade(String cidade) {
        ContextoDTO ctx = contextoService.obterContexto(cidade, null);
        double temperatura = ctx.temperatura();

        if (ctx.feriadoProximo()) {
            return String.format(
                    "🎉 *Feriado/evento a chegar em %s (%.1f °C)!*%nAntecipe o pedido! Reforce *Fruki Guaraná 2L*, *Água da Pedra* e *Cervejas*.",
                    ctx.cidade(), temperatura);
        }
        if (temperatura > 28.0) {
            return String.format(
                    "🔥 *Dia quente em %s (%.1f °C)!*%nReforce *Água Mineral Pedra* gelada e *Fruki Guaraná 2L*: alta saída garantida!",
                    ctx.cidade(), temperatura);
        }
        if (ctx.estacao() == Estacao.VERAO) {
            return String.format(
                    "☀️ *Verão em %s (%.1f °C).*%nMix de verão: águas, refrigerantes e energéticos gelados.",
                    ctx.cidade(), temperatura);
        }
        if (ctx.estacao() == Estacao.INVERNO) {
            return String.format(
                    "❄️ *Inverno em %s (%.1f °C).*%nSucos e refrescos ganham espaço; mantenha o mix equilibrado.",
                    ctx.cidade(), temperatura);
        }
        if (LocalDate.now().getMonthValue() == 9) {
            return "🐎 *É tempo de Revolução Farroupilha no RS!*\nO churrasco dos seus clientes vai pedir os sabores tradicionais da **Fruki** e **Citrus**. Garanta o seu mix!";
        }
        return String.format(
                "💡 *Dica do Dia (%s - %.1f °C):*%nMantenha o mix equilibrado entre **Fruki Cola** e os sabores de fruta para giro rápido.",
                ctx.cidade(), temperatura);
    }
}
