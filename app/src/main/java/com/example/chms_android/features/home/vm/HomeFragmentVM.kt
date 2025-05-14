package com.example.chms_android.features.home.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.MainActivity
import com.example.chms_android.data.Doctor

class HomeFragmentVM(): ViewModel() {
    val doctorList: LiveData<List<Doctor>> get() = _doctorList
    private val _doctorList = MutableLiveData<List<Doctor>>()
    
    // 离线模式相关
    private val _isOfflineMode = MutableLiveData<Boolean>()
    val isOfflineMode: LiveData<Boolean> = _isOfflineMode
    
    private val _connectionRetryResult = MutableLiveData<Boolean>()
    val connectionRetryResult: LiveData<Boolean> = _connectionRetryResult

    fun setDoctorList(doctorList: List<Doctor>) {
        _doctorList.value = doctorList
    }
    
    // 检查是否处于离线模式
    fun checkOfflineMode(context: Context) {
        try {
            val mainActivity = context as? MainActivity
            if (mainActivity != null) {
                val isOffline = mainActivity.isInOfflineMode()
                Log.d(TAG, "Checking offline mode: isOffline=$isOffline")
                _isOfflineMode.value = isOffline
            } else {
                Log.e(TAG, "Context is not MainActivity")
                _isOfflineMode.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking offline mode: ${e.message}")
            e.printStackTrace()
            _isOfflineMode.value = false
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
                        _isOfflineMode.value = false
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
        private const val TAG = "HomeFragmentVM"
    }
}