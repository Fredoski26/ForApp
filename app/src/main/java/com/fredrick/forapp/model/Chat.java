package com.fredrick.forapp.model;

public class Chat {
    private String message;
    private String receiver;
    private String sender;
    private String timestamp;
    private String type;

    boolean messageSeen;

    public Chat() {
    }

    public Chat(String message, String receiver, String sender, String timestamp, String type, boolean messageSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
        this.messageSeen = messageSeen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMessageSeen() {
        return messageSeen;
    }

    public void setMessageSeen(boolean messageSeen) {
        this.messageSeen = messageSeen;
    }
}
