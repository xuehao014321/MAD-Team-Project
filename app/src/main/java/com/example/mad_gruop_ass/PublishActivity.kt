package com.example.mad_gruop_ass

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.mad_gruop_ass.utils.UserSessionManager
import kotlinx.coroutines.launch

class PublishActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var titleInput: TextInputEditText
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var descriptionInputLayout: TextInputLayout
    private lateinit var priceInput: TextInputEditText
    private lateinit var publishButton: MaterialButton
    
    private lateinit var uploadPlaceholder: LinearLayout
    private lateinit var selectedPhoto: ImageView
    private lateinit var removePhotoBtn: ImageView
    private var selectedImageUri: Uri? = null
    
    private lateinit var userSessionManager: UserSessionManager
    private var currentUsername: String = ""
    
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
            showPermissionDeniedDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        
        // Initialize user session manager
        userSessionManager = UserSessionManager(this)
        currentUsername = userSessionManager.getUsername() ?: ""
        
        setupToolbar()
        setupViews()
        setupClickListeners()
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
        
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder)
        selectedPhoto = findViewById(R.id.selectedPhoto)
        removePhotoBtn = findViewById(R.id.removePhotoBtn)
    }
    
    private fun setupClickListeners() {
        publishButton.setOnClickListener {
            handlePublish()
        }
        
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
    
    private fun getRequiredPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    
    private fun checkPermissionAndPickImage() {
        val permission = getRequiredPermission()
        
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
    
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Photo Gallery Permission Required")
            .setMessage("To upload item photos, we need access to your photo gallery. Please allow gallery permission to continue.")
            .setPositiveButton("Allow") { _, _ ->
                permissionLauncher.launch(getRequiredPermission())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Gallery permission was denied, unable to upload images.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open image picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
        
        // 图片不是必须的，如果没有选择图片，将使用默认图片
        
        if (hasError) return
        
        publishItem(title, description, price)
    }
    
    private fun publishItem(title: String, description: String, price: String) {
        publishButton.isEnabled = false
        publishButton.text = "Publishing..."
        
        if (selectedImageUri != null) {
            // Upload image first, then create item
            uploadImageAndCreateItem(title, description, price)
        } else {
            // Create item with default image URL
            createItemDirectly(title, description, price, "https://picsum.photos/200?random")
        }
    }
    
    private fun uploadImageAndCreateItem(title: String, description: String, price: String) {
        publishButton.text = "Uploading image..."
        
        lifecycleScope.launch {
            try {
                val uploadedImageUrl = ApiClient.uploadImage(selectedImageUri!!, this@PublishActivity)
                if (uploadedImageUrl != null) {
                    runOnUiThread {
                        Toast.makeText(this@PublishActivity, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                        createItemDirectly(title, description, price, uploadedImageUrl)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PublishActivity, "Image upload failed, please try again", Toast.LENGTH_SHORT).show()
                        publishButton.isEnabled = true
                        publishButton.text = "Publish Item"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PublishActivity, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    publishButton.isEnabled = true
                    publishButton.text = "Publish Item"
                }
            }
        }
    }
    
    private fun createItemDirectly(title: String, description: String, price: String, imageUrl: String) {
        publishButton.text = "Creating item..."
        
        // 获取当前用户的距离信息
        ApiClient.getUserById(userSessionManager.getUserId(), object : ApiClient.UserCallback {
            override fun onSuccess(user: User) {
                val newItem = Item().apply {
                    this.title = title
                    this.description = description
                    this.price = price.toDoubleOrNull() ?: 0.0
                    this.status = "Available"
                    this.likes = 0
                    this.views = 0
                    this.distance = user.getDistanceDouble() // 使用当前用户的距离信息
                    this.userId = userSessionManager.getUserId()
                    this.username = currentUsername
                    this.imageUrl = imageUrl
                    this.createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                }
                
                ApiClient.createItem(newItem, object : ApiClient.ItemCallback {
                    override fun onSuccess(item: Item) {
                        runOnUiThread {
                            Toast.makeText(this@PublishActivity, "Item published successfully!", Toast.LENGTH_SHORT).show()
                            
                            // Return to main screen and refresh data
                            val intent = Intent()
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                    
                    override fun onError(error: String) {
                        runOnUiThread {
                            Toast.makeText(this@PublishActivity, "Publishing failed: $error", Toast.LENGTH_SHORT).show()
                            publishButton.isEnabled = true
                            publishButton.text = "Publish Item"
                        }
                    }
                })
            }
            
            override fun onError(error: String) {
                // 如果获取用户信息失败，使用默认距离值
                val newItem = Item().apply {
                    this.title = title
                    this.description = description
                    this.price = price.toDoubleOrNull() ?: 0.0
                    this.status = "Available"
                    this.likes = 0
                    this.views = 0
                    this.distance = 5.0 // 默认距离值
                    this.userId = userSessionManager.getUserId()
                    this.username = currentUsername
                    this.imageUrl = imageUrl
                    this.createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                }
                
                ApiClient.createItem(newItem, object : ApiClient.ItemCallback {
                    override fun onSuccess(item: Item) {
                        runOnUiThread {
                            Toast.makeText(this@PublishActivity, "Item published successfully!", Toast.LENGTH_SHORT).show()
                            
                            // Return to main screen and refresh data
                            val intent = Intent()
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                    
                    override fun onError(error: String) {
                        runOnUiThread {
                            Toast.makeText(this@PublishActivity, "Publishing failed: $error", Toast.LENGTH_SHORT).show()
                            publishButton.isEnabled = true
                            publishButton.text = "Publish Item"
                        }
                    }
                })
            }
        })
    }
    
}