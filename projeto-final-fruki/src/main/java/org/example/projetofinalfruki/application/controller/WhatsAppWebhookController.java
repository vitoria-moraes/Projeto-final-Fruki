package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dtos.WhatsAppMessageDTO;
import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook")
public class WhatsAppWebhookController {

    private final JwtService jwtService;

    public WhatsAppWebhookController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/whatsapp")
    public ResponseEntity<String> receberMensagemWhatsApp(@RequestBody WhatsAppMessageDTO payload) {
        try {
            if (payload != null && payload.getData() != null) {
                String remetente = payload.getData().getRemoteJid();
                String nomeCliente = payload.getData().getPushName();
                String textoMensagem = payload.getData().getMessage() != null ?
                        payload.getData().getMessage().getConversation() : "";

                System.out.println("📩 Mensagem recebida de " + nomeCliente + " (" + remetente + "): " + textoMensagem);

                // Exemplo: Se o cliente pedir o catálogo, geramos o token JWT seguro com os dados dele
                if (textoMensagem != null && textoMensagem.toLowerCase().contains("catalogo")) {
                    // Simulamos a região e tabela de preço associadas ao lojista
                    String regiao = "Porto Alegre - RS";
                    String tabelaPreco = "TABELA_VIP_RS";

                    // Gera o token JWT com validade de 2 horas
                    String tokenJwt = jwtService.gerarTokenCatalogo(remetente, nomeCliente, regiao, tabelaPreco);

                    // Monta o link seguro para enviar de volta no WhatsApp
                    String linkSeguro = "https://pedefruki.com.br/catalogo?token=" + tokenJwt;

                    System.out.println("🔗 Link seguro gerado para o cliente: " + linkSeguro);

                    // Aqui entraria o envio da mensagem de volta pela Evolution API do WhatsApp
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
        }

        return ResponseEntity.ok("Webhook da Evolution API processado com sucesso!");
    }
}