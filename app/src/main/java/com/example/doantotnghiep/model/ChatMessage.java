package com.example.doantotnghiep.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    public static final int TYPE_USER = 1;
    public static final int TYPE_BOT = 2;
    public static final int TYPE_TYPING = 3;

    private String message;
    private int type;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String message, int type, long timestamp) {
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}