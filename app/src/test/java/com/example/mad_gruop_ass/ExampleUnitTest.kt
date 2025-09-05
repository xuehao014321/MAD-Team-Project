package com.example.mad_gruop_ass

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * MainActivity的单元测试类
 * 测试MainActivity中的业务逻辑方法
 */
class MainActivityUnitTest {
    
    private lateinit var mainActivity: MainActivity
    
    @Before
    fun setUp() {
        // 注意：这里我们只能测试不依赖Android Context的方法
        // 对于依赖Context的方法，需要使用Instrumented测试
        mainActivity = MainActivity()
    }
    
    @Test
    fun addNumbers_returnsCorrectSum() {
        // 测试加法功能
        val result = mainActivity.addNumbers(5, 3)
        assertEquals(8, result)
        
        // 测试负数
        val negativeResult = mainActivity.addNumbers(-2, 3)
        assertEquals(1, negativeResult)
        
        // 测试零
        val zeroResult = mainActivity.addNumbers(0, 5)
        assertEquals(5, zeroResult)
    }
    
    @Test
    fun formatMessage_withValidName_returnsFormattedGreeting() {
        // 测试有效姓名
        val result = mainActivity.formatMessage("张三")
        assertEquals("Hello, 张三!", result)
        
        val englishResult = mainActivity.formatMessage("John")
        assertEquals("Hello, John!", result)
    }
    
    @Test
    fun formatMessage_withEmptyName_returnsGuestGreeting() {
        // 测试空字符串
        val emptyResult = mainActivity.formatMessage("")
        assertEquals("Hello, Guest!", emptyResult)
        
        // 测试空白字符串
        val blankResult = mainActivity.formatMessage("   ")
        assertEquals("Hello, Guest!", blankResult)
    }
    
    @Test
    fun isValidEmail_withValidEmails_returnsTrue() {
        // 测试有效邮箱
        assertTrue(mainActivity.isValidEmail("test@example.com"))
        assertTrue(mainActivity.isValidEmail("user.name@domain.org"))
        assertTrue(mainActivity.isValidEmail("user123@test.net"))
    }
    
    @Test
    fun isValidEmail_withInvalidEmails_returnsFalse() {
        // 测试无效邮箱
        assertFalse(mainActivity.isValidEmail("invalid-email"))
        assertFalse(mainActivity.isValidEmail("@example.com"))
        assertFalse(mainActivity.isValidEmail("user@"))
        assertFalse(mainActivity.isValidEmail("user@.com"))
        assertFalse(mainActivity.isValidEmail(""))
    }
    
    @Test
    fun addition_isCorrect() {
        // 保留原有的基础测试
        assertEquals(4, 2 + 2)
    }
}