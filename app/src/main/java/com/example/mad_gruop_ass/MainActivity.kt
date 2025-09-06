package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<ItemModel>()
    private lateinit var fabAdd: ImageView
    private val apiClient = ApiClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupRecyclerView()
        setupFAB()
        loadData()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = layoutManager
        
        itemAdapter = ItemAdapter(itemList) { item ->
            // Handle item click - navigate to detail page
            // TODO: Implement navigation to detail page
        }
        recyclerView.adapter = itemAdapter
        
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing_small)
        recyclerView.addItemDecoration(StaggeredSpacingItemDecoration(spacing))
    }
    
    private fun setupFAB() {
        fabAdd = findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, PublishActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            try {
                val isConnected = apiClient.testConnection()
                if (isConnected) {
                    val apiItems = apiClient.getItems()
                    val apiUsers = apiClient.getUsers()
                    
                    if (apiItems.isNotEmpty()) {
                        val userMap = apiUsers.associate { it.userId to it.username }
                        val itemsWithUsernames = apiItems.map { item ->
                            item.copy(username = userMap[item.userId] ?: "未知用户")
                        }
                        
                        itemList.clear()
                        itemList.addAll(itemsWithUsernames)
                        itemAdapter.notifyDataSetChanged()
                        Toast.makeText(this@MainActivity, "加载了 ${itemsWithUsernames.size} 个商品", Toast.LENGTH_SHORT).show()
                    } else {
                        loadFallbackData()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "API连接失败，使用本地数据", Toast.LENGTH_SHORT).show()
                    loadFallbackData()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "加载数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
                loadFallbackData()
            }
        }
    }
    
    private fun loadFallbackData() {
        itemList.addAll(listOf(
            ItemModel(
                itemId = 1,
                userId = 1,
                title = "篮球",
                description = "专业篮球，适合室内外使用",
                price = "50",
                imageUrl = "https://picsum.photos/200?1",
                status = "Available",
                views = 120,
                likes = 15,
                distance = "2.5 km",
                createdAt = "2025-09-02 09:00:00",
                username = "测试用户1"
            ),
            ItemModel(
                itemId = 2,
                userId = 2,
                title = "吉他",
                description = "古典吉他，音质优美",
                price = "200",
                imageUrl = "https://picsum.photos/200?2",
                status = "Available",
                views = 85,
                likes = 23,
                distance = "1.8 km",
                createdAt = "2025-09-02 10:30:00",
                username = "测试用户2"
            ),
            ItemModel(
                itemId = 3,
                userId = 3,
                title = "跑步机",
                description = "家用跑步机，功能齐全",
                price = "800",
                imageUrl = "https://picsum.photos/200?3",
                status = "Available",
                views = 200,
                likes = 45,
                distance = "3.2 km",
                createdAt = "2025-09-02 11:15:00",
                username = "测试用户3"
            )
        ))
        itemAdapter.notifyDataSetChanged()
    }
}