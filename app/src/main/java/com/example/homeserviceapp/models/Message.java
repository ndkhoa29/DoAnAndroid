package com.example.homeserviceapp.models;

public class Message {
    private String messageId;
    private String senderId;
    private String text;
    private boolean isRead;
    private long createdAt;

    public Message() {}

    public Message(String senderId, String text) {
        this.senderId = senderId;
        this.text = text;
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
