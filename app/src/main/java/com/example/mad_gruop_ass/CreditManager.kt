package com.example.mad_gruop_ass

import android.content.Context
import android.util.Log

/**
 * 统一的Credit管理器
 * 整合所有credit计算逻辑，确保一致性
 * 
 * @author YourName
 * @version 1.0
 */
class CreditManager {
    
    companion object {
        private const val TAG = "CreditManager"
        
        // Credit计算常量 - 简化版本
        private const val BASE_CREDIT_SCORE = 50        // 基础分数
        private const val POINTS_PER_RECORD = 10        // 每条租赁记录的分数
        
        /**
         * 简化的Credit计算方法
         * 公式: 50 + (total_records × 10)
         * 
         * @param context 上下文
         * @param username 用户名
         * @param callback 回调接口
         */
        fun calculateUserCredit(
            context: Context,
            username: String, 
            callback: CreditCalculationCallback
        ) {
            Log.d(TAG, "=== 开始计算用户Credit: $username ===")
            
            try {
                // 获取用户的所有租赁记录
                val rentalRecords = RentalDataManager.getRentalHistoryForUser(context, username)
                val totalRecords = rentalRecords.size
                
                // 应用简化计算公式: 50 + (total_records × 10)
                val calculatedCredit = BASE_CREDIT_SCORE + (totalRecords * POINTS_PER_RECORD)
                
                // 记录详细信息
                logSimpleCreditCalculation(username, totalRecords, calculatedCredit, rentalRecords)
                
                callback.onSuccess(calculatedCredit)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Credit计算失败: ${e.message}")
                // 返回基础分数作为默认值
                callback.onSuccess(BASE_CREDIT_SCORE)
            }
        }
        
        /**
         * 记录简化Credit计算详细信息
         */
        private fun logSimpleCreditCalculation(
            username: String,
            totalRecords: Int,
            finalCredit: Int,
            records: List<RentalRecord>
        ) {
            Log.d(TAG, "🎯 === 简化CREDIT计算结果 === 🎯")
            Log.d(TAG, "👤 用户: $username")
            Log.d(TAG, "📝 租赁记录数: $totalRecords")
            Log.d(TAG, "🏆 最终Credit: $finalCredit")
            
            Log.d(TAG, "📊 计算公式: 50 + ($totalRecords × 10)")
            Log.d(TAG, "📊 计算明细:")
            Log.d(TAG, "  - 基础分数: $BASE_CREDIT_SCORE")
            Log.d(TAG, "  - 记录分数: ${totalRecords * POINTS_PER_RECORD} ($totalRecords × $POINTS_PER_RECORD)")
            
            if (records.isNotEmpty()) {
                Log.d(TAG, "📋 租赁记录详情:")
                records.forEachIndexed { index, record ->
                    Log.d(TAG, "  ${index + 1}. ${record.itemName} - ${record.type} (${record.status})")
                }
            } else {
                Log.d(TAG, "📋 该用户暂无租赁记录")
            }
            
            Log.d(TAG, "=== Credit计算完成 ===")
        }
        
        /**
         * 根据简化Credit分数计算星级评分
         * 基于公式: 50 + (records × 10)
         */
        fun calculateRatingFromCredit(credit: Int): Float {
            return when {
                credit >= 100 -> 5.0f    // 5条记录以上: 优秀 
                credit >= 80 -> 4.5f     // 3条记录: 很好
                credit >= 70 -> 4.0f     // 2条记录: 良好
                credit >= 60 -> 3.5f     // 1条记录: 一般
                credit >= 50 -> 3.0f     // 0条记录: 基础
                else -> 2.5f             // 异常情况
            }
        }
        
        /**
         * 获取简化Credit等级描述
         */
        fun getCreditLevelDescription(credit: Int): String {
            return when {
                credit >= 100 -> "活跃用户"
                credit >= 80 -> "积极用户" 
                credit >= 70 -> "参与用户"
                credit >= 60 -> "体验用户"
                credit >= 50 -> "新手用户"
                else -> "待激活用户"
            }
        }
    }
    
    /**
     * Credit计算回调接口
     */
    interface CreditCalculationCallback {
        fun onSuccess(credit: Int)
        fun onError(error: String)
    }
} 