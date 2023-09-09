package com.example.groupassignment;

public class CommentClass {
    private String commentText;
    private String commenterUsername;

    public CommentClass() {
        // Default constructor required for Firebase
    }

    public CommentClass(String commentText, String commenterUsername) {
        this.commentText = commentText;
        this.commenterUsername = commenterUsername;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setCommenterUsername(String commenterUsername) {
        this.commenterUsername = commenterUsername;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getCommenterUsername() {
        return commenterUsername;
    }
}
