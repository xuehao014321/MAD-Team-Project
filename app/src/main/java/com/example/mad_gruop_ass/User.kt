package com.example.mad_gruop_ass

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")
    var userId: Int = 0,
    
    @SerializedName("username")
    var username: String = "",
    
    @SerializedName("email")
    var email: String = "",
    
    @SerializedName("password")
    var password: String = "",
    
    @SerializedName("phone")
    var phone: String = "",
    
    @SerializedName("gender")
    var gender: String = "",
    
    @SerializedName("distance")
    private var distance: String = "0", // API returns string format, e.g. "5.5"
    
    @SerializedName("created_at")
    var createdAt: String? = null,
    
    @SerializedName("avatar_url")
    var avatarUrl: String? = null, // Avatar URL field from API
    
    var credit: Int = 0 // Local credit field, not from API
) {
    
    // Constructor for sign up form (simplified)
    constructor(
        username: String,
        password: String,
        email: String,
        phone: String,
        category: String
    ) : this(
        username = username,
        password = password,
        email = email,
        phone = phone,
        gender = category, // Using gender field to store category
        credit = 0, // Default credit is 0
        distance = "0", // Default distance
        createdAt = null,
        avatarUrl = null
    )

    // Distance getter methods for compatibility
    fun getDistance(): Int {
        return try {
            distance.toDouble().toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    fun getDistanceString(): String = distance
    
    fun getDistanceDouble(): Double {
        return try {
            distance.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
    
    // Distance setter methods for compatibility
    fun setDistance(distance: Int) {
        this.distance = distance.toString()
    }
    
    fun setDistanceString(distance: String) {
        this.distance = distance
    }
    
    fun setDistanceDouble(distance: Double) {
        this.distance = distance.toString()
    }
    
    override fun toString(): String {
        return "User(" +
                "userId=$userId, " +
                "username='$username', " +
                "email='$email', " +
                "phone='$phone', " +
                "gender='$gender', " +
                "distance='$distance', " +
                "createdAt='$createdAt', " +
                "avatarUrl='$avatarUrl', " +
                "credit=$credit" +
                ")"
    }
}


