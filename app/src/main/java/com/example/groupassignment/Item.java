package com.example.groupassignment;

public class Item {
    private int id;
    private String name;
    private String description;
    private String type;
    private String postedDate;
    private String distance;
    private int imageResource;
    private String imageUrl; // 添加网络图片URL字段
    private int likeCount;
    private int favoriteCount;

    public Item() {
    }

    public Item(String name, String description, String type, String postedDate, 
                String distance, int imageResource, int likeCount, int favoriteCount) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.postedDate = postedDate;
        this.distance = distance;
        this.imageResource = imageResource;
        this.likeCount = likeCount;
        this.favoriteCount = favoriteCount;
    }

    public Item(int id, String name, String description, String type, String postedDate, 
                String distance, int imageResource, int likeCount, int favoriteCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.postedDate = postedDate;
        this.distance = distance;
        this.imageResource = imageResource;
        this.likeCount = likeCount;
        this.favoriteCount = favoriteCount;
    }

    // 新增带imageUrl的构造函数
    public Item(int id, String name, String description, String type, String postedDate, 
                String distance, String imageUrl, int imageResource, int likeCount, int favoriteCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.postedDate = postedDate;
        this.distance = distance;
        this.imageUrl = imageUrl;
        this.imageResource = imageResource;
        this.likeCount = likeCount;
        this.favoriteCount = favoriteCount;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getPostedDate() { return postedDate; }
    public String getDistance() { return distance; }
    public int getImageResource() { return imageResource; }
    public String getImageUrl() { return imageUrl; } // 新增getter
    public int getLikeCount() { return likeCount; }
    public int getFavoriteCount() { return favoriteCount; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setPostedDate(String postedDate) { this.postedDate = postedDate; }
    public void setDistance(String distance) { this.distance = distance; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; } // 新增setter
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }

    // 辅助方法：判断是否有网络图片URL
    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }
} 