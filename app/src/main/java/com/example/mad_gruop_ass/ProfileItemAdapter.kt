package com.example.mad_gruop_ass


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

class ProfileItemAdapter(
    private val items: MutableList<Item>,
    private val onItemClick: (Item) -> Unit,
    private val onDeleteClick: (Item, Int) -> Unit,
    private val onReturnAvailableClick: ((Item, Int) -> Unit)? = null
) : RecyclerView.Adapter<ProfileItemAdapter.ProfileItemViewHolder>() {
    
    // 存储每个item的点赞状态
    private val likeStates = mutableMapOf<Int, Boolean>()
    
    class ProfileItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val priceText: TextView = itemView.findViewById(R.id.priceText)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val statusText: TextView = itemView.findViewById(R.id.statusText)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likesText: TextView = itemView.findViewById(R.id.likesText)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        // 封印层相关视图
        val borrowedSealOverlay: View = itemView.findViewById(R.id.borrowedSealOverlay)
        val sealIcon: ImageView = itemView.findViewById(R.id.sealIcon)
        val sealText: TextView = itemView.findViewById(R.id.sealText)
        val returnAvailableButton: TextView = itemView.findViewById(R.id.returnAvailableButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_profile, parent, false)
        return ProfileItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProfileItemViewHolder, position: Int) {
        val item = items[position]
        
        // 绑定数据
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description
        holder.priceText.text = "RM${item.price}"
        holder.statusText.text = item.status
        
        // 处理距离显示
        val distance = item.distance.toString()
        holder.distanceText.text = when {
            distance.isEmpty() -> "0 km"
            distance.endsWith("km") -> distance
            distance.endsWith("m") -> distance.replace("m", "km")
            else -> "$distance km"
        }
        
        holder.likesText.text = item.likes.toString()
        
        // 加载图片
        loadImage(holder.itemImage, item.imageUrl)
        
        // 设置点赞状态
        val isLiked = (item.isLiked == 1) || (likeStates[item.itemId] ?: false)
        updateLikeUI(holder, isLiked, item.likes)
        
        // 设置点赞点击事件
        holder.likeIcon.setOnClickListener {
            try {
                val currentLiked = (item.isLiked == 1) || (likeStates[item.itemId] ?: false)
                val newLiked = !currentLiked
                likeStates[item.itemId] = newLiked
                
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
        
        // 设置删除按钮点击事件
        holder.deleteButton.setOnClickListener {
            onDeleteClick(item, position)
        }
        
        // 设置返回可用按钮点击事件
        holder.returnAvailableButton.setOnClickListener {
            onReturnAvailableClick?.invoke(item, position)
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    fun removeItem(position: Int) {
        if (position >= 0 && position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
    }
    
    private fun loadImage(imageView: ImageView, imageUrl: String) {
        try {
            if (imageUrl.isNotEmpty()) {
                // 尝试加载网络图片
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    Glide.with(imageView.context)
                        .load(imageUrl)
                        .transform(CenterCrop(), RoundedCorners(24))
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image)
                        .into(imageView)
                } else if (imageUrl.startsWith("file://")) {
                    // 本地文件路径
                    val file = File(imageUrl.removePrefix("file://"))
                    if (file.exists()) {
                        Glide.with(imageView.context)
                            .load(file)
                            .transform(CenterCrop(), RoundedCorners(24))
                            .placeholder(R.drawable.default_image)
                            .error(R.drawable.default_image)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.default_image)
                    }
                } else {
                    // 尝试作为本地文件路径处理
                    val file = File(imageUrl)
                    if (file.exists()) {
                        Glide.with(imageView.context)
                            .load(file)
                            .transform(CenterCrop(), RoundedCorners(24))
                            .placeholder(R.drawable.default_image)
                            .error(R.drawable.default_image)
                            .into(imageView)
                    } else {
                        // 如果不是有效的文件路径，尝试作为网络URL
                        Glide.with(imageView.context)
                            .load(imageUrl)
                            .transform(CenterCrop(), RoundedCorners(24))
                            .placeholder(R.drawable.default_image)
                            .error(R.drawable.default_image)
                            .into(imageView)
                    }
                }
            } else {
                imageView.setImageResource(R.drawable.default_image)
            }
        } catch (e: Exception) {
            Log.e("ProfileItemAdapter", "Error loading image: ${e.message}")
            imageView.setImageResource(R.drawable.default_image)
        }
    }
    
    private fun updateLikeUI(holder: ProfileItemViewHolder, isLiked: Boolean, likesCount: Int) {
        if (isLiked) {
            holder.likeIcon.setImageResource(R.drawable.ic_heart_filled)
            holder.likeIcon.setColorFilter(android.graphics.Color.parseColor("#FF6B6B"))
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_heart_outline)
            holder.likeIcon.setColorFilter(android.graphics.Color.parseColor("#9E9E9E"))
        }
        holder.likesText.text = likesCount.toString()
    }
    
    private fun playLikeAnimation(likeIcon: ImageView, isLiked: Boolean) {
        try {
            // Use simple scale animation since we don't have animator resources
            val scaleX = if (isLiked) 1.2f else 0.8f
            val scaleY = if (isLiked) 1.2f else 0.8f
            
            likeIcon.animate()
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(150)
                .withEndAction {
                    likeIcon.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start()
                }
                .start()
        } catch (e: Exception) {
            Log.w("ProfileItemAdapter", "Animation failed: ${e.message}")
        }
    }
    
    private fun updateLikesOnServer(itemId: Int, newLikesCount: Int, isLiked: Boolean) {
        Thread {
            try {
                val itemToUpdate = items.find { it.itemId == itemId }
                if (itemToUpdate != null) {
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
                    
                    ApiClient.updateItem(updatedItem, object : ApiClient.ItemCallback {
                        override fun onSuccess(item: Item) {
                            Log.d("ProfileItemAdapter", "Successfully updated likes for item $itemId")
                        }

                        override fun onError(error: String) {
                            Log.e("ProfileItemAdapter", "Failed to update likes on server for item $itemId: $error")
                        }
                    })
                }
            } catch (e: Exception) {
                Log.e("ProfileItemAdapter", "Error updating likes on server", e)
            }
        }.start()
    }
    
    /**
     * 根据物品状态控制封印效果
     */
    private fun updateSealEffect(holder: ProfileItemViewHolder, status: String) {
        when (status.lowercase()) {
            "borrowed" -> {
                // 显示封印效果和返回可用按钮
                holder.borrowedSealOverlay.visibility = View.VISIBLE
                holder.sealIcon.setImageResource(R.drawable.ic_lock)
                holder.sealText.text = "BORROWED"
                holder.returnAvailableButton.visibility = View.VISIBLE
            }
            "available", "lend" -> {
                // 隐藏封印效果
                holder.borrowedSealOverlay.visibility = View.GONE
                holder.returnAvailableButton.visibility = View.GONE
            }
            else -> {
                // 其他状态也隐藏封印效果
                holder.borrowedSealOverlay.visibility = View.GONE
                holder.returnAvailableButton.visibility = View.GONE
            }
        }
    }
} 