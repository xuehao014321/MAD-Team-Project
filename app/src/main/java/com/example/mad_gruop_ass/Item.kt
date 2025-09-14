package com.example.mad_gruop_ass

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("item_id")
    var itemId: Int = 0,
    
    @SerializedName("user_id")
    var userId: Int = 0,
    
    @SerializedName("title")
    var title: String = "",
    
    @SerializedName("description")
    var description: String = "",
    
    @SerializedName("price")
    private var priceString: String = "0.0", // API returns string format
    
    @SerializedName("image_url")
    var imageUrl: String = "",
    
    @SerializedName("status")
    var status: String = "",
    
    @SerializedName("views")
    var views: Int = 0,
    
    @SerializedName("likes")
    var likes: Int = 0,
    
    @SerializedName("distance")
    private var distanceString: String = "0.0", // API returns string format
    
    @SerializedName("created_at")
    var createdAt: String = "",
    
    @SerializedName("is_liked")
    var isLiked: Int = 0,
    
    @SerializedName("username")
    var username: String = "",
    
    @SerializedName("owner_name")
    var ownerName: String = "",
    
    @SerializedName("user_name")
    var userName: String = "",
    
    @SerializedName("owner")
    var owner: String = ""
) {
    // Price getter/setter methods for compatibility
    var price: Double
        get() = try {
            priceString.toDouble()
        } catch (e: Exception) {
            0.0
        }
        set(value) {
            priceString = value.toString()
        }
    
    // Distance getter/setter methods for compatibility
    var distance: Double
        get() = try {
            distanceString.toDouble()
        } catch (e: Exception) {
            0.0
        }
        set(value) {
            distanceString = value.toString()
        }
    
    // Additional helper methods
    fun getPriceString(): String = priceString
    fun setPriceString(price: String) {
        this.priceString = price
    }
    
    fun getDistanceString(): String = distanceString
    fun setDistanceString(distance: String) {
        this.distanceString = distance
    }
}


