package com.example.mad_gruop_ass

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule
import org.junit.Assert.*

/**
 * MainActivity的集成测试类
 * 测试UI交互和完整的应用流程
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun useAppContext() {
        // 测试应用上下文
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.mad_gruop_ass", appContext.packageName)
    }
    
    @Test
    fun counterStartsAtZero() {
        // 验证计数器初始值为0
        onView(withId(R.id.counterTextView))
            .check(matches(withText("0")))
    }
    
    @Test
    fun incrementButtonIncreasesCounter() {
        // 测试增加按钮功能
        onView(withId(R.id.incrementButton))
            .perform(click())
        
        onView(withId(R.id.counterTextView))
            .check(matches(withText("1")))
        
        // 再次点击
        onView(withId(R.id.incrementButton))
            .perform(click())
        
        onView(withId(R.id.counterTextView))
            .check(matches(withText("2")))
    }
    
    @Test
    fun decrementButtonDecreasesCounter() {
        // 测试减少按钮功能
        onView(withId(R.id.decrementButton))
            .perform(click())
        
        onView(withId(R.id.counterTextView))
            .check(matches(withText("-1")))
    }
    
    @Test
    fun resetButtonResetsCounter() {
        // 先增加计数器
        onView(withId(R.id.incrementButton))
            .perform(click())
        onView(withId(R.id.incrementButton))
            .perform(click())
        
        // 验证计数器不为0
        onView(withId(R.id.counterTextView))
            .check(matches(withText("2")))
        
        // 点击重置按钮
        onView(withId(R.id.resetButton))
            .perform(click())
        
        // 验证计数器重置为0
        onView(withId(R.id.counterTextView))
            .check(matches(withText("0")))
    }
    
    @Test
    fun multipleButtonClicksWorkCorrectly() {
        // 测试多次按钮点击的组合操作
        
        // 增加5次
        repeat(5) {
            onView(withId(R.id.incrementButton))
                .perform(click())
        }
        onView(withId(R.id.counterTextView))
            .check(matches(withText("5")))
        
        // 减少2次
        repeat(2) {
            onView(withId(R.id.decrementButton))
                .perform(click())
        }
        onView(withId(R.id.counterTextView))
            .check(matches(withText("3")))
        
        // 重置
        onView(withId(R.id.resetButton))
            .perform(click())
        onView(withId(R.id.counterTextView))
            .check(matches(withText("0")))
    }
    
    @Test
    fun allButtonsAreDisplayed() {
        // 验证所有按钮都显示在界面上
        onView(withId(R.id.incrementButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.decrementButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.resetButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.counterTextView))
            .check(matches(isDisplayed()))
    }
}