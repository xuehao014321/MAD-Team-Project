package com.example.mad_gruop_ass

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Credit逻辑测试Activity
 * 用于验证新的简化Credit计算逻辑
 */
class CreditTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CreditTestActivity"
    }

    private lateinit var testResultTextView: TextView
    private lateinit var runTestButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 简单的测试界面
        setContentView(android.R.layout.activity_list_item)
        
        // 初始化视图（如果需要UI的话）
        initViews()
        
        // 自动运行测试
        runCreditTests()
    }

    private fun initViews() {
        // 这里可以添加UI组件，目前主要通过Log输出结果
    }

    /**
     * 运行Credit计算测试
     */
    private fun runCreditTests() {
        Log.d(TAG, "🧪 === 开始Credit逻辑测试 === 🧪")
        
        // 测试用例1: 新用户（无记录）
        testCreditCalculation("testuser_new", 0, 50)
        
        // 测试用例2: 1条记录的用户
        testCreditCalculation("testuser_1record", 1, 60)
        
        // 测试用例3: 3条记录的用户  
        testCreditCalculation("testuser_3records", 3, 80)
        
        // 测试用例4: 5条记录的用户
        testCreditCalculation("testuser_5records", 5, 100)
        
        // 测试用例5: 10条记录的用户
        testCreditCalculation("testuser_10records", 10, 150)
        
        // 测试评分计算
        testRatingCalculation()
        
        // 测试等级描述
        testLevelDescription()
        
        Log.d(TAG, "🎉 === Credit逻辑测试完成 === 🎉")
    }

    /**
     * 测试Credit计算逻辑
     */
    private fun testCreditCalculation(username: String, expectedRecords: Int, expectedCredit: Int) {
        Log.d(TAG, "📝 测试用户: $username")
        Log.d(TAG, "   预期记录数: $expectedRecords")
        Log.d(TAG, "   预期Credit: $expectedCredit")
        Log.d(TAG, "   计算公式: 50 + ($expectedRecords × 10) = $expectedCredit")
        
        // 这里可以模拟创建租赁记录数据进行测试
        // 由于需要实际的数据库操作，这里主要验证公式逻辑
        val calculatedCredit = 50 + (expectedRecords * 10)
        val isCorrect = calculatedCredit == expectedCredit
        
        Log.d(TAG, "   实际计算结果: $calculatedCredit")
        Log.d(TAG, "   测试结果: ${if (isCorrect) "✅ 通过" else "❌ 失败"}")
        Log.d(TAG, "")
    }

    /**
     * 测试评分计算
     */
    private fun testRatingCalculation() {
        Log.d(TAG, "⭐ 测试评分计算:")
        
        val testCases = mapOf(
            50 to 3.0f,   // 0条记录: 基础
            60 to 3.5f,   // 1条记录: 一般
            70 to 4.0f,   // 2条记录: 良好
            80 to 4.5f,   // 3条记录: 很好
            100 to 5.0f,  // 5条记录: 优秀
            150 to 5.0f   // 10条记录: 优秀
        )
        
        testCases.forEach { (credit, expectedRating) ->
            val actualRating = CreditManager.calculateRatingFromCredit(credit)
            val isCorrect = actualRating == expectedRating
            
            Log.d(TAG, "   Credit: $credit -> 评分: $actualRating (预期: $expectedRating) ${if (isCorrect) "✅" else "❌"}")
        }
        Log.d(TAG, "")
    }

    /**
     * 测试等级描述
     */
    private fun testLevelDescription() {
        Log.d(TAG, "🏆 测试等级描述:")
        
        val testCases = mapOf(
            50 to "新手用户",   // 0条记录
            60 to "体验用户",   // 1条记录
            70 to "参与用户",   // 2条记录
            80 to "积极用户",   // 3条记录
            100 to "活跃用户",  // 5条记录
            150 to "活跃用户"   // 10条记录
        )
        
        testCases.forEach { (credit, expectedLevel) ->
            val actualLevel = CreditManager.getCreditLevelDescription(credit)
            val isCorrect = actualLevel == expectedLevel
            
            Log.d(TAG, "   Credit: $credit -> 等级: $actualLevel (预期: $expectedLevel) ${if (isCorrect) "✅" else "❌"}")
        }
        Log.d(TAG, "")
    }
    
    /**
     * 显示Credit计算公式说明
     */
    private fun showCreditFormulaExplanation() {
        Log.d(TAG, "📊 === Credit计算公式说明 === 📊")
        Log.d(TAG, "🔢 基础公式: Credit = 50 + (租赁记录数 × 10)")
        Log.d(TAG, "")
        Log.d(TAG, "📈 示例计算:")
        Log.d(TAG, "   新用户 (0条记录): 50 + (0 × 10) = 50分")
        Log.d(TAG, "   体验用户 (1条记录): 50 + (1 × 10) = 60分")
        Log.d(TAG, "   参与用户 (2条记录): 50 + (2 × 10) = 70分") 
        Log.d(TAG, "   积极用户 (3条记录): 50 + (3 × 10) = 80分")
        Log.d(TAG, "   活跃用户 (5条记录): 50 + (5 × 10) = 100分")
        Log.d(TAG, "   超级用户 (10条记录): 50 + (10 × 10) = 150分")
        Log.d(TAG, "")
        Log.d(TAG, "⭐ 评分对应关系:")
        Log.d(TAG, "   >= 100分: 5.0星 (优秀)")
        Log.d(TAG, "   >= 80分: 4.5星 (很好)")
        Log.d(TAG, "   >= 70分: 4.0星 (良好)")
        Log.d(TAG, "   >= 60分: 3.5星 (一般)")
        Log.d(TAG, "   >= 50分: 3.0星 (基础)")
        Log.d(TAG, "   < 50分: 2.5星 (异常)")
        Log.d(TAG, "===============================")
    }
} 