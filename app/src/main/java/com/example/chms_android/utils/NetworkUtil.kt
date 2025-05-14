package com.example.chms_android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.chms_android.CHMSApplication
import com.example.chms_android.common.Constants
import com.google.gson.Gson
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

object NetworkUtil {
    private const val TAG = "NetworkUtil"
    
    // 检查网络是否可用
    fun isNetworkAvailable(): Boolean {
        val context = CHMSApplication.getInstance()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    // 处理网络错误
    fun handleNetworkError(tag: String, context: Context, e: IOException): String {
        // 创建错误信息的结构化数据
        val errorData = mapOf(
            "error_type" to e.javaClass.simpleName,
            "error_message" to e.message,
            "error_cause" to e.cause?.toString(),
            "stack_trace" to e.stackTraceToString().take(500),
            "timestamp" to System.currentTimeMillis(),
            "network_available" to isNetworkAvailable()
        )
        
        // 转换为JSON
        val gson = Gson()
        val errorJson = gson.toJson(errorData)
        Log.e("${TAG}-${tag}", "Network error JSON: $errorJson")
        
        // 根据错误类型生成用户友好的错误消息
        val userMessage = when (e) {
            is ConnectException -> {
                if (e.message?.contains("EHOSTUNREACH") == true) {
                    "无法连接到服务器(${Constants.BASE_URL})，请检查网络设置或联系管理员"
                } else {
                    "连接服务器失败，请稍后重试"
                }
            }
            is SocketTimeoutException -> "连接服务器超时，请检查网络或稍后重试"
            is UnknownHostException -> "找不到服务器地址，请检查网络设置"
            else -> "网络错误: ${e.message}"
        }
        
        // 显示错误消息
        ToastUtil.show(context, userMessage, Toast.LENGTH_LONG)
        return userMessage
    }
    
    // 检查服务器是否可达
    fun checkServerReachable(serverUrl: String): Boolean {
        return try {
            val url = URL(serverUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            val responseCode = connection.responseCode
            Log.d(TAG, "Server response code: $responseCode")
            connection.disconnect()

            // 2xx 或 3xx 状态码表示服务器可达
            responseCode in 200..399
        } catch (e: IOException) {
            Log.e(TAG, "Server unreachable: $serverUrl", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking server: $serverUrl", e)
            false
        }
    }
}