package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<ItemModel>()
    
    private lateinit var fabAdd: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupRecyclerView()
        setupFAB()
        loadSampleData()
        setupAnimations()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        
        // Setup StaggeredGridLayoutManager for Pinterest-like layout
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = layoutManager
        
        // Create and set adapter
        itemAdapter = ItemAdapter(itemList) { item ->
            // Handle item click - navigate to detail page
            // TODO: Implement navigation to detail page
        }
        recyclerView.adapter = itemAdapter
        
        // Add item decoration for spacing - Xiaohongshu style
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing_small)
        recyclerView.addItemDecoration(StaggeredSpacingItemDecoration(spacing))
    }
    
    private fun setupFAB() {
        fabAdd = findViewById(R.id.fabAdd)
        
        // 设置加号按钮点击事件
        fabAdd.setOnClickListener {
            // 打开发布作品页面
            val intent = Intent(this, PublishActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadSampleData() {
        // TODO: 替换为从Excel表格读取数据
        // Excel表格应包含以下列：
        // - 商品标题 (title)
        // - 价格 (price) 
        // - 距离 (distance)
        // - 拥有者 (owner)
        // - 点赞数 (likes)
        // - 照片链接 (imageUrl) - 完整的URL地址
        // - 商品类型 (type)
        
        // 示例硬编码数据 - 正式使用时需要替换
        itemList.addAll(listOf(
            ItemModel(
                id = 1,
                title = "Jersey Jersey Jersey Jersey Jersey Jersey",
                price = "RM20",
                distance = "500m",
                owner = "little bird",
                likes = 11,
                imageRes = R.drawable.default_image,
                imageUrl = null, // TODO: 从Excel表格"照片链接"列获取，如："https://example.com/images/jersey1.jpg"
                type = "Sports"
            ),
            ItemModel(
                id = 2,
                title = "vacuum cleaner vacuum cleaner vacuum cleaner",
                price = "RM20",
                distance = "500m", 
                owner = "big bird",
                likes = 100,
                imageRes = R.drawable.default_image,
                imageUrl = null, // TODO: 从Excel表格"照片链接"列获取
                type = "Appliance"
            ),
            ItemModel(
                id = 3,
                title = "refrigerator refrigerator refrigerator refrig",
                price = "RM20",
                distance = "500m",
                owner = "big bird", 
                likes = 30,
                imageRes = R.drawable.default_image,
                type = "Appliance"
            ),
            ItemModel(
                id = 4,
                title = "TVTVTVTVTVTVTVTVTVTVTVTVTVTVTV",
                price = "RM20",
                distance = "500m",
                owner = "little dog",
                likes = 20,
                imageRes = R.drawable.default_image,
                type = "Electronics"
            ),
            ItemModel(
                id = 5,
                title = "Kitchen Mixer Blender",
                price = "RM15",
                distance = "300m",
                owner = "cat lover",
                likes = 45,
                imageRes = R.drawable.default_image,
                type = "Kitchen"
            ),
            ItemModel(
                id = 6,
                title = "Gaming Chair Comfortable",
                price = "RM25",
                distance = "800m",
                owner = "gamer123",
                likes = 67,
                imageRes = R.drawable.default_image,
                type = "Furniture"
            ),
            ItemModel(
                id = 7,
                title = "Electric Drill Tool",
                price = "RM18",
                distance = "400m",
                owner = "tool master",
                likes = 25,
                imageRes = R.drawable.default_image,
                type = "Tools"
            ),
            ItemModel(
                id = 8,
                title = "Cooking Pot Large",
                price = "RM12",
                distance = "200m",
                owner = "chef cook",
                likes = 35,
                imageRes = R.drawable.default_image,
                type = "Kitchen"
            )
        ))
        
        itemAdapter.notifyDataSetChanged()
    }
    
    private fun setupAnimations() {
        // Add entrance animation to RecyclerView
        recyclerView.alpha = 0f
        recyclerView.translationY = 100f
        
        recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
} 