package org.example.projetofinalfruki.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMessageDTO {
    private String event;
    private DataPayload data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataPayload {
        private String remoteJid;
        private String pushName;
        private MessageContent message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageContent {
        private String conversation;
    }
}