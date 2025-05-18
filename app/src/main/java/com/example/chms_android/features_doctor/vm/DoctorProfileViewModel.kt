package com.example.chms_android.features_doctor.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chms_android.data.User
import com.example.chms_android.utils.AccountUtil
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DoctorProfileViewModel : ViewModel() {
    
    private val _patientsCount = MutableLiveData<Int>()
    val patientsCount: LiveData<Int> = _patientsCount
    
    private val _appointmentsCount = MutableLiveData<Int>()
    val appointmentsCount: LiveData<Int> = _appointmentsCount
    
    private val _reportsCount = MutableLiveData<Int>()
    val reportsCount: LiveData<Int> = _reportsCount
    
    // 添加用户数据的LiveData
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    init {
        // 注册EventBus
        EventBus.getDefault().register(this)
    }
    
    // 接收用户更新事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(user: User) {
        // 更新ViewModel中的用户数据
        _currentUser.value = user
    }
    
    // 加载用户信息
    fun loadUserInfo(context: Context) {
        val user = AccountUtil(context).getUser()
        _currentUser.value = user
    }
    
    // 加载统计数据
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                // 这里应该从API获取实际数据
                // 暂时使用模拟数据
                _patientsCount.value = 15
                _appointmentsCount.value = 8
                _reportsCount.value = 5
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 注销EventBus
        EventBus.getDefault().unregister(this)
    }
}