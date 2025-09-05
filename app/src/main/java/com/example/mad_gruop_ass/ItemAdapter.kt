package com.example.mad_gruop_ass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView

class ItemAdapter(
    private val items: List<ItemModel>,
    private val onItemClick: (ItemModel) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
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
        holder.priceText.text = item.price
        holder.distanceText.text = item.distance
        holder.ownerText.text = item.owner
        holder.likesText.text = item.likes.toString()
        
        // 图片加载处理 - Excel照片链接对应
        // TODO: 替换为从Excel表格照片链接加载图片
        // 当前使用本地资源作为占位符
        if (!item.imageUrl.isNullOrEmpty()) {
            // 如果有Excel中的照片链接，使用网络图片加载库(如Glide或Picasso)
            // 示例：Glide.with(holder.itemView.context)
            //          .load(item.imageUrl)
            //          .placeholder(item.imageRes)
            //          .error(R.drawable.default_image)
            //          .into(holder.itemImage)
            
            // 当前临时使用本地资源
            holder.itemImage.setImageResource(item.imageRes)
        } else {
            // 如果没有Excel照片链接，使用默认图片
            holder.itemImage.setImageResource(item.imageRes)
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