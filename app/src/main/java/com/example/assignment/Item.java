package com.example.assignment;

public class Item {
    private int item_id;
    private int user_id;
    private String title;
    private String description;
    private double price;
    private String image_url;
    private String status;
    private int views;
    private int likes;
    private double distance;
    private String created_at;

    public Item() {}

    public Item(int item_id, int user_id, String title, String description, double price, 
                String image_url, String status, int views, int likes, double distance, String created_at) {
        this.item_id = item_id;
        this.user_id = user_id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.image_url = image_url;
        this.status = status;
        this.views = views;
        this.likes = likes;
        this.distance = distance;
        this.created_at = created_at;
    }

    // Getters
    public int getItemId() { return item_id; }
    public int getUserId() { return user_id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return image_url; }
    public String getStatus() { return status; }
    public int getViews() { return views; }
    public int getLikes() { return likes; }
    public double getDistance() { return distance; }
    public String getCreatedAt() { return created_at; }

    // Setters
    public void setItemId(int item_id) { this.item_id = item_id; }
    public void setUserId(int user_id) { this.user_id = user_id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImageUrl(String image_url) { this.image_url = image_url; }
    public void setStatus(String status) { this.status = status; }
    public void setViews(int views) { this.views = views; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setDistance(double distance) { this.distance = distance; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
}