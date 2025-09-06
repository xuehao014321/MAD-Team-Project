package com.example.mad_gruop_ass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
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
        val ownerIcon: ImageView = itemView.findViewById(R.id.ownerIcon)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        
        // Create staggered heights for Xiaohongshu effect
        val imageHeights = listOf(180, 200, 160, 220, 170, 190, 210, 150)
        val imageHeight = imageHeights[position % imageHeights.size]
        
        // Set dynamic image height
        val layoutParams = holder.itemImage.layoutParams
        layoutParams.height = (imageHeight * holder.itemView.context.resources.displayMetrics.density).toInt()
        holder.itemImage.layoutParams = layoutParams
        
        // Bind data
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description
        holder.priceText.text = "RM${item.price}"
        holder.distanceText.text = item.distance
        holder.ownerText.text = item.username
        holder.likesText.text = item.likes.toString()
        
        // 智能图片加载：支持网络URL和本地文件路径
        when {
            item.imageUrl.startsWith("http://") || item.imageUrl.startsWith("https://") -> {
                // 网络图片
                Glide.with(holder.itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(holder.itemImage)
            }
            item.imageUrl.startsWith("file://") || item.imageUrl.startsWith("/") -> {
                // 本地文件路径
                val file = if (item.imageUrl.startsWith("file://")) {
                    File(item.imageUrl.substring(7))
                } else {
                    File(item.imageUrl)
                }
                
                if (file.exists()) {
                    Glide.with(holder.itemView.context)
                        .load(file)
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image)
                        .transform(CenterCrop(), RoundedCorners(16))
                        .into(holder.itemImage)
                } else {
                    holder.itemImage.setImageResource(R.drawable.default_image)
                }
            }
            item.imageUrl.isNotEmpty() -> {
                // 尝试作为资源ID加载
                try {
                    val resourceId = item.imageUrl.toIntOrNull()
                    if (resourceId != null) {
                        Glide.with(holder.itemView.context)
                            .load(resourceId)
                            .placeholder(R.drawable.default_image)
                            .error(R.drawable.default_image)
                            .transform(CenterCrop(), RoundedCorners(16))
                            .into(holder.itemImage)
                    } else {
                        holder.itemImage.setImageResource(R.drawable.default_image)
                    }
                } catch (e: Exception) {
                    holder.itemImage.setImageResource(R.drawable.default_image)
                }
            }
            else -> {
                // 默认图片
                holder.itemImage.setImageResource(R.drawable.default_image)
            }
        }
        
        // Set click listener
        holder.cardView.setOnClickListener {
            // Add click animation
            animateClick(holder.cardView) {
                onItemClick(item)
            }
        }
        
        // Like button functionality
        holder.likeIcon.setOnClickListener {
            animateLike(holder.likeIcon)
            // TODO: Handle like functionality
        }
        
        // Add entrance animation
        animateItemEntry(holder.cardView, position)
    }
    
    override fun getItemCount(): Int = items.size
    
    private fun animateClick(view: View, onComplete: () -> Unit) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction { onComplete() }
                    .start()
            }
            .start()
    }
    
    private fun animateLike(view: View) {
        view.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
            .start()
    }
    
    private fun animateItemEntry(view: View, position: Int) {
        view.alpha = 0f
        view.translationY = 100f
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay((position * 50).toLong()) // Stagger animation
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
} 