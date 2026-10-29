package com.health_ai_project.aiservice.dto;

import java.util.List;
import java.util.Map;

public class OllamaChatRequest {

    private String model;
    private List<Message> messages;
    private boolean stream;
    private Map<String, Object> format = Map.of(
            "type", "object",
            "properties", Map.of(
                    "condition", Map.of("type", "string"),
                    "risk", Map.of("type", "string", "enum", List.of("BAIXO", "MEDIO", "ALTO")),
                    "recommendation", Map.of("type", "string"),
                    "red_flags", Map.of("type", "array", "items", Map.of("type", "string")),
                    "questions", Map.of("type", "array", "items", Map.of("type", "string"))
            ),
            "required", List.of("condition", "risk", "recommendation", "red_flags", "questions"),
            "additionalProperties", false
    );
    private Map<String, Object> options = Map.of("temperature", 0);

    public OllamaChatRequest() {
    }

    public OllamaChatRequest(String model, List<Message> messages, boolean stream) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Map<String, Object> getFormat() {
        return format;
    }

    public void setFormat(Map<String, Object> format) {
        this.format = format;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public static class Message {
        private String role;
        private String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
