package com.example.mad_gruop_ass.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

object NetworkUtils {
    
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
}

