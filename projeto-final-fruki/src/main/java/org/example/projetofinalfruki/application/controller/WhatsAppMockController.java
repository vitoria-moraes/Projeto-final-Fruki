package org.example.projetofinalfruki.application.controller;

import org.example.projetofinalfruki.infrastructure.messaging.whatsapp.WhatsAppBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mock/whatsapp")
public class WhatsAppMockController {

    private final WhatsAppBotService botService;

    public WhatsAppMockController(WhatsAppBotService botService) {
        this.botService = botService;
    }

    @PostMapping("/enviar-mensagem")
    public ResponseEntity<Map<String, String>> simularMensagemWhatsapp(@RequestBody Map<String, String> payload) {
        String telefone = payload != null ? payload.get("telefone") : "";
        String mensagem = payload != null ? payload.get("mensagem") : "";

        String respostaBot = botService.processarMensagemRecebida(telefone, mensagem);

        Map<String, String> respostaMap = new HashMap<>();
        respostaMap.put("remetente", "Bot Fruki");
        respostaMap.put("paraTelefone", telefone != null ? telefone : "");
        respostaMap.put("resposta", respostaBot != null ? respostaBot : "");

        return ResponseEntity.ok(respostaMap);
    }
}