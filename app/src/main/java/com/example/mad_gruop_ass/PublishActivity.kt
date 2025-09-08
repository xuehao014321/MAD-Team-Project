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
    
    private val apiClient = ApiClient()
    
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
        
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a photo for your item", Toast.LENGTH_SHORT).show()
            hasError = true
        }
        
        if (hasError) return
        
        publishItem(title, description, price)
    }
    
    private fun publishItem(title: String, description: String, price: String) {
        lifecycleScope.launch {
            try {
                val connectionOk = apiClient.testConnection()
                
                if (connectionOk) {
                    var imageUrl: String? = null
                    
                    if (selectedImageUri != null) {
                        publishButton.text = "Uploading image..."
                        
                        val uploadedImageUrl = apiClient.uploadImage(selectedImageUri!!, this@PublishActivity)
                        if (uploadedImageUrl != null) {
                            imageUrl = uploadedImageUrl
                            Toast.makeText(this@PublishActivity, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@PublishActivity, "Image upload failed, please try again", Toast.LENGTH_LONG).show()
                            publishButton.text = "Publish Item"
                            return@launch
                        }
                    } else {
                        imageUrl = "https://picsum.photos/200?random"
                    }
                    
                    val finalImageUrl = imageUrl ?: "https://picsum.photos/200?random"
                    
                    // Get alice user's distance information
                    val users = apiClient.getUsers()
                    val aliceUser = users.find { it.username == "alice" }
                    val aliceDistance = aliceUser?.distance?.toString() ?: "5"
                    
                    val newItem = ItemModel(
                        itemId = 0,
                        userId = 1,
                        title = title,
                        description = description,
                        price = price,
                        imageUrl = finalImageUrl,
                        status = "Available",
                        views = 0,
                        likes = 0,
                        distance = aliceDistance,
                        createdAt = "",
                        username = "Current User"
                    )
                    
                    publishButton.isEnabled = false
                    publishButton.text = "Publishing..."
                    
                    val success = apiClient.createItem(newItem)
                    
                    if (success) {
                        Toast.makeText(this@PublishActivity, "Item published successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@PublishActivity, "Publishing failed, please try again", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PublishActivity, "Unable to connect to server, please check your network connection", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PublishActivity, "Publishing failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                publishButton.isEnabled = true
                publishButton.text = "Publish"
            }
        }
    }
}