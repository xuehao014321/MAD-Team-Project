package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.mad_gruop_ass.utils.UserSessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<Item>()
    private lateinit var fabAdd: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var userSessionManager: UserSessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        userSessionManager = UserSessionManager(this)
        setupRecyclerView()
        setupFAB()
        setupProfileIcon()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        // ˢ���û�ͷ�񣨿��ܸոյ�¼��
        updateProfileIcon()
        // ������ҳʱˢ�£���֤����ͬ��
        refreshData()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = layoutManager
        
        itemAdapter = ItemAdapter(itemList) { item ->
            // Handle item click - navigate to detail page
            val intent = Intent(this@MainActivity, ItemDetailActivity::class.java)
            intent.putExtra("itemId", item.itemId)  //itemId
            intent.putExtra("itemTitle", item.title)
            intent.putExtra("itemDescription", item.description)
            intent.putExtra("itemImageUrl", item.imageUrl)
            intent.putExtra("itemStatus", item.status)
            intent.putExtra("itemLikes", item.likes)
            // 修复距离处理 - 将 Double 转换为格式化的字符串
            val distanceText = when {
                item.distance <= 0 -> "0 km"
                item.distance < 1 -> "${(item.distance * 1000).toInt()}m"
                else -> "${String.format("%.1f", item.distance)} km"
            }
            intent.putExtra("itemDistance", distanceText)
            intent.putExtra("itemCreatedAt", item.createdAt)
            intent.putExtra("itemUsername", item.username)
            intent.putExtra("itemUserId", item.userId) // ûID
            startActivity(intent)
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

    private fun setupProfileIcon() {
        profileIcon = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            // ����ѵ�¼����ת���û�����ҳ�棻���δ��¼����ת����¼ҳ��
            if (userSessionManager.isLoggedIn()) {
                val intent = Intent(this, MyProfileActivity::class.java)
                // �����û���Ϣ����������ҳ��
                intent.putExtra("username", userSessionManager.getUsername())
                intent.putExtra("user_id", userSessionManager.getUserId())
                intent.putExtra("email", userSessionManager.getEmail())
                intent.putExtra("gender", userSessionManager.getGender())
                // Note: Credit will be calculated in MyProfileActivity from API
                startActivity(intent)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        
        // �����û�ͷ��
        updateProfileIcon()
    }
    
    private fun updateProfileIcon() {
        if (userSessionManager.isLoggedIn()) {
            val avatarUrl = userSessionManager.getAvatarUrl()
            if (!avatarUrl.isNullOrEmpty()) {
                // �����û�ͷ��
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_user_circle)
                    .error(R.drawable.ic_user_circle)
                    .transform(CircleCrop())
                    .into(profileIcon)
            } else {
                // ʹ��Ĭ��ͷ��
                profileIcon.setImageResource(R.drawable.ic_user_circle)
            }
        } else {
            // ʹ��Ĭ��ͷ��
            profileIcon.setImageResource(R.drawable.ic_user_circle)
        }
    }
    
    private fun loadData() {
        // ʹ�� ApiClient �Ļص���ʽ��������
        ApiClient.getAllItems(object : ApiClient.ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                lifecycleScope.launch {
                    itemList.clear()
                    itemList.addAll(items)
                    itemAdapter.notifyDataSetChanged()
                }
            }

            override fun onError(error: String) {
                lifecycleScope.launch {
                    Toast.makeText(this@MainActivity, "��������ʧ��: $error", Toast.LENGTH_SHORT).show()
                    // ���APIʧ�ܣ����ر�������
                    loadFallbackData()
                }
            }
        })
    }

    private fun refreshData() {
        // ˢ�����ݣ�ֱ�ӵ���loadData
        loadData()
    }
    
    private fun loadFallbackData() {
        val fallbackItems = listOf(
            Item().apply {
                itemId = 1
                userId = 1
                title = "Basketball"
                description = "Professional basketball, suitable for indoor and outdoor use"
                setPriceString("50")
                imageUrl = "https://picsum.photos/200?1"
                status = "Available"
                views = 120
                likes = 15
                createdAt = "2025-09-02 09:00:00"
                username = "Test User 1"
            },
            Item().apply {
                itemId = 2
                userId = 2
                title = "Guitar"
                description = "Classical guitar with beautiful sound quality"
                setPriceString("200")
                imageUrl = "https://picsum.photos/200?2"
                status = "Available"
                views = 85
                likes = 23
                createdAt = "2025-09-02 10:30:00"
                username = "Test User 2"
            },
            Item().apply {
                itemId = 3
                userId = 3
                title = "Treadmill"
                description = "Home treadmill with complete functions"
                setPriceString("800")
                imageUrl = "https://picsum.photos/200?3"
                status = "Available"
                views = 200
                likes = 45
                createdAt = "2025-09-02 11:15:00"
                username = "Test User 3"
            }
        )
        itemList.addAll(fallbackItems)
        itemAdapter.notifyDataSetChanged()
    }
}
