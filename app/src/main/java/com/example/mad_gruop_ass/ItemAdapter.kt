package com.example.mad_gruop_ass

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.File

class ItemAdapter(
    private val items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    
    // 存储每个item的点赞状态
    private val likeStates = mutableMapOf<Int, Boolean>()
    
    // 用户缓存，避免重复API调用
    private val userCache = mutableMapOf<Int, String>()
    
    /**
     * 根据user_id获取用户名，优先使用缓存
     */
    private fun getUsernameByUserId(userId: Int, callback: (String) -> Unit) {
        // 先检查缓存
        userCache[userId]?.let { username ->
            callback(username)
            return
        }
        
        // 如果缓存中没有，调用API获取
        ApiClient.getUserById(userId, object : ApiClient.UserCallback {
            override fun onSuccess(user: User) {
                val username = user.username
                // 缓存用户名
                userCache[userId] = username
                callback(username)
            }
            
            override fun onError(error: String) {
                Log.e("ItemAdapter", "Failed to get username for user ID $userId: $error")
                // 如果API调用失败，使用默认格式
                callback("User $userId")
            }
        })
    }
    
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val priceText: TextView = itemView.findViewById(R.id.priceText)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val ownerText: TextView = itemView.findViewById(R.id.ownerText)
        val likesText: TextView = itemView.findViewById(R.id.likesText)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        // 封印层相关视图
        val borrowedSealOverlay: View = itemView.findViewById(R.id.borrowedSealOverlay)
        val sealIcon: ImageView = itemView.findViewById(R.id.sealIcon)
        val sealText: TextView = itemView.findViewById(R.id.sealText)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        
        // 绑定数据
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description
        holder.priceText.text = "RM${item.price}"
        // 处理距离显示，确保有km单位
        val distance = item.distance.toString()
        holder.distanceText.text = when {
            distance.isEmpty() -> "0 km"
            distance.endsWith("km") -> distance
            distance.endsWith("m") -> distance.replace("m", "km")
            else -> "$distance km"
        }
        // 显示用户名，优先使用API返回的字段，如果没有则根据user_id查询
        Log.d("ItemAdapter", "Item ${item.title}: username='${item.username}', ownerName='${item.ownerName}', userName='${item.userName}', owner='${item.owner}', userId=${item.userId}")
        
        val displayName = when {
            item.username.isNotEmpty() -> item.username
            item.ownerName.isNotEmpty() -> item.ownerName
            item.userName.isNotEmpty() -> item.userName
            item.owner.isNotEmpty() -> item.owner
            else -> {
                // 如果API没有返回用户名，根据user_id查询
                getUsernameByUserId(item.userId) { username ->
                    // 在主线程中更新UI
                    holder.itemView.post {
                        holder.ownerText.text = username
                    }
                }
                "Loading..." // 临时显示
            }
        }
        
        holder.ownerText.text = displayName
        holder.likesText.text = item.likes.toString()
        
        // 加载图片
        loadImage(holder.itemImage, item.imageUrl)
        
        // 设置点赞状态 - 优先使用item.isLiked，如果没有则使用本地状态
        val isLiked = (item.isLiked == 1) || (likeStates[item.itemId] ?: false)
        updateLikeUI(holder, isLiked, item.likes)
        
        // 设置点赞点击事件
        holder.likeIcon.setOnClickListener {
            try {
                val currentLiked = (item.isLiked == 1) || (likeStates[item.itemId] ?: false)
                val newLiked = !currentLiked
                likeStates[item.itemId] = newLiked
                
                // 获取当前显示的点赞数
                val currentLikesText = holder.likesText.text.toString()
                val currentLikesCount = currentLikesText.toIntOrNull() ?: item.likes
                
                val newLikesCount = if (newLiked) currentLikesCount + 1 else currentLikesCount - 1
                
                // 先更新UI
                updateLikeUI(holder, newLiked, newLikesCount)
                
                // 播放点赞动画
                playLikeAnimation(holder.likeIcon, newLiked)
                
                // 异步更新服务器数据
                updateLikesOnServer(item.itemId, newLikesCount, newLiked)
                
            } catch (e: Exception) {
                // 如果点赞处理失败，恢复原始状态
                holder.likesText.text = item.likes.toString()
                updateLikeUI(holder, (item.isLiked == 1), item.likes)
            }
        }
        
        // 根据status控制封印效果
        updateSealEffect(holder, item.status)
        
        // 设置卡片点击事件
        holder.cardView.setOnClickListener {
            onItemClick(item)
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    private fun loadImage(imageView: ImageView, imageUrl: String) {
        when {
            imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> {
                // 网络图片
                Glide.with(imageView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(imageView)
            }
            imageUrl.startsWith("content://") -> {
                // Content URI
                val uri = android.net.Uri.parse(imageUrl)
                Glide.with(imageView.context)
                    .load(uri)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(imageView)
            }
            imageUrl.startsWith("file://") || imageUrl.startsWith("/") -> {
                // 本地文件
                val file = if (imageUrl.startsWith("file://")) {
                    File(imageUrl.substring(7))
                } else {
                    File(imageUrl)
                }
                
                if (file.exists()) {
                    Glide.with(imageView.context)
                        .load(file)
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image)
                        .transform(CenterCrop(), RoundedCorners(16))
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.default_image)
                }
            }
            else -> {
                // 默认图片
                imageView.setImageResource(R.drawable.default_image)
            }
        }
    }
    
    private fun updateLikeUI(holder: ItemViewHolder, isLiked: Boolean, likesCount: Int) {
        try {
            if (isLiked) {
                // 使用系统图标避免资源问题
                holder.likeIcon.setImageResource(android.R.drawable.btn_star_big_on)
                holder.likeIcon.setColorFilter(android.graphics.Color.parseColor("#FF6B6B"))
            } else {
                holder.likeIcon.setImageResource(android.R.drawable.btn_star_big_off)
                holder.likeIcon.setColorFilter(android.graphics.Color.parseColor("#999999"))
            }
            holder.likesText.text = likesCount.toString()
        } catch (e: Exception) {
            // 如果资源加载失败，使用默认图标
            holder.likeIcon.setImageResource(android.R.drawable.btn_star_big_off)
            holder.likesText.text = likesCount.toString()
        }
    }
    
    private fun playLikeAnimation(imageView: ImageView, isLiked: Boolean) {
        // 使用简单的View.animate()避免资源加载问题
        if (isLiked) {
            // 点赞动画：放大再缩小
            imageView.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(200)
                .withEndAction {
                    imageView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start()
                }
                .start()
        } else {
            // 取消点赞动画：缩小再放大
            imageView.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction {
                    imageView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start()
                }
                .start()
        }
    }
    
    private fun updateLikesOnServer(itemId: Int, newLikesCount: Int, isLiked: Boolean) {
        // 在后台线程中更新服务器数据
        Thread {
            try {
                // ApiClient is an object (singleton), no need to instantiate
                // 创建更新后的Item对象
                val itemToUpdate = items.find { it.itemId == itemId }
                if (itemToUpdate != null) {
                    // 创建一个新的Item对象，更新点赞数
                    val updatedItem = Item(
                        itemId = itemToUpdate.itemId,
                        userId = itemToUpdate.userId,
                        title = itemToUpdate.title,
                        description = itemToUpdate.description,
                        imageUrl = itemToUpdate.imageUrl,
                        status = itemToUpdate.status,
                        views = itemToUpdate.views,
                        likes = newLikesCount,
                        createdAt = itemToUpdate.createdAt
                    ).apply {
                        price = itemToUpdate.price
                        distance = itemToUpdate.distance
                        this.isLiked = if (isLiked) 1 else 0
                    }
                    
                    // 更新商品信息
                    ApiClient.updateItem(updatedItem, object : ApiClient.ItemCallback {
                        override fun onSuccess(item: Item) {
                            android.util.Log.d("ItemAdapter", "Successfully updated likes for item $itemId")
                        }

                        override fun onError(error: String) {
                            android.util.Log.e("ItemAdapter", "Failed to update likes on server for item $itemId: $error")
                        }
                    })
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemAdapter", "Error updating likes on server", e)
            }
        }.start()
    }
    
    /**
     * 根据物品状态控制封印效果
     */
    private fun updateSealEffect(holder: ItemViewHolder, status: String) {
        when (status.lowercase()) {
            "borrowed" -> {
                // 显示封印效果
                holder.borrowedSealOverlay.visibility = View.VISIBLE
                holder.sealIcon.setImageResource(R.drawable.ic_lock)
                holder.sealText.text = "BORROWED"
            }
            "available", "lend" -> {
                // 隐藏封印效果
                holder.borrowedSealOverlay.visibility = View.GONE
            }
            else -> {
                // 其他状态也隐藏封印效果
                holder.borrowedSealOverlay.visibility = View.GONE
            }
        }
    }
}