package com.example.chms_android.features.health.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.api.HealthInfoApi
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.repo.HealthInfoRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.DateUtil
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale

class HealthInfoViewModel : ViewModel() {
    
    private val _healthInfo = MutableLiveData<HealthInfo?>()
    val healthInfo: LiveData<HealthInfo?> = _healthInfo
    
    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus> = _loadStatus

    // 加载健康信息
    fun loadHealthInfo(context: Context) {
        // 显示加载状态
        _loadStatus.value = LoadStatus.Loading

        HealthInfoRepo.getHealthInfoOfMine(
            context,
            onSuccess = { healthInfo ->
                _healthInfo.postValue(healthInfo)
                _loadStatus.postValue(LoadStatus.Success)
            },
            onError = { errorMessage ->
                // 如果API调用失败，尝试使用模拟数据（仅在没有获取到数据时）
                if (_healthInfo.value == null) {
                    createMockHealthInfo(context)
                }
                _loadStatus.postValue(LoadStatus.Error(errorMessage))
            }
        )
    }

    // 创建模拟健康信息数据（仅在无法从API获取数据时使用）
    private fun createMockHealthInfo(context: Context) {
        val user = AccountUtil(context).getUser()
        if (user != null) {
            _healthInfo.value = HealthInfo(
                healthId = 0,
                userId = user.userId ?: 0,
                weight = BigDecimal("70.0"),
                height = BigDecimal("175.0"),
                bmi = BigDecimal("22.9"),
                bloodPressureSystolic = 120,
                bloodPressureDiastolic = 80,
                lastMedicalCheckup = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("2023-05-15"),
                allergies = "无",
                medicalHistory = "无",
                familyMedicalHistory = "父亲高血压",
                currentConditions = "无",
                medications = "无",
                vaccinationRecords = "已完成新冠疫苗全程接种",
                lifestyleHabits = "规律作息，不吸烟不饮酒",
                exerciseFrequency = "每周3-4次",
                dietaryPreferences = "清淡饮食，多吃蔬果",
                heartRate = 75,
                arterialBloodOxygenLevel = BigDecimal("98.0"),
                venousBloodOxygenLevel = BigDecimal("75.0"),
                createdAt = DateUtil.getCurrentTimestamp(),
                updatedAt = DateUtil.getCurrentTimestamp()
            )
        }
    }
    
    // 保存健康信息
    fun saveHealthInfo(context: Context, healthInfo: HealthInfo) {
        _saveStatus.value = SaveStatus.Loading
        
        // 设置用户ID
        val userId = AccountUtil(context).getUserId()
        healthInfo.userId = userId.toInt()
        
        // 更新时间戳
        healthInfo.updatedAt = DateUtil.getCurrentTimestamp()
        
        // 调用API保存健康信息
        HealthInfoRepo.addOrUpdateHealthInfo(
            healthInfo, 
            context,
            onSuccess = { updatedHealthInfo ->
                _saveStatus.postValue(SaveStatus.Success)
                // 更新本地缓存的健康信息
                _healthInfo.postValue(updatedHealthInfo)
            },
            onError = { errorMessage ->
                _saveStatus.postValue(SaveStatus.Error(errorMessage))
            }
        )
    }

    // 加载状态
    sealed class LoadStatus {
        object Loading : LoadStatus()
        object Success : LoadStatus()
        data class Error(val message: String) : LoadStatus()
    }

    // 保存状态
    sealed class SaveStatus {
        object Loading : SaveStatus()
        object Success : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }
}