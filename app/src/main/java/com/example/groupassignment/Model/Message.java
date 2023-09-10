package com.example.groupassignment.Model;

//Done by Kuek Gavin
public class Message {
    private String text;
    private String senderId;
    private Object timestamp; // You can adjust the type based on your database structure

    public Message() {
        // Default constructor required for Firebase
    }

    public Message(String text, String senderId, Object timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public Object getTimestamp() {
        return timestamp;
    }
}

