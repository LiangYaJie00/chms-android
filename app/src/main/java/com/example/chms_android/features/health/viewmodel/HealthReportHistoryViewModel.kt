package com.example.chms_android.features.health.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.repo.DailyHealthReportRepo

class HealthReportHistoryViewModel : ViewModel() {
    
    // 健康日报列表
    private val _healthReports = MutableLiveData<List<DailyHealthReport>>()
    val healthReports: LiveData<List<DailyHealthReport>> = _healthReports
    
    // 加载状态
    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus> = _loadStatus
    
    // 加载状态枚举
    sealed class LoadStatus {
        object Loading : LoadStatus()
        object Success : LoadStatus()
        data class Error(val message: String) : LoadStatus()
    }
    
    // 加载用户的所有健康日报
    fun loadUserHealthReports(userId: Int, context: Context) {
        _loadStatus.value = LoadStatus.Loading
        
        // 先从本地数据库获取数据
        val localReports = DailyHealthReportRepo.getLocalDailyHealthReportsByUserId(userId)
        if (localReports.isNotEmpty()) {
            _healthReports.postValue(localReports)
        }
        
        // 从网络获取最新数据
        DailyHealthReportRepo.getReportsByUserId(
            userId,
            context,
            onSuccess = { reports ->
                _healthReports.postValue(reports)
                _loadStatus.postValue(LoadStatus.Success)
            },
            onError = { message ->
                // 如果本地有数据，则显示本地数据
                if (localReports.isNotEmpty()) {
                    _loadStatus.postValue(LoadStatus.Error("网络请求失败，显示本地缓存数据: $message"))
                } else {
                    _loadStatus.postValue(LoadStatus.Error(message))
                }
            }
        )
    }
}