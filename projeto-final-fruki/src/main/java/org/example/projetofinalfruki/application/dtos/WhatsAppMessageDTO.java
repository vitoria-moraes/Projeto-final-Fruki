package org.example.projetofinalfruki.application.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Formato do evento messages.upsert da Evolution API v2 (confira com o payload real da sua instância). */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMessageDTO {
    private String event;
    private String instance;
    private DataPayload data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataPayload {
        private Key key;
        private String pushName;
        private MessageContent message;

        public String extrairTexto() {
            if (message == null) return "";
            if (message.getConversation() != null) return message.getConversation();
            if (message.getExtendedTextMessage() != null && message.getExtendedTextMessage().getText() != null) {
                return message.getExtendedTextMessage().getText();
            }
            return "";
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {
        private String remoteJid;
        private boolean fromMe;
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageContent {
        private String conversation;
        private ExtendedText extendedTextMessage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtendedText {
        private String text;
    }
}
