package com.example.mad_gruop_ass

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
    
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val priceText: TextView = itemView.findViewById(R.id.priceText)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val ownerText: TextView = itemView.findViewById(R.id.ownerText)
        val likesText: TextView = itemView.findViewById(R.id.likesText)
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
        
        // 设置点击事件
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
}