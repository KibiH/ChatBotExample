package com.kibisoftware.chatbotexample.chatmodel;

import com.kibisoftware.chatbotexample.server.MessageServer;

public class ChatMessage {
    private String message;
    private Type type;
    private long step;
    private String responseType;
    private String responses[];

    public ChatMessage(String message, Type type, long step,
                       String responseType, String responses[]) {
        this.message = message;
        this.type = type;
        this.step = step;
        this.responseType = responseType;
        this.responses = responses;
    }

    public ChatMessage(String message, Type type, MessageServer.Step step,
                       String responseType, String responses[]) {
        this.message = message;
        this.type = type;
        this.step = step.getValue();
        this.responseType = responseType;
        this.responses = responses;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getStep() { return step; }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() { return responseType; }

    public void setResponses(String responses[]) {
        this.responses = responses;
    }

    public String[] getResponses() {
        return responses;
    }

    public enum Type {
        SENT, RECEIVED
    }
}
