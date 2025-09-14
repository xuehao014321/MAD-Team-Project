# 修改build.gradle.kts
$gradle = Get-Content "app/build.gradle.kts" -Raw
$newDeps = @"
    
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
"@
$gradle = $gradle -replace "    testImplementation\(libs\.junit\)", $newDeps
$gradle | Set-Content "app/build.gradle.kts" -NoNewline
