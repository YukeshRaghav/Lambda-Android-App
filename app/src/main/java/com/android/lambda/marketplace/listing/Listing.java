package com.android.lambda.marketplace.listing;

public class Listing {

    public String listingId;
    public String userId;
    public String name;
    public String itemName;
    public long timestamp;
    public String description;
    public String category;
    public double price;
    public String itemImageUrl;
    public String profileImageUrl;

    public Listing() {
    }

    public Listing(String listingId, String userId, String name, String itemName, long timestamp, String description, String category, double price, String itemImageUrl, String profileImageUrl) {
        this.listingId = listingId;
        this.userId = userId;
        this.name = name;
        this.itemName = itemName;
        this.timestamp = timestamp;
        this.description = description;
        this.category = category;
        this.price = price;
        this.itemImageUrl = itemImageUrl;
        this.profileImageUrl = profileImageUrl;
    }
}
