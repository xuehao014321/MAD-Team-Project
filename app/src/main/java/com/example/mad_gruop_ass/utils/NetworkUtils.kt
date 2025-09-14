package com.example.mad_gruop_ass.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import java.net.NetworkInterface
import java.net.InetAddress

object NetworkUtils {
    private const val TAG = "NetworkUtils"
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    
    fun logNetworkInfo(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)
        
        Log.d("NetworkUtils", "Network available: ${network != null}")
        Log.d("NetworkUtils", "Network capabilities: $activeNetwork")
        
        activeNetwork?.let {
            Log.d("NetworkUtils", "Has WiFi: ${it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}")
            Log.d("NetworkUtils", "Has Cellular: ${it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
            Log.d("NetworkUtils", "Has Internet: ${it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
        }
    }
    
    /**
     * ✅ 精确匹配WLAN接口 - 类似Node.js版本的getWLANIP()
     * 优先获取WiFi网络的IP地址
     */
    fun getWLANIP(context: Context): String {
        try {
            // 方法1: 使用WifiManager（推荐，更精确）
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            
            // 检查WiFi是否连接
            if (wifiInfo.networkId != -1) {
                val ipAddress = wifiInfo.ipAddress
                if (ipAddress != 0) {
                    val wifiIP = String.format(
                        "%d.%d.%d.%d",
                        ipAddress and 0xff,
                        ipAddress shr 8 and 0xff,
                        ipAddress shr 16 and 0xff,
                        ipAddress shr 24 and 0xff
                    )
                    Log.d(TAG, "✅ 通过WifiManager获取到WiFi IP: $wifiIP")
                    return wifiIP
                }
            }
            
            // 方法2: 遍历网络接口（备用方法）
            val targetInterfaces = listOf("wlan", "wi-fi", "wireless", "无线")
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            
            for (networkInterface in networkInterfaces) {
                val interfaceName = networkInterface.name.lowercase()
                
                // 检查是否是无线网络接口
                if (targetInterfaces.any { target -> interfaceName.contains(target) }) {
                    val addresses = networkInterface.inetAddresses
                    
                    for (address in addresses) {
                        if (!address.isLoopbackAddress && 
                            address is InetAddress && 
                            address.hostAddress?.contains(":") == false) { // IPv4地址
                            
                            val ip = address.hostAddress ?: continue
                            Log.d(TAG, "✅ 通过NetworkInterface获取到WiFi IP: $ip (接口: ${networkInterface.name})")
                            return ip
                        }
                    }
                }
            }
            
            Log.w(TAG, "⚠️ 未找到WiFi IP，返回localhost")
            return "localhost"
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 获取WiFi IP失败: ${e.message}")
            return "localhost"
        }
    }
    
    /**
     * 获取当前设备的所有网络接口信息（用于调试）
     */
    fun logAllNetworkInterfaces() {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            Log.d(TAG, "=== 所有网络接口 ===")
            
            for (networkInterface in networkInterfaces) {
                Log.d(TAG, "接口名称: ${networkInterface.name}")
                Log.d(TAG, "显示名称: ${networkInterface.displayName}")
                Log.d(TAG, "是否启用: ${networkInterface.isUp}")
                
                val addresses = networkInterface.inetAddresses
                for (address in addresses) {
                    Log.d(TAG, "  地址: ${address.hostAddress} (${if (address.isLoopbackAddress) "回环" else "非回环"})")
                }
                Log.d(TAG, "---")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "获取网络接口信息失败: ${e.message}")
        }
    }
    
    /**
     * 检查当前是否连接到WiFi网络
     */
    fun isConnectedToWiFi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}

