package com.example.groupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ITEM_DETAIL = 1001;
    
    private ImageButton backButton;
    private LinearLayout itemsContainer;
    private ProgressBar loadingProgressBar;
    private TextView loadingText;
    private List<Item> itemList;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        initializeViews();
        setupClickListeners();
        
        // 初始化API服务
        apiService = new ApiService(this);
        
        // 从API加载数据
        loadItemsFromApi();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        itemsContainer = findViewById(R.id.items_container);
        
        // 假设布局中有这些加载状态视图
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        loadingText = findViewById(R.id.loading_text);
        
        // 如果布局中没有这些视图，创建默认值
        if (loadingProgressBar == null) {
            loadingProgressBar = new ProgressBar(this);
        }
        if (loadingText == null) {
            loadingText = new TextView(this);
            loadingText.setText("加载中...");
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 作为启动Activity，返回按钮退出应用
                finishAffinity();
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_ITEM_DETAIL && resultCode == RESULT_OK && data != null) {
            // 获取更新的物品信息
            int updatedItemId = data.getIntExtra("updated_item_id", -1);
            String updatedStatus = data.getStringExtra("updated_item_status");
            
            if (updatedItemId != -1 && updatedStatus != null) {
                // 在列表中找到对应的物品并更新状态
                updateItemStatusInList(updatedItemId, updatedStatus);
            }
        }
    }
    
    private void updateItemStatusInList(int itemId, String newStatus) {
        if (itemList != null) {
            for (Item item : itemList) {
                if (item.getId() == itemId) {
                    item.setType(newStatus);
                    break;
                }
            }
            // 重新显示物品列表以反映状态变化
            displayItems();
        }
    }

    // 从API加载物品数据
    private void loadItemsFromApi() {
        showLoading(true);
        
        apiService.getItems(new ApiService.ApiCallback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                showLoading(false);
                itemList = items;
                displayItems();
                
                Toast.makeText(ItemListActivity.this, 
                    "成功加载 " + items.size() + " 个物品", 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(ItemListActivity.this, 
                    "加载失败: " + error, 
                    Toast.LENGTH_LONG).show();
                // 不再使用本地硬编码数据；严格跟随数据库
                itemList = new ArrayList<>();
                displayItems();
            }
        });
    }

    // 显示/隐藏加载状态
    private void showLoading(boolean show) {
        if (show) {
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.VISIBLE);
            }
            if (loadingText != null) {
                loadingText.setVisibility(View.VISIBLE);
            }
            itemsContainer.setVisibility(View.GONE);
        } else {
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            if (loadingText != null) {
                loadingText.setVisibility(View.GONE);
            }
            itemsContainer.setVisibility(View.VISIBLE);
        }
    }

    // 备用本地数据加载方法
    private void loadLocalItems() {
        itemList = new ArrayList<>();
        
        // 添加电钻
        itemList.add(new Item(
            "Electric Drill",
            "Available for 3 days. Requires RM50 refundable deposit.",
            "Lend",
            "2025-03-15",
            "500m",
            R.drawable.drill_image,
            100,
            58
        ));

        // 添加锤子
        itemList.add(new Item(
            "Hammer",
            "Heavy duty hammer for construction work. Available for 1 week.",
            "Lend",
            "2025-03-14",
            "200m",
            R.drawable.hammer_image,
            85,
            42
        ));

        // 添加螺丝刀套装
        itemList.add(new Item(
            "Screwdriver Set",
            "Complete set with 20 different screwdrivers. Perfect for home repairs.",
            "Lend",
            "2025-03-13",
            "800m",
            R.drawable.screwdriver_image,
            120,
            35
        ));

        // 添加梯子
        itemList.add(new Item(
            "Step Ladder",
            "5-step aluminum ladder. Great for reaching high places safely.",
            "Lend",
            "2025-03-12",
            "1.2km",
            R.drawable.ladder_image,
            95,
            28
        ));

        // 添加电锯
        itemList.add(new Item(
            "Electric Saw",
            "Professional grade electric saw. Requires safety training certificate.",
            "Lend",
            "2025-03-11",
            "1.5km",
            R.drawable.saw_image,
            75,
            15
        ));

        // 添加耳机
        itemList.add(new Item(
            "Headphones",
            "High-quality wireless headphones. Perfect for music and calls.",
            "Lend",
            "2025-03-10",
            "600m",
            R.drawable.headphones_image,
            65,
            32
        ));

        // 添加相机
        itemList.add(new Item(
            "Camera",
            "Professional DSLR camera with multiple lenses. Great for photography projects.",
            "Lend",
            "2025-03-09",
            "900m",
            R.drawable.camera_image,
            110,
            45
        ));

        // 添加手机
        itemList.add(new Item(
            "Phone",
            "Latest smartphone with excellent camera and long battery life.",
            "Lend",
            "2025-03-08",
            "300m",
            R.drawable.phone_image,
            95,
            67
        ));
    }

    private void displayItems() {
        // 清空容器
        itemsContainer.removeAllViews();
        
        if (itemList == null || itemList.isEmpty()) {
            // 显示空状态
            TextView emptyText = new TextView(this);
            emptyText.setText("暂无物品数据");
            emptyText.setTextSize(16);
            emptyText.setPadding(20, 20, 20, 20);
            itemsContainer.addView(emptyText);
            return;
        }
        
        for (int i = 0; i < itemList.size(); i++) {
            final Item item = itemList.get(i);
            final int position = i;
            
            View itemView = getLayoutInflater().inflate(R.layout.item_card, null);
            
            // 填充数据
            ImageView itemImage = itemView.findViewById(R.id.item_image);
            TextView itemName = itemView.findViewById(R.id.item_name);
            TextView itemDescription = itemView.findViewById(R.id.item_description);
            TextView itemStatus = itemView.findViewById(R.id.item_status);
            TextView itemDistance = itemView.findViewById(R.id.item_distance);
            TextView itemLikes = itemView.findViewById(R.id.item_likes);
            
            // 使用ImageLoader加载图片（支持网络图片URL）
            ImageLoader.loadItemImage(this, itemImage, item);
            itemName.setText(item.getName());
            itemDescription.setText(item.getDescription());
            itemDistance.setText(item.getDistance());
            itemLikes.setText(String.valueOf(item.getLikeCount()));
            
            // 设置状态显示
            String status = item.getType();
            if (status != null && status.equalsIgnoreCase("Borrowed")) {
                itemStatus.setText("Borrowed");
                itemStatus.setTextColor(getResources().getColor(R.color.error_color));
                itemStatus.setBackgroundResource(R.drawable.status_borrowed_background);
            } else {
                itemStatus.setText("Available");
                itemStatus.setTextColor(getResources().getColor(R.color.primary_color));
                itemStatus.setBackgroundResource(R.drawable.status_background);
            }
            
            // 设置点击监听器
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ItemListActivity.this, ItemDetailActivity.class);
                    intent.putExtra("item_position", position);
                    intent.putExtra("item_name", item.getName());
                    intent.putExtra("item_id", item.getId());
                    intent.putExtra("item_description", item.getDescription());
                    intent.putExtra("item_status", item.getType());
                    intent.putExtra("item_date", item.getPostedDate());
                    intent.putExtra("item_distance", item.getDistance());
                    intent.putExtra("item_image", item.getImageResource());
                    intent.putExtra("item_image_url", item.getImageUrl()); // 添加图片URL
                    intent.putExtra("item_likes", item.getLikeCount());
                    intent.putExtra("item_favorites", item.getFavoriteCount());
                    startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL);
                }
            });
            
            itemsContainer.addView(itemView);
        }
    }

    // 静态方法保持不变，用于向后兼容
    public static List<Item> getItemList() {
        List<Item> items = new ArrayList<>();
        
        items.add(new Item(
            "Electric Drill",
            "Available for 3 days. Requires RM50 refundable deposit.",
            "Lend",
            "2025-03-15",
            "500m",
            R.drawable.drill_image,
            100,
            58
        ));

        items.add(new Item(
            "Hammer",
            "Heavy duty hammer for construction work. Available for 1 week.",
            "Lend",
            "2025-03-14",
            "200m",
            R.drawable.hammer_image,
            85,
            42
        ));

        items.add(new Item(
            "Screwdriver Set",
            "Complete set with 20 different screwdrivers. Perfect for home repairs.",
            "Lend",
            "2025-03-13",
            "800m",
            R.drawable.screwdriver_image,
            120,
            35
        ));

        items.add(new Item(
            "Step Ladder",
            "5-step aluminum ladder. Great for reaching high places safely.",
            "Lend",
            "2025-03-12",
            "1.2km",
            R.drawable.ladder_image,
            95,
            28
        ));

        items.add(new Item(
            "Electric Saw",
            "Professional grade electric saw. Requires safety training certificate.",
            "Lend",
            "2025-03-11",
            "1.5km",
            R.drawable.saw_image,
            75,
            15
        ));

        items.add(new Item(
            "Headphones",
            "High-quality wireless headphones. Perfect for music and calls.",
            "Lend",
            "2025-03-10",
            "600m",
            R.drawable.headphones_image,
            65,
            32
        ));

        items.add(new Item(
            "Camera",
            "Professional DSLR camera with multiple lenses. Great for photography projects.",
            "Lend",
            "2025-03-09",
            "900m",
            R.drawable.camera_image,
            110,
            45
        ));

        items.add(new Item(
            "Phone",
            "Latest smartphone with excellent camera and long battery life.",
            "Lend",
            "2025-03-08",
            "300m",
            R.drawable.phone_image,
            95,
            67
        ));
        
        return items;
    }

    // 刷新数据的方法
    public void refreshData() {
        loadItemsFromApi();
    }
}