package com.example.mad_gruop_ass

data class RentalRecord(
    var username: String = "",
    var password: String = "", // Add password field
    var itemName: String = "",
    var type: String = "", // "Lend" or "Borrow"
    var status: String = "", // "Available", "Completed", etc.
    var description: String = "",
    var distance: String = "",
    var credit: Int = 0,
    var like: Int = 0,
    var favor: Int = 0,
    var gmail: String = "",
    var gender: String = "" // Added gender field
) {
    constructor(
        username: String,
        itemName: String,
        type: String,
        status: String,
        description: String,
        distance: String,
        credit: Int
    ) : this(
        username = username,
        itemName = itemName,
        type = type,
        status = status,
        description = description,
        distance = distance,
        credit = credit,
        like = 0, // Default
        favor = 0, // Default
        gender = "" // Default
    )

    fun getDisplayText(): String {
        return "$type \"$itemName\" ($status) - $distance"
    }
}


