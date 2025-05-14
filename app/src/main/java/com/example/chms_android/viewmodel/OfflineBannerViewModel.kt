package com.example.chms_android.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.MainActivity

/**
 * 离线模式横幅的ViewModel，处理离线状态检测和状态管理
 */
class OfflineBannerViewModel : ViewModel() {
    
    // 是否应该显示离线横幅
    private val _shouldShowBanner = MutableLiveData<Boolean>()
    val shouldShowBanner: LiveData<Boolean> = _shouldShowBanner
    
    // 重试连接的结果
    private val _connectionRetryResult = MutableLiveData<Boolean>()
    val connectionRetryResult: LiveData<Boolean> = _connectionRetryResult
    
    // 检查是否处于离线模式
    fun checkOfflineMode(context: Context) {
        try {
            val mainActivity = context as? MainActivity
            if (mainActivity != null) {
                val isOffline = mainActivity.isInOfflineMode()
                Log.d(TAG, "Checking offline mode: isOffline=$isOffline")
                _shouldShowBanner.value = isOffline
            } else {
                Log.e(TAG, "Context is not MainActivity")
                _shouldShowBanner.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking offline mode: ${e.message}")
            e.printStackTrace()
            _shouldShowBanner.value = false
        }
    }
    
    // 尝试重新连接服务器
    fun retryConnection(context: Context) {
        try {
            val mainActivity = context as? MainActivity
            if (mainActivity != null) {
                mainActivity.checkServerAndReconnect { isConnected ->
                    _connectionRetryResult.value = isConnected
                    if (isConnected) {
                        _shouldShowBanner.value = false
                    }
                }
            } else {
                Log.e(TAG, "Context is not MainActivity")
                _connectionRetryResult.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrying connection: ${e.message}")
            e.printStackTrace()
            _connectionRetryResult.value = false
        }
    }
    
    companion object {
        private const val TAG = "OfflineBannerViewModel"
    }
}