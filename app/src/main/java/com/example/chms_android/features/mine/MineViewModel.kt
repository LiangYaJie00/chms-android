package com.example.chms_android.features.mine

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chms_android.repo.OssRepo
import com.example.chms_android.repo.UserRepo
import com.example.chms_android.utils.AccountUtil
import kotlinx.coroutines.launch

class MineViewModel : ViewModel() {
    
    private val _uploadStatus = MutableLiveData<UploadStatus>()
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus
    
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
                    UserRepo.updateUser(user, context)
                    _uploadStatus.value = UploadStatus.Success(imageUrl)
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
        data class Error(val message: String) : UploadStatus()
    }
}