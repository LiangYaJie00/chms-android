package com.example.chms_android.features.health.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.repo.HealthInfoRepo

class UserHealthInfoViewModel : ViewModel() {
    private val healthInfoRepo = HealthInfoRepo

    fun getUserHealthInfo(userId: Int, context: Context, callback: (HealthInfo) -> Unit) {
        // 通过HealthInfoRepo获取指定用户的健康档案信息
        healthInfoRepo.getHealthInfoByUserId(
            userId = userId,
            context = context,
            onSuccess = { healthInfo ->
                callback(healthInfo)
            },
            onError = { errorMsg ->
                // 可以在这里处理错误情况，例如显示错误信息或提供默认数据
                // 如果需要，你可以添加一个额外的错误回调参数
            }
        )
    }
}