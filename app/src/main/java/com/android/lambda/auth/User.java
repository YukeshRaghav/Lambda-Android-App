package com.android.lambda.auth;

public class User {
    public String userId;
    public String name;
    public String email;
    public String year;
    public String department;
    public String profileImageUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String name, String email, String year, String department, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.year = year;
        this.department = department;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters (optional, for better data handling)
}