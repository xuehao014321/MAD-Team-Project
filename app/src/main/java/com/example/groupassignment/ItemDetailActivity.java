package com.example.groupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

public class ItemDetailActivity extends AppCompatActivity {

    private ImageView itemImageView;
    private TextView itemNameTextView;
    private TextView itemDescriptionTextView;
    private TextView itemStatusTextView;
    private TextView postedDateTextView;
    private TextView distanceTextView;
    private TextView likeCountTextView;
    private TextView favoriteCountTextView;
    private ImageButton backButton;
    private ImageButton favoriteButton;
    private Button lendButton;
    private ImageButton likeButton;
    private LinearLayout ownerProfileButton;
    private TextView ownerNameHeader;
    
    // 加载状态相关
    private ProgressBar loadingProgressBar;
    private TextView loadingText;
    private View contentView;
    
    // API服务
    private ApiService apiService;
    private Item currentItem;

    private boolean isFavorited = false;
    private boolean isLiked = false;
    private int likeCount = 100;
    private int favoriteCount = 0;
    
    // 🔄 添加借用状态管理变量（模仿点赞功能）
    private Map<Integer, String> borrowStates = new HashMap<>(); // 存储物品ID -> 状态映射
    private String currentBorrowStatus = "Available"; // 当前显示的借用状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_detail);

        // 让内容避开系统栏
        View root = findViewById(R.id.detail_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
        
        // 初始化API服务
        apiService = new ApiService(this);
        
        // 从数据库加载物品详情
        loadItemDetailsFromApi();
    }

    private void initializeViews() {
        itemImageView = findViewById(R.id.item_image);
        itemNameTextView = findViewById(R.id.item_name);
        itemDescriptionTextView = findViewById(R.id.item_description);
        itemStatusTextView = findViewById(R.id.item_status);
        postedDateTextView = findViewById(R.id.posted_date);
        distanceTextView = findViewById(R.id.distance);
        likeCountTextView = findViewById(R.id.like_count);
        favoriteCountTextView = findViewById(R.id.favorite_count);
        backButton = findViewById(R.id.back_button);
        favoriteButton = findViewById(R.id.favorite_button);
        lendButton = findViewById(R.id.lend_button);
        likeButton = findViewById(R.id.like_button);
        ownerProfileButton = findViewById(R.id.owner_profile_button);
        ownerNameHeader = findViewById(R.id.owner_name_header);
        
        // 加载状态视图（布局中可能没有这些视图，所以创建默认值）
        loadingProgressBar = new ProgressBar(this);
        loadingText = new TextView(this);
        loadingText.setText("正在加载物品详情...");
        
        // 使用主要的ScrollView作为内容视图
        contentView = findViewById(R.id.detail_root);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLike();
            }
        });

        lendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLendAction();
            }
        });

        ownerProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示简单的提示信息，因为OwnerProfileActivity已被删除
                Toast.makeText(ItemDetailActivity.this, 
                    "Owner Profile feature is not available in this version", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 从Intent或API加载物品详情
    private void loadItemDetailsFromApi() {
        showLoading(true);
        
        Intent intent = getIntent();
        boolean hasIntentData = loadItemFromIntent();
        
        // 🔄 修复：如果从Intent已有完整数据，直接使用，不调用API
        if (hasIntentData && currentItem != null) {
            showLoading(false);
            Log.d("ItemDetail", "✅ 使用Intent数据，跳过API调用");
            Toast.makeText(this, "物品详情加载成功", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 如果有ID，优先用ID从数据库刷新
        int itemId = intent.getIntExtra("item_id", 0);
        if (itemId > 0) {
            loadItemById(itemId);
            return;
        }
        
        // 没有ID则尝试用名称从数据库刷新
        String itemName = intent.getStringExtra("item_name");
        if (itemName != null && !itemName.trim().isEmpty()) {
            loadItemByName(itemName);
            return;
        }
        
        // 既没有ID也没有名称，显示错误
        showLoading(false);
        Toast.makeText(this, "缺少物品标识，无法加载详情", Toast.LENGTH_LONG).show();
    }
    
    // 从Intent中读取物品数据
    private boolean loadItemFromIntent() {
        Intent intent = getIntent();
        
        // 检查是否有完整的物品数据
        if (intent.hasExtra("item_name") && 
            intent.hasExtra("item_description") &&
            intent.hasExtra("item_status")) {
            
            int id = intent.getIntExtra("item_id", 0);
            String name = intent.getStringExtra("item_name");
            String description = intent.getStringExtra("item_description");
            String status = intent.getStringExtra("item_status");
            String date = intent.getStringExtra("item_date");
            String distance = intent.getStringExtra("item_distance");
            String imageUrl = intent.getStringExtra("item_image_url"); // 获取图片URL
            int imageResource = intent.getIntExtra("item_image", R.drawable.drill_image);
            int likes = intent.getIntExtra("item_likes", 0);
            int favorites = intent.getIntExtra("item_favorites", 0);
            
            // 创建Item对象（包含imageUrl）
            currentItem = new Item(id, name, description, status, date, distance, imageUrl, imageResource, likes, favorites);
            
            // 显示数据
            displayItemDetails(currentItem);
            
            Toast.makeText(this, "物品详情加载成功", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return false;
    }

    private void loadItemById(int itemId) {
        apiService.getItemById(itemId, new ApiService.ApiCallback<Item>() {
            @Override
            public void onSuccess(Item item) {
                showLoading(false);
                currentItem = item;
                displayItemDetails(item);
                Toast.makeText(ItemDetailActivity.this, "物品详情加载成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e("ItemDetail", "API加载失败: " + error);
                
                // 🔄 改进：如果API失败但有Intent数据，使用Intent数据作为备用
                Intent intent = getIntent();
                if (loadItemFromIntent() && currentItem != null) {
                    Toast.makeText(ItemDetailActivity.this, "使用本地数据显示物品详情", Toast.LENGTH_SHORT).show();
                    displayItemDetails(currentItem);
                } else {
                    Toast.makeText(ItemDetailActivity.this, "加载失败: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadItemByName(String itemName) {
        apiService.getItemByName(itemName, new ApiService.ApiCallback<Item>() {
            @Override
            public void onSuccess(Item item) {
                showLoading(false);
                currentItem = item;
                displayItemDetails(item);
                Toast.makeText(ItemDetailActivity.this, "物品详情加载成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e("ItemDetail", "API加载失败: " + error);
                
                // 🔄 改进：如果API失败但有Intent数据，使用Intent数据作为备用
                Intent intent = getIntent();
                if (loadItemFromIntent() && currentItem != null) {
                    Toast.makeText(ItemDetailActivity.this, "使用本地数据显示物品详情", Toast.LENGTH_SHORT).show();
                    displayItemDetails(currentItem);
                } else {
                    Toast.makeText(ItemDetailActivity.this, "加载失败: " + error, Toast.LENGTH_LONG).show();
                }
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
            if (contentView != null) {
                contentView.setVisibility(View.GONE);
            }
        } else {
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            if (loadingText != null) {
                loadingText.setVisibility(View.GONE);
            }
            if (contentView != null) {
                contentView.setVisibility(View.VISIBLE);
            }
        }
    }

    // 显示物品详情
    private void displayItemDetails(Item item) {
        itemNameTextView.setText(item.getName() + ":");
        itemDescriptionTextView.setText(item.getDescription());
        itemStatusTextView.setText(item.getType());
        postedDateTextView.setText(item.getPostedDate());
        distanceTextView.setText(item.getDistance());
        
        likeCount = item.getLikeCount();
        favoriteCount = item.getFavoriteCount();
        
        // 🔄 初始化借用状态
        currentBorrowStatus = item.getType();
        borrowStates.put(item.getId(), currentBorrowStatus);
        
        // 设置物品图片（支持网络图片URL）
        ImageLoader.loadItemImage(this, itemImageView, item);
        
        // 设置物主名字
        String ownerName = getOwnerNameForItem(item.getName());
        ownerNameHeader.setText(ownerName);
        
        // 根据物品状态设置借用按钮状态
        updateBorrowButtonState(item.getType());
        
        updateCounts();
    }

    // 备用本地数据加载方法
    private void loadLocalItemDetails() {
        // 从Intent获取物品位置
        int itemPosition = getIntent().getIntExtra("item_position", 0);
        List<Item> itemList = getLocalItemList();
        
        if (itemPosition < itemList.size()) {
            Item item = itemList.get(itemPosition);
            currentItem = item;
            displayItemDetails(item);
        } else {
            // 默认值（如果没有传递position）
            itemNameTextView.setText("Electric Drill:");
            itemDescriptionTextView.setText("Available for 3 days. Requires RM50 refundable deposit.");
            itemStatusTextView.setText("Lend");
            postedDateTextView.setText("2025-03-15");
            distanceTextView.setText("500m");
            itemImageView.setImageResource(R.drawable.drill_image);
            ownerNameHeader.setText("John Smith");
            updateCounts();
        }
    }

    private void toggleFavorite() {
        isFavorited = !isFavorited;
        if (isFavorited) {
            favoriteCount++;
            favoriteButton.setImageResource(R.drawable.ic_star_filled);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            favoriteCount--;
            favoriteButton.setImageResource(R.drawable.ic_star_outline);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
        updateCounts();
    }

    private void toggleLike() {
        isLiked = !isLiked;
        if (isLiked) {
            likeCount++;
            likeButton.setImageTintList(getColorStateList(R.color.liked_color));
            Toast.makeText(this, "Liked", Toast.LENGTH_SHORT).show();
        } else {
            likeCount--;
            likeButton.setImageTintList(getColorStateList(R.color.text_primary));
            Toast.makeText(this, "Like removed", Toast.LENGTH_SHORT).show();
        }
        updateCounts();
    }

    private void updateCounts() {
        likeCountTextView.setText(String.valueOf(likeCount));
        favoriteCountTextView.setText(String.valueOf(favoriteCount));
    }

    // 根据物品状态更新借用按钮状态
    private void updateBorrowButtonState(String status) {
        // 使用新的updateBorrowUI方法
        updateBorrowUI(status);
    }

    private void handleLendAction() {
        if (currentItem == null) {
            Toast.makeText(this, "物品信息不完整", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 获取当前状态（优先使用本地状态）
            String currentStatus = borrowStates.get(currentItem.getId());
            if (currentStatus == null) {
                currentStatus = currentItem.getType();
            }
            
            // 检查是否已被借用
            if ("Borrowed".equalsIgnoreCase(currentStatus)) {
                Toast.makeText(this, "物品已被借用", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 显示加载状态
            lendButton.setEnabled(false);
            lendButton.setText("Borrowing...");
            
            // 直接调用服务器更新，等待服务器确认后再更新UI
            updateBorrowStatusOnServer(currentItem.getId(), "Borrowed");
            
        } catch (Exception e) {
            // 如果借用处理失败，恢复按钮状态
            lendButton.setEnabled(true);
            lendButton.setText("Borrow");
            
            Toast.makeText(this, "借用操作失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ItemDetail", "借用操作异常", e);
        }
    }

    // 🔄 新增：更新借用UI状态（模仿updateLikeUI）
    private void updateBorrowUI(String status) {
        if (status != null && status.equalsIgnoreCase("Borrowed")) {
            // 物品已被借用，按钮变灰并失效
            lendButton.setEnabled(false);
            lendButton.setBackgroundTintList(getColorStateList(R.color.button_disabled));
            lendButton.setTextColor(getColorStateList(R.color.text_disabled));
            lendButton.setText("Borrowed");
            
            // 更新状态显示
            itemStatusTextView.setText("Borrowed");
            itemStatusTextView.setTextColor(getColorStateList(R.color.text_disabled));
        } else {
            // 物品可借用，按钮正常状态
            lendButton.setEnabled(true);
            lendButton.setBackgroundTintList(getColorStateList(R.color.primary_color));
            lendButton.setTextColor(getColorStateList(R.color.white));
            lendButton.setText("Borrow");
            
            // 更新状态显示
            itemStatusTextView.setText("Available");
            itemStatusTextView.setTextColor(getColorStateList(R.color.primary_color));
        }
    }

    // 🔄 新增：播放借用动画（模仿playLikeAnimation）
    private void playBorrowAnimation() {
        // 创建缩放动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(lendButton, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(lendButton, "scaleY", 1.0f, 1.2f, 1.0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.start();
        
        // 显示借用成功对话框
        showBorrowSuccessDialog();
    }

    // 🔄 更新服务器借用状态，等待服务器确认后更新UI
    private void updateBorrowStatusOnServer(int itemId, String newStatus) {
        // 调用API更新物品状态
        apiService.borrowItem(itemId, new ApiService.ApiCallback<String>() {
            @Override
            public void onSuccess(String message) {
                Log.d("ItemDetail", "✅ 服务器状态更新成功: " + message);
                
                runOnUiThread(() -> {
                    // 服务器确认成功后，更新本地状态和UI
                    borrowStates.put(itemId, newStatus);
                    currentBorrowStatus = newStatus;
                    
                    // 更新本地物品对象
                    if (currentItem != null) {
                        currentItem.setType(newStatus);
                    }
                    
                    // 更新UI状态
                    updateBorrowButtonState(newStatus);
                    
                    // 播放借用动画
                    playBorrowAnimation();
                    
                    // 设置结果返回给列表页，通知物品状态已更新
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updated_item_id", itemId);
                    resultIntent.putExtra("updated_item_status", newStatus);
                    setResult(RESULT_OK, resultIntent);
                    
                    // 显示成功提示
                    Toast.makeText(ItemDetailActivity.this, "物品借用成功！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                Log.e("ItemDetail", "❌ 服务器状态更新失败: " + error);
                
                runOnUiThread(() -> {
                    // 服务器更新失败时，恢复按钮状态
                    lendButton.setEnabled(true);
                    lendButton.setText("Borrow");
                    
                    // 显示错误提示
                    Toast.makeText(ItemDetailActivity.this, 
                        "借用失败: " + error, 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void showBorrowSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Borrow Successful!")
                .setMessage("You have successfully borrowed this item!\n\nPlease contact the item owner according to the agreed time to pick up the item.")
                .setIcon(R.drawable.ic_success)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // 可以在这里添加其他操作，比如跳转到订单页面
                    Toast.makeText(ItemDetailActivity.this, "Borrow record saved", Toast.LENGTH_SHORT).show();
                    
                    // 更新状态显示
                    if (currentItem != null) {
                        itemStatusTextView.setText("Borrowed");
                    }
                })
                .setCancelable(false);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private String getOwnerNameForItem(String itemName) {
        // 根据物品名称返回物主名字
        // 这可以从数据库中获取，或者硬编码映射
        if (itemName == null) return "Unknown Owner";
        
        String lowerName = itemName.toLowerCase();
        if (lowerName.contains("drill")) {
            return "John Smith";
        } else if (lowerName.contains("hammer")) {
            return "Mary Johnson";
        } else if (lowerName.contains("screwdriver")) {
            return "David Lee";
        } else if (lowerName.contains("ladder")) {
            return "Sarah Wilson";
        } else if (lowerName.contains("saw")) {
            return "Mike Brown";
        } else {
            return "John Smith"; // 默认物主
        }
    }

    // 刷新数据的方法
    public void refreshItemDetails() {
        loadItemDetailsFromApi();
    }

    // 本地物品列表数据，替代已删除的ItemListActivity.getItemList()
    private List<Item> getLocalItemList() {
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
}