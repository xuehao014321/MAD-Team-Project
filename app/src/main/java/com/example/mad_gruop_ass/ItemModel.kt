package com.example.mad_gruop_ass

/**
 * 商品数据模型
 * TODO: 后续需要从Excel表格中读取数据并替换hardcoded数据
 */
data class ItemModel(
    val id: Int,
    val title: String,
    val price: String,
    val distance: String,
    val owner: String,
    val likes: Int,
    
    // 图片相关字段 - Excel表格照片链接对应
    val imageRes: Int,  // 当前使用本地资源ID，临时占位用
    val imageUrl: String? = null,  // Excel表格中的照片链接字段，用于网络图片加载
    // 注意：imageUrl将从Excel中的"照片链接"列获取，格式应为完整的URL地址
    // 例如：https://example.com/images/product1.jpg
    
    val type: String,
    var isLiked: Boolean = false,
    var isFavorite: Boolean = false
) 