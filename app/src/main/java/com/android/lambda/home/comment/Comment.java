package com.android.lambda.home.comment;

public class Comment {
    public String commentId;
    public String postId;
    public String userId;
    public String content;
    public long timestamp;

    public Comment() {
    }

    public Comment(String commentId, String postId, String userId, String content, long timestamp) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
    }
}
