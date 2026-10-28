package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dto.WhatsAppMessageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook")
public class WhatsAppWebhookController {

    @PostMapping("/whatsapp")
    public ResponseEntity<String> receberMensagemWhatsApp(@RequestBody WhatsAppMessageDTO payload) {
        try {
            if (payload != null && payload.getData() != null) {
                String remetente = payload.getData().getRemoteJid();
                String nomeCliente = payload.getData().getPushName();
                String textoMensagem = payload.getData().getMessage() != null ?
                        payload.getData().getMessage().getConversation() : "";

                System.out.println("📩 Mensagem recebida de " + nomeCliente + " (" + remetente + "): " + textoMensagem);

                // Aqui é onde vais encaixar a lógica de negócio do bot B2B (ex: consultar produtos da Fruki, registar pedido, etc.)
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
        }

        return ResponseEntity.ok("Webhook da Evolution API processado com sucesso!");
    }
}