package com.example.chms_android.features.health.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.repo.HealthInfoRepo
import com.example.chms_android.utils.AccountUtil
import java.math.BigDecimal
import java.sql.Timestamp

class BluetoothHealthViewModel : ViewModel() {
    private val TAG = "BluetoothHealthViewModel"
    
    // 保存状态
    sealed class SaveStatus {
        object Idle : SaveStatus()
        object Loading : SaveStatus()
        object Success : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }
    
    private val _saveStatus = MutableLiveData<SaveStatus>(SaveStatus.Idle)
    val saveStatus: LiveData<SaveStatus> = _saveStatus
    
    /**
     * 保存从蓝牙设备获取的健康数据
     */
    fun saveHealthData(
        heartRate: Int,
        systolicPressure: Int,
        diastolicPressure: Int,
        oxygenLevel: Float,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            _saveStatus.value = SaveStatus.Loading
            
            // 获取当前用户ID
            val userId = AccountUtil(context).getUser()?.userId
            if (userId == null) {
                val errorMsg = "未登录，无法保存健康数据"
                _saveStatus.value = SaveStatus.Error(errorMsg)
                onError(errorMsg)
                return
            }
            
            // 创建健康信息对象
            val healthInfo = HealthInfo(
                healthId = 0, // 新建记录，ID由服务器生成
                userId = userId,
                weight = null, // 这些字段不是从蓝牙设备获取的，设为null
                height = null,
                bmi = null,
                bloodPressureSystolic = systolicPressure,
                bloodPressureDiastolic = diastolicPressure,
                lastMedicalCheckup = null,
                allergies = null,
                medicalHistory = null,
                familyMedicalHistory = null,
                currentConditions = null,
                medications = null,
                vaccinationRecords = null,
                lifestyleHabits = null,
                exerciseFrequency = null,
                dietaryPreferences = null,
                heartRate = heartRate,
                arterialBloodOxygenLevel = BigDecimal(oxygenLevel.toString()),
                venousBloodOxygenLevel = null,
                createdAt = Timestamp(System.currentTimeMillis()),
                updatedAt = Timestamp(System.currentTimeMillis())
            )
            
            // 保存到服务器
            HealthInfoRepo.addOrUpdateHealthInfo(
                healthInfo,
                context,
                onSuccess = {
                    _saveStatus.postValue(SaveStatus.Success)
                    onSuccess()
                },
                onError = { errorMsg ->
                    _saveStatus.postValue(SaveStatus.Error(errorMsg))
                    onError(errorMsg)
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "保存健康数据失败", e)
            val errorMsg = "保存健康数据失败: ${e.message}"
            _saveStatus.value = SaveStatus.Error(errorMsg)
            onError(errorMsg)
        }
    }
}