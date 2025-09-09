package com.example.assignment;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private int user_id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("gender")
    private String gender;
    
    @SerializedName("distance")
    private String distance; // API returns string format, e.g. "5.5"
    
    @SerializedName("created_at")
    private String created_at;
    
    @SerializedName("avatar_url")
    private String avatar_url; // Avatar URL field from API
    
    private int credit; // Local credit field, not from API

    public User() {}

    // Original constructor (compatibility)
    public User(int user_id, String username, String email, String password, String phone, String gender, int distance, String created_at) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.distance = String.valueOf(distance); // Convert to string
        this.created_at = created_at;
        this.credit = 0; // Default credit is 0
        this.avatar_url = null;
    }

    // Constructor with credit (compatibility)
    public User(int user_id, String username, String email, String password, String phone, String gender, int distance, String created_at, int credit) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.distance = String.valueOf(distance); // Convert to string
        this.created_at = created_at;
        this.credit = credit;
        this.avatar_url = null;
    }

    // Constructor for sign up form (simplified)
    public User(String username, String password, String email, String phone, String category) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.gender = category; // Using gender field to store category
        this.credit = 0; // Default credit is 0
        this.distance = "0"; // Default distance
        this.created_at = null;
        this.avatar_url = null;
    }

    // Complete constructor (includes all API fields)
    public User(int user_id, String username, String email, String password, String phone, 
                String gender, String distance, String created_at, String avatar_url, int credit) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.distance = distance;
        this.created_at = created_at;
        this.avatar_url = avatar_url;
        this.credit = credit;
    }

    // Getters
    public int getUserId() { return user_id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    
    // ✅ Return distance in numeric form (compatibility)
    public int getDistance() { 
        try {
            return (int) Double.parseDouble(distance);
        } catch (Exception e) {
            return 0;
        }
    }
    
    // ✅ Return distance in string form (API format)
    public String getDistanceString() { return distance; }
    
    // ✅ Return distance in double form
    public double getDistanceDouble() {
        try {
            return Double.parseDouble(distance);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public String getCreatedAt() { return created_at; }
    public String getAvatarUrl() { return avatar_url; }
    public int getCredit() { return credit; }

    // Setters
    public void setUserId(int user_id) { this.user_id = user_id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setGender(String gender) { this.gender = gender; }
    
    // ✅ Set distance (accepts integer)
    public void setDistance(int distance) { this.distance = String.valueOf(distance); }
    
    // ✅ Set distance (accepts string)
    public void setDistanceString(String distance) { this.distance = distance; }
    
    // ✅ Set distance (accepts double)
    public void setDistanceDouble(double distance) { this.distance = String.valueOf(distance); }
    
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
    public void setAvatarUrl(String avatar_url) { this.avatar_url = avatar_url; }
    public void setCredit(int credit) { this.credit = credit; }
    
    // ✅ toString method for debugging
    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", distance='" + distance + '\'' +
                ", created_at='" + created_at + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", credit=" + credit +
                '}';
    }
}
