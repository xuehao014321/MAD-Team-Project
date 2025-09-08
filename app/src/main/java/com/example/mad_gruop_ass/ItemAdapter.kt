package com.example.mad_gruop_ass

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
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
    private val items: List<ItemModel>,
    private val onItemClick: (ItemModel) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    
    // 存储每个item的点赞状态
    private val likeStates = mutableMapOf<Int, Boolean>()
    
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
        val distance = item.distance.trim()
        holder.distanceText.text = when {
            distance.isEmpty() -> "0 km"
            distance.endsWith("km") -> distance
            distance.endsWith("m") -> distance.replace("m", "km")
            else -> "$distance km"
        }
        holder.ownerText.text = item.username
        holder.likesText.text = item.likes.toString()
        
        // 加载图片
        loadImage(holder.itemImage, item.imageUrl)
        
        // 设置点赞状态 - 优先使用item.isLiked，如果没有则使用本地状态
        val isLiked = item.isLiked || (likeStates[item.itemId] ?: false)
        updateLikeUI(holder, isLiked, item.likes)
        
        // 设置点赞点击事件
        holder.likeIcon.setOnClickListener {
            try {
                val currentLiked = item.isLiked || (likeStates[item.itemId] ?: false)
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
                updateLikeUI(holder, item.isLiked, item.likes)
            }
        }
        
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
                val apiClient = ApiClient()
                // 这里需要使用协程，但我们在Thread中，所以使用runBlocking
                kotlinx.coroutines.runBlocking {
                    val success = apiClient.updateItemLikes(itemId, newLikesCount, isLiked)
                    if (!success) {
                        android.util.Log.e("ItemAdapter", "Failed to update likes on server for item $itemId")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemAdapter", "Error updating likes on server", e)
            }
        }.start()
    }
}