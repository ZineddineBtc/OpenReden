package com.example.openreden.model;

import java.util.ArrayList;

public class Chat {
    private String id, lastMessageContent;
    private long lastMessageTime;
    private boolean isRead;
    private ArrayList<String> interlocutors;


    public Chat(String id, ArrayList<String> interlocutors,
                String lastMessageContent) {
        this.id = id;
        this.interlocutors = interlocutors;
        this.lastMessageContent = lastMessageContent;
    }

    public Chat(String id, ArrayList<String> interlocutors,
                String lastMessageContent, long lastMessageTime, boolean isRead) {
        this.id = id;
        this.interlocutors = interlocutors;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTime = lastMessageTime;
        this.isRead = isRead;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public ArrayList<String> getInterlocutors() {
        return interlocutors;
    }

    public void setInterlocutors(ArrayList<String> interlocutors) {
        this.interlocutors = interlocutors;
    }
}
