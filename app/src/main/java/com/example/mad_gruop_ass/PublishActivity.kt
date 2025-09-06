package com.example.mad_gruop_ass

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class PublishActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var titleInput: TextInputEditText
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var descriptionInputLayout: TextInputLayout
    private lateinit var priceInput: TextInputEditText
    private lateinit var publishButton: MaterialButton
    
    // Photo upload related
    private lateinit var uploadPlaceholder: LinearLayout
    private lateinit var selectedPhoto: ImageView
    private lateinit var removePhotoBtn: ImageView
    private var selectedImageUri: Uri? = null
    
    // API client
    private val apiClient = ApiClient()
    
    // Permission and image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                showSelectedPhoto(uri)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Storage permission is required to upload photos", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        
        setupToolbar()
        setupViews()
        setupClickListeners()
        setupTextWatchers()
    }
    
    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Publish Item"
        }
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupViews() {
        titleInput = findViewById(R.id.titleInput)
        titleInputLayout = findViewById(R.id.titleInputLayout)
        descriptionInput = findViewById(R.id.descriptionInput)
        descriptionInputLayout = findViewById(R.id.descriptionInputLayout)
        priceInput = findViewById(R.id.priceInput)
        publishButton = findViewById(R.id.publishButton)
        
        // Photo upload views
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder)
        selectedPhoto = findViewById(R.id.selectedPhoto)
        removePhotoBtn = findViewById(R.id.removePhotoBtn)
    }
    
    private fun setupClickListeners() {
        publishButton.setOnClickListener {
            handlePublish()
        }
        
        // Photo upload click listeners
        uploadPlaceholder.setOnClickListener {
            checkPermissionAndPickImage()
        }
        
        selectedPhoto.setOnClickListener {
            checkPermissionAndPickImage()
        }
        
        removePhotoBtn.setOnClickListener {
            removeSelectedPhoto()
        }
    }
    
    private fun setupTextWatchers() {
        // Title character counter
        titleInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val length = s?.length ?: 0
                if (length > 50) {
                    titleInputLayout.error = "Title must be 50 characters or less"
                } else {
                    titleInputLayout.error = null
                }
            }
        })
        
        // Description character counter
        descriptionInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val length = s?.length ?: 0
                if (length > 200) {
                    descriptionInputLayout.error = "Description must be 200 characters or less"
                } else {
                    descriptionInputLayout.error = null
                }
            }
        })
    }
    
    private fun checkPermissionAndPickImage() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
    
    private fun showSelectedPhoto(uri: Uri) {
        selectedPhoto.setImageURI(uri)
        selectedPhoto.visibility = android.view.View.VISIBLE
        removePhotoBtn.visibility = android.view.View.VISIBLE
        uploadPlaceholder.visibility = android.view.View.GONE
    }
    
    private fun removeSelectedPhoto() {
        selectedImageUri = null
        selectedPhoto.visibility = android.view.View.GONE
        removePhotoBtn.visibility = android.view.View.GONE
        uploadPlaceholder.visibility = android.view.View.VISIBLE
        selectedPhoto.setImageURI(null)
    }
    
    private fun handlePublish() {
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val price = priceInput.text.toString().trim()
        
        // Validation
        var hasError = false
        
        if (title.isEmpty()) {
            titleInputLayout.error = "Please enter item title"
            hasError = true
        } else if (title.length > 50) {
            titleInputLayout.error = "Title must be 50 characters or less"
            hasError = true
        } else {
            titleInputLayout.error = null
        }
        
        if (description.isEmpty()) {
            descriptionInputLayout.error = "Please enter item description"
            hasError = true
        } else if (description.length > 200) {
            descriptionInputLayout.error = "Description must be 200 characters or less"
            hasError = true
        } else {
            descriptionInputLayout.error = null
        }
        
        if (price.isEmpty()) {
            priceInput.error = "Please enter item price"
            hasError = true
        } else {
            try {
                val priceValue = price.toDouble()
                if (priceValue <= 0) {
                    priceInput.error = "Price must be greater than 0"
                    hasError = true
                } else {
                    priceInput.error = null
                }
            } catch (e: NumberFormatException) {
                priceInput.error = "Please enter a valid price"
                hasError = true
            }
        }
        
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a photo for your item", Toast.LENGTH_SHORT).show()
            hasError = true
        }
        
        if (hasError) return
        
        // 发布商品到API
        publishItemToApi(title, description, price)
    }
    
    private fun publishItemToApi(title: String, description: String, price: String) {
        lifecycleScope.launch {
            try {
                // 创建商品对象
                val newItem = ItemModel(
                    itemId = 0, // 新商品ID为0，服务器会自动分配
                    userId = 1, // 临时用户ID，实际应用中应该从登录状态获取
                    title = title,
                    description = description,
                    price = price,
                    imageUrl = selectedImageUri?.toString() ?: "https://picsum.photos/200?random",
                    status = "Available",
                    views = 0,
                    likes = 0,
                    distance = "0 km",
                    createdAt = "",
                    username = "当前用户"
                )
                
                // 显示加载状态
                publishButton.isEnabled = false
                publishButton.text = "发布中..."
                
                // 调用API创建商品
                val success = apiClient.createItem(newItem)
                
                if (success) {
                    Log.d("PublishActivity", "商品发布成功")
                    Toast.makeText(this@PublishActivity, "商品发布成功！", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("PublishActivity", "商品发布失败")
                    Toast.makeText(this@PublishActivity, "发布失败，请重试", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PublishActivity", "发布商品时出错", e)
                Toast.makeText(this@PublishActivity, "发布失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // 恢复按钮状态
                publishButton.isEnabled = true
                publishButton.text = "发布"
            }
        }
    }
}



