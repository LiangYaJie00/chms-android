package com.example.chms_android.features.mine.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chms_android.data.User
import com.example.chms_android.repo.OssRepo
import com.example.chms_android.repo.UserRepo
import com.example.chms_android.utils.AccountUtil
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UserEditViewModel : ViewModel() {
    
    private val _uploadStatus = MutableLiveData<UploadStatus>()
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    
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
        
        if (user == null) {
            _uploadStatus.value = UploadStatus.Error("无法获取用户信息")
        }
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
                        _user.value = user
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
    
    // 更新用户信息
    fun updateUserInfo(
        context: Context,
        name: String,
        ageStr: String,
        genderStr: String,
        phoneStr: String,
        weightStr: String,
        heightStr: String
    ) {
        val user = _user.value
        if (user == null) {
            _uploadStatus.value = UploadStatus.Error("用户信息不存在")
            return
        }
        
        try {
            // 更新用户对象
            user.name = name
            user.age = ageStr.toIntOrNull() ?: user.age
            user.gender = if (genderStr == "男") 1 else 0
            user.phone = phoneStr.toLongOrNull() ?: user.phone
            user.weight = weightStr.toIntOrNull() ?: user.weight
            user.height = heightStr.toIntOrNull() ?: user.height
            
            // 保存到服务器
            UserRepo.updateUser(user, context, onSuccess = { updatedUser ->
                // 更新LiveData
                _user.value = user
                _uploadStatus.value = UploadStatus.UpdateSuccess
            }, onFailure = { errorMsg ->
                _uploadStatus.value = UploadStatus.Error(errorMsg)
            })
        } catch (e: Exception) {
            _uploadStatus.value = UploadStatus.Error(e.message ?: "更新失败")
        }
    }
    
    // 上传状态封装类
    sealed class UploadStatus {
        object Loading : UploadStatus()
        data class Success(val imageUrl: String) : UploadStatus()
        object UpdateSuccess : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }
}