package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.application.dtos.WhatsAppMessageDTO;
import org.example.projetofinalfruki.domain.service.WhatsAppService;
import org.example.projetofinalfruki.infrastructure.messaging.whatsapp.WhatsAppBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);
    private static final String OK = "Webhook da Evolution API processado com sucesso!";

    private final WhatsAppBotService botService;
    private final WhatsAppService whatsAppService;
    private final String webhookToken;

    public WhatsAppWebhookController(WhatsAppBotService botService,
                                     WhatsAppService whatsAppService,
                                     @Value("${evolution.webhook-token:}") String webhookToken) {
        this.botService = botService;
        this.whatsAppService = whatsAppService;
        this.webhookToken = webhookToken;
    }

    /** Configure na Evolution API o header X-Webhook-Token com o mesmo valor de evolution.webhook-token. */
    @PostMapping("/whatsapp")
    public ResponseEntity<String> receber(@RequestBody WhatsAppMessageDTO payload,
                                          @RequestHeader(value = "X-Webhook-Token", required = false) String token) {
        if (!webhookToken.isBlank() && !webhookToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token do webhook inválido.");
        }
        try {
            var data = payload != null ? payload.getData() : null;
            if (data == null || data.getKey() == null || data.getKey().isFromMe()) return ResponseEntity.ok(OK);

            String jid = data.getKey().getRemoteJid();
            String texto = data.extrairTexto();
            if (jid == null || jid.endsWith("@g.us") || texto.isBlank()) return ResponseEntity.ok(OK);

            String telefone = jid.split("@")[0];
            String resposta = botService.processarMensagemRecebida(telefone, texto);
            whatsAppService.enviarMensagem(telefone, resposta);
        } catch (Exception e) {
            // Sempre 200: erro de negócio não pode virar retentativa infinita do webhook
            log.error("Erro ao processar webhook", e);
        }
        return ResponseEntity.ok(OK);
    }
}
