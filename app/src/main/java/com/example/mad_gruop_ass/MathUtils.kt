package com.example.mad_gruop_ass

/**
 * 数学工具类
 * 提供各种数学计算功能用于测试演示
 */
object MathUtils {
    
    /**
     * 计算阶乘
     * @param n 非负整数
     * @return n的阶乘
     * @throws IllegalArgumentException 当n为负数时
     */
    fun factorial(n: Int): Long {
        if (n < 0) {
            throw IllegalArgumentException("阶乘不能计算负数")
        }
        
        return when (n) {
            0, 1 -> 1L
            else -> {
                var result = 1L
                for (i in 2..n) {
                    result *= i
                }
                result
            }
        }
    }
    
    /**
     * 判断是否为质数
     * @param n 要判断的数
     * @return 如果是质数返回true，否则返回false
     */
    fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        if (n % 2 == 0 || n % 3 == 0) return false
        
        var i = 5
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false
            }
            i += 6
        }
        return true
    }
    
    /**
     * 计算最大公约数
     * @param a 第一个数
     * @param b 第二个数
     * @return 最大公约数
     */
    fun gcd(a: Int, b: Int): Int {
        return if (b == 0) Math.abs(a) else gcd(b, a % b)
    }
    
    /**
     * 计算最小公倍数
     * @param a 第一个数
     * @param b 第二个数
     * @return 最小公倍数
     */
    fun lcm(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return Math.abs(a * b) / gcd(a, b)
    }
    
    /**
     * 计算斐波那契数列第n项
     * @param n 项数（从0开始）
     * @return 斐波那契数列第n项的值
     */
    fun fibonacci(n: Int): Long {
        if (n < 0) throw IllegalArgumentException("斐波那契数列项数不能为负")
        if (n <= 1) return n.toLong()
        
        var prev = 0L
        var curr = 1L
        
        for (i in 2..n) {
            val temp = curr
            curr += prev
            prev = temp
        }
        
        return curr
    }
    
    /**
     * 判断是否为完全平方数
     * @param n 要判断的数
     * @return 如果是完全平方数返回true，否则返回false
     */
    fun isPerfectSquare(n: Int): Boolean {
        if (n < 0) return false
        val sqrt = Math.sqrt(n.toDouble()).toInt()
        return sqrt * sqrt == n
    }
} 