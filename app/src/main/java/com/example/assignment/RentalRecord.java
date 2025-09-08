package com.example.assignment;

public class RentalRecord {
    private String username;
    private String password; // Add password field
    private String itemName;
    private String type; // "Lend" or "Borrow"
    private String status; // "Available", "Completed", etc.
    private String description;
    private String distance;
    private int credit;
    private int like;
    private int favor;
    private String gmail;
    private String gender; // Added gender field

    public RentalRecord() {}

    public RentalRecord(String username, String itemName, String type, String status, String description, String distance, int credit) {
        this.username = username;
        this.itemName = itemName;
        this.type = type;
        this.status = status;
        this.description = description;
        this.distance = distance;
        this.credit = credit;
        this.like = 0; // Default
        this.favor = 0; // Default
        this.gender = ""; // Default
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; } // Add password getter
    public void setPassword(String password) { this.password = password; } // Add password setter

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public int getCredit() { return credit; }
    public void setCredit(int credit) { this.credit = credit; }

    public int getLike() { return like; }
    public void setLike(int like) { this.like = like; }

    public int getFavor() { return favor; }
    public void setFavor(int favor) { this.favor = favor; }

    public String getGmail() { return gmail; }
    public void setGmail(String gmail) { this.gmail = gmail; }

    public String getGender() { return gender; } // Added getter
    public void setGender(String gender) { this.gender = gender; } // Added setter

    public String getDisplayText() {
        return type + " \"" + itemName + "\" (" + status + ") - " + distance;
    }
}
