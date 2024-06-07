package com.android.lambda.home.post;

public class Post {

    public String postId;
    public String userId;
    public String name;
    public String title;
    public long timestamp;
    public String content;
    public int upvotes;
    public int downvotes;
    public String profileImageUrl;
    public String department;
    public String year;
    public String topic;

    public Post() {
    }

    public Post(String postId, String userId, String name, String title, long timestamp, String content, int upvotes, int downvotes, String profileImageUrl, String department, String year, String topic) {
        this.postId = postId;
        this.userId = userId;
        this.name = name;
        this.title = title;
        this.timestamp = timestamp;
        this.content = content;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.year = year;
        this.topic = topic;
    }
}
