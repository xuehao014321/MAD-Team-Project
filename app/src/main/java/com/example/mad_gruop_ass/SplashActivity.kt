package com.example.mad_gruop_ass

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import com.example.mad_gruop_ass.R

class SplashActivity : ComponentActivity() {
    
    private lateinit var backgroundCircle: View
    private lateinit var logoImageView: View
    private lateinit var appNameText: View
    private lateinit var subtitleText: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // 初始化视图
        backgroundCircle = findViewById(R.id.backgroundCircle)
        logoImageView = findViewById(R.id.logoImageView)
        appNameText = findViewById(R.id.appNameText)
        subtitleText = findViewById(R.id.subtitleText)
        
        // 开始动画序列
        startAnimationSequence()
    }
    
    private fun startAnimationSequence() {
        // 创建动画集合
        val animatorSet = AnimatorSet()
        
        // 1. 背景圆形淡入并放大
        val backgroundFadeIn = ObjectAnimator.ofFloat(backgroundCircle, "alpha", 0f, 1f)
        val backgroundScaleX = ObjectAnimator.ofFloat(backgroundCircle, "scaleX", 0f, 1.3f, 1f)
        val backgroundScaleY = ObjectAnimator.ofFloat(backgroundCircle, "scaleY", 0f, 1.3f, 1f)
        backgroundFadeIn.duration = 1000
        backgroundScaleX.duration = 1200
        backgroundScaleY.duration = 1200
        
        val backgroundAnimator = AnimatorSet()
        backgroundAnimator.playTogether(backgroundFadeIn, backgroundScaleX, backgroundScaleY)
        backgroundAnimator.interpolator = OvershootInterpolator(1.5f)
        
        // 2. Logo图片淡入并放大（带旋转效果）
        val logoFadeIn = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f)
        val logoScaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0f, 1.2f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0f, 1.2f, 1f)
        val logoRotation = ObjectAnimator.ofFloat(logoImageView, "rotation", -10f, 0f)
        logoFadeIn.duration = 800
        logoScaleX.duration = 1000
        logoScaleY.duration = 1000
        logoRotation.duration = 1000
        
        val logoAnimator = AnimatorSet()
        logoAnimator.playTogether(logoFadeIn, logoScaleX, logoScaleY, logoRotation)
        logoAnimator.interpolator = OvershootInterpolator(1.2f)
        
        // 3. 应用名称淡入（带轻微上移效果）
        val appNameFadeIn = ObjectAnimator.ofFloat(appNameText, "alpha", 0f, 1f)
        val appNameScaleX = ObjectAnimator.ofFloat(appNameText, "scaleX", 0.7f, 1f)
        val appNameScaleY = ObjectAnimator.ofFloat(appNameText, "scaleY", 0.7f, 1f)
        val appNameTranslationY = ObjectAnimator.ofFloat(appNameText, "translationY", 30f, 0f)
        appNameFadeIn.duration = 600
        appNameScaleX.duration = 600
        appNameScaleY.duration = 600
        appNameTranslationY.duration = 600
        
        val appNameAnimator = AnimatorSet()
        appNameAnimator.playTogether(appNameFadeIn, appNameScaleX, appNameScaleY, appNameTranslationY)
        appNameAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        // 4. 副标题淡入（带轻微上移效果）
        val subtitleFadeIn = ObjectAnimator.ofFloat(subtitleText, "alpha", 0f, 1f)
        val subtitleScaleX = ObjectAnimator.ofFloat(subtitleText, "scaleX", 0.8f, 1f)
        val subtitleScaleY = ObjectAnimator.ofFloat(subtitleText, "scaleY", 0.8f, 1f)
        val subtitleTranslationY = ObjectAnimator.ofFloat(subtitleText, "translationY", 20f, 0f)
        subtitleFadeIn.duration = 500
        subtitleScaleX.duration = 500
        subtitleScaleY.duration = 500
        subtitleTranslationY.duration = 500
        
        val subtitleAnimator = AnimatorSet()
        subtitleAnimator.playTogether(subtitleFadeIn, subtitleScaleX, subtitleScaleY, subtitleTranslationY)
        subtitleAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        // 设置动画序列
        animatorSet.playSequentially(
            backgroundAnimator,
            logoAnimator,
            appNameAnimator,
            subtitleAnimator
        )
        
        // 开始动画
        animatorSet.start()
        
        // 动画完成后延迟跳转
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToLogin()
        }, 4000) // 总时长约4秒
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        // 添加平滑过渡效果
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish() // 结束SplashActivity，避免用户按返回键回到启动页
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理动画资源
        backgroundCircle.clearAnimation()
        logoImageView.clearAnimation()
        appNameText.clearAnimation()
        subtitleText.clearAnimation()
    }
}

