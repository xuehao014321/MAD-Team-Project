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

    override fun onResume() {
        super.onResume()
        // 返回首页时刷新，保证数据同步
        refreshData()
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
                            item.copy(username = userMap[item.userId] ?: "Unknown User")
                        }
                        
                        itemList.clear()
                        itemList.addAll(itemsWithUsernames)
                        itemAdapter.notifyDataSetChanged()
                        Toast.makeText(this@MainActivity, "Loaded ${itemsWithUsernames.size} items", Toast.LENGTH_SHORT).show()
                    } else {
                        loadFallbackData()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "API connection failed, using local data", Toast.LENGTH_SHORT).show()
                    loadFallbackData()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to load data: ${e.message}", Toast.LENGTH_SHORT).show()
                loadFallbackData()
            }
        }
    }

    private fun refreshData() {
        lifecycleScope.launch {
            try {
                val apiItems = apiClient.getItems()
                val apiUsers = apiClient.getUsers()
                val userMap = apiUsers.associate { it.userId to it.username }

                itemList.clear()
                if (apiItems.isNotEmpty()) {
                    val itemsWithUsernames = apiItems.map { item ->
                        item.copy(username = userMap[item.userId] ?: "Unknown User")
                    }
                    itemList.addAll(itemsWithUsernames)
                }
                itemAdapter.notifyDataSetChanged()
            } catch (_: Exception) {
                // Silent failure to avoid disturbing user
            }
        }
    }
    
    private fun loadFallbackData() {
        itemList.addAll(listOf(
            ItemModel(
                itemId = 1,
                userId = 1,
                title = "Basketball",
                description = "Professional basketball, suitable for indoor and outdoor use",
                price = "50",
                imageUrl = "https://picsum.photos/200?1",
                status = "Available",
                views = 120,
                likes = 15,
                distance = "2.5 km",
                createdAt = "2025-09-02 09:00:00",
                username = "Test User 1"
            ),
            ItemModel(
                itemId = 2,
                userId = 2,
                title = "Guitar",
                description = "Classical guitar with beautiful sound quality",
                price = "200",
                imageUrl = "https://picsum.photos/200?2",
                status = "Available",
                views = 85,
                likes = 23,
                distance = "1.8 km",
                createdAt = "2025-09-02 10:30:00",
                username = "Test User 2"
            ),
            ItemModel(
                itemId = 3,
                userId = 3,
                title = "Treadmill",
                description = "Home treadmill with complete functions",
                price = "800",
                imageUrl = "https://picsum.photos/200?3",
                status = "Available",
                views = 200,
                likes = 45,
                distance = "3.2 km",
                createdAt = "2025-09-02 11:15:00",
                username = "Test User 3"
            )
        ))
        itemAdapter.notifyDataSetChanged()
    }
}