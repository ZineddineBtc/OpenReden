package com.example.openreden.model;

public class Message {
    private String id, content, sender;

    public Message(String id, String content, String sender) {
        this.id = id;
        this.content = content;
        this.sender = sender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
