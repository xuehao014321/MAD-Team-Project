package com.example.mad_gruop_ass.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class NetworkDiscovery {
    
    companion object {
        private const val TAG = "NetworkDiscovery"
        private const val API_PORT = 5000
        private const val DISCOVERY_TIMEOUT = 2000L // 2秒超时
        
        /**
         * 动态发现API服务器地址
         */
        suspend fun discoverApiServer(context: Context): String? = withContext(Dispatchers.IO) {
            try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val ipAddress = wifiInfo.ipAddress
                
                // 获取当前设备的IP地址
                val deviceIP = String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
                
                Log.d(TAG, "设备IP: $deviceIP")
                
                // 获取网络段（例如：192.168.0.）
                val networkSegment = deviceIP.substring(0, deviceIP.lastIndexOf('.') + 1)
                Log.d(TAG, "扫描网络段: ${networkSegment}xxx")
                
                // 并行扫描常见的IP地址
                val commonIPs = listOf(
                    "${networkSegment}1",    // 路由器
                    "${networkSegment}100",  // 常见服务器IP
                    "${networkSegment}101",
                    "${networkSegment}102",
                    "${networkSegment}103",  // 你当前使用的IP
                    "${networkSegment}104",  // 之前使用的IP
                    "${networkSegment}105",
                    deviceIP                 // 当前设备IP（可能服务器就在本机）
                )
                
                // 并行测试所有IP
                val deferredResults = commonIPs.map { ip ->
                    async { testApiServer(ip) }
                }
                
                // 等待所有结果，返回第一个成功的
                for (deferred in deferredResults) {
                    val result = deferred.await()
                    if (result != null) {
                        Log.d(TAG, "✅ 发现API服务器: $result")
                        return@withContext result
                    }
                }
                
                Log.w(TAG, "❌ 未找到API服务器")
                null
                
            } catch (e: Exception) {
                Log.e(TAG, "网络发现失败: ${e.message}")
                null
            }
        }
        
        /**
         * 测试指定IP是否运行API服务器
         */
        private suspend fun testApiServer(ip: String): String? = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(DISCOVERY_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(DISCOVERY_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build()
                
                val testUrl = "http://$ip:$API_PORT/api/test"
                val request = Request.Builder()
                    .url(testUrl)
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body?.contains("NeighborLink API") == true) {
                        Log.d(TAG, "✅ API服务器响应: $ip")
                        return@withContext "http://$ip:$API_PORT/api"
                    }
                }
                
                null
                
            } catch (e: Exception) {
                // 静默处理连接失败（这是正常的，因为大部分IP不会有服务器）
                null
            }
        }
        
        /**
         * 获取设备当前IP地址
         */
        fun getCurrentDeviceIP(context: Context): String? {
            try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val ipAddress = wifiInfo.ipAddress
                
                return String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            } catch (e: Exception) {
                Log.e(TAG, "获取设备IP失败: ${e.message}")
                return null
            }
        }
    }
} 