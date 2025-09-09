plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mad_gruop_ass"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mad_gruop_ass"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // RecyclerView and CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Coordinator Layout
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    
    // Material Design Components
    implementation("com.google.android.material:material:1.10.0")
    
    // Image loading library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    
    // 新增：详情页面依赖
    // Volley网络库（用于详情页面的API调用）
    implementation("com.android.volley:volley:1.2.1")
    
    // Activity KTX（用于详情页面的协程支持）
    implementation("androidx.activity:activity-ktx:1.7.2")
    
    // Fragment KTX（如果需要Fragment支持）
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    
    // ConstraintLayout（详情页面布局需要）
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}