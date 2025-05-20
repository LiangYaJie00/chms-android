package com.example.chms_android.features.mine

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chms_android.MainActivity
import com.example.chms_android.data.User
import com.example.chms_android.repo.OssRepo
import com.example.chms_android.repo.UserRepo
import com.example.chms_android.utils.AccountUtil
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MineViewModel : ViewModel() {
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    
    private val _uploadStatus = MutableLiveData<UploadStatus>()
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus
    
    // 离线模式相关
    private val _isOfflineMode = MutableLiveData<Boolean>()
    val isOfflineMode: LiveData<Boolean> = _isOfflineMode
    
    private val _connectionRetryResult = MutableLiveData<Boolean>()
    val connectionRetryResult: LiveData<Boolean> = _connectionRetryResult
    
    init {
        // 注册EventBus
        EventBus.getDefault().register(this)
    }
    
    override fun onCleared() {
        super.onCleared()
        // 注销EventBus
        EventBus.getDefault().unregister(this)
    }
    
    // 接收用户更新事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(user: User) {
        // 直接更新ViewModel中的用户数据
        _user.value = user
    }
    
    // 加载用户信息
    fun loadUserInfo(context: Context) {
        val user = AccountUtil(context).getUser()
        _user.value = user
    }
    
    // 退出登录
    fun logout(context: Context) {
        AccountUtil(context).logout(context)
        _user.value = null
    }
    
    // 上传头像
    fun uploadAvatar(context: Context, imageUri: Uri) {
        _uploadStatus.value = UploadStatus.Loading
        
        viewModelScope.launch {
            try {
                // 使用协程方法上传图片
                val imageUrl = OssRepo.uploadImageCoroutine(context, imageUri)
                
                // 更新用户头像URL
                val user = AccountUtil(context).getUser()
                if (user != null) {
                    user.avatar = imageUrl
                    UserRepo.updateUser(user, context, onSuccess = { updatedUser ->
                        _uploadStatus.value = UploadStatus.Success(imageUrl)
                    }, onFailure = { errorMsg ->
                        _uploadStatus.value = UploadStatus.Error(errorMsg)
                    })
                } else {
                    _uploadStatus.value = UploadStatus.Error("用户信息不存在")
                }
            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus.Error(e.message ?: "上传失败")
            }
        }
    }
    
    // 上传状态封装类
    sealed class UploadStatus {
        object Loading : UploadStatus()
        data class Success(val imageUrl: String) : UploadStatus()
        object UpdateSuccess : UploadStatus()
        data class Error(val message: String) : UploadStatus()
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
        private const val TAG = "MineViewModel"
    }
}
