package com.example.mad_gruop_ass

/**
 * 商品数据模型
 * 从CSV文件中读取数据 - 严格按照实际CSV字段结构
 */
data class ItemModel(
    val itemId: Int,         // item_id
    val userId: Int,         // user_id
    val title: String,       // title
    val description: String, // description
    val price: String,       // price
    val imageUrl: String,    // image_url
    val status: String,      // status
    val views: Int,          // views
    val likes: Int,          // likes
    val distance: String,    // distance
    val createdAt: String,   // created_at
    val username: String = "", // 从用户数据中关联获取
    var isLiked: Boolean = false,
    var isFavorite: Boolean = false
) 