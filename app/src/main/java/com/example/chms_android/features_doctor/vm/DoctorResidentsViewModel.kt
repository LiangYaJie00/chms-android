package com.example.chms_android.features_doctor.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chms_android.data.User
import com.example.chms_android.repo.UserRepo
import kotlinx.coroutines.launch

class DoctorResidentsViewModel : ViewModel() {

    private val _residents = MutableLiveData<List<User>>()
    val residents: LiveData<List<User>> = _residents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // 加载指定社区的居民数据
    fun loadResidents(context: Context, community: String) {
        _isLoading.value = true
        _error.value = ""
        
        // 先尝试从本地数据库加载
        loadResidentsFromDb(community)
        
        // 然后从网络获取最新数据
        UserRepo.getUsersByRoleAndCommunity(
            role = "consumer",
            community = community,
            context = context,
            onSuccess = { users ->
                _residents.value = users
                _isLoading.value = false
            },
            onError = { errorMsg ->
                // 如果网络请求失败但已经从本地加载了数据，不显示错误
                if (_residents.value.isNullOrEmpty()) {
                    _error.value = errorMsg
                }
                _isLoading.value = false
            }
        )
    }
    
    // 从本地数据库加载居民数据
    private fun loadResidentsFromDb(community: String) {
        viewModelScope.launch {
            try {
                UserRepo.getUsersByRoleAndCommunityFromDb(
                    role = "consumer",
                    community = community,
                    onSuccess = { users ->
                        if (users.isNotEmpty()) {
                            _residents.value = users
                        }
                    },
                    onError = { /* 忽略本地数据库错误，等待网络请求结果 */ }
                )
            } catch (e: Exception) {
                // 忽略本地数据库错误，等待网络请求结果
            }
        }
    }
    
    // 搜索居民
    fun searchResidents(query: String) {
        val currentResidents = _residents.value ?: return
        
        if (query.isEmpty()) {
            // 如果搜索词为空，显示所有居民
            _residents.value = currentResidents
        } else {
            // 否则过滤居民列表
            val filteredResidents = currentResidents.filter { 
                it.name?.contains(query, ignoreCase = true) == true || 
                it.phone?.toString()?.contains(query) == true ||
                it.email?.contains(query, ignoreCase = true) == true
            }
            _residents.value = filteredResidents
        }
    }
}