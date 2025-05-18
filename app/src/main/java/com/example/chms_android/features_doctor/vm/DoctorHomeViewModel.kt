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

class DoctorHomeViewModel : ViewModel() {
    
    private val _todayAppointments = MutableLiveData<Int>(0)
    val todayAppointments: LiveData<Int> = _todayAppointments
    
    private val _todayPatients = MutableLiveData<Int>(0)
    val todayPatients: LiveData<Int> = _todayPatients
    
    private val _pendingReports = MutableLiveData<Int>(0)
    val pendingReports: LiveData<Int> = _pendingReports
    
    // 添加用户数据的LiveData
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    init {
        // 注册EventBus
        EventBus.getDefault().register(this)
        loadDoctorHomeData()
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
    
    private fun loadDoctorHomeData() {
        viewModelScope.launch {
            try {
                // 这里应该从API获取实际数据
                // 暂时使用模拟数据
                _todayAppointments.value = 5
                _todayPatients.value = 12
                _pendingReports.value = 3
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    // 刷新数据
    fun refreshData() {
        loadDoctorHomeData()
    }
    
    override fun onCleared() {
        super.onCleared()
        // 注销EventBus
        EventBus.getDefault().unregister(this)
    }
}