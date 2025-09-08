package com.example.mad_gruop_ass

data class ItemModel(
    val itemId: Int,
    val userId: Int,
    val title: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val status: String,
    val views: Int,
    val likes: Int,
    val isLiked: Boolean = false,
    val distance: String,
    val createdAt: String,
    val username: String = ""
)