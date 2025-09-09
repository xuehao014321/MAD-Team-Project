package com.example.groupassignment;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * 图片加载工具类
 * 使用Glide库加载网络图片，并提供本地图片作为备用
 */
public class ImageLoader {
    
    /**
     * 加载物品图片
     * 优先使用网络图片URL，如果没有则使用本地资源图片
     * 
     * @param context 上下文
     * @param imageView 要加载图片的ImageView
     * @param item 物品对象，包含图片URL和本地资源ID
     */
    public static void loadItemImage(Context context, ImageView imageView, Item item) {
        if (item.hasImageUrl()) {
            // 有网络图片URL，使用Glide加载
            loadNetworkImage(context, imageView, item.getImageUrl(), item.getImageResource());
        } else {
            // 没有网络图片URL，使用本地资源
            imageView.setImageResource(item.getImageResource());
        }
    }
    
    /**
     * 加载网络图片
     * 
     * @param context 上下文
     * @param imageView 要加载图片的ImageView
     * @param imageUrl 图片URL
     * @param fallbackResource 备用本地资源ID
     */
    public static void loadNetworkImage(Context context, ImageView imageView, String imageUrl, int fallbackResource) {
        Glide.with(context)
            .load(imageUrl)
            .placeholder(fallbackResource)        // 加载中显示的图片
            .error(fallbackResource)             // 加载失败显示的图片
            .fallback(fallbackResource)          // URL为空时显示的图片
            .centerInside()                      // 图片缩放方式：不裁剪，按原比例置于容器内
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存策略
            .into(imageView);
    }
    
    /**
     * 预加载图片到缓存
     * 
     * @param context 上下文
     * @param imageUrl 图片URL
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .preload();
        }
    }
} 