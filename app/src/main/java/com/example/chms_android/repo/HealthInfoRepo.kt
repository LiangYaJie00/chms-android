package com.example.chms_android.repo

import android.content.Context
import android.widget.Toast
import com.example.chms_android.api.HealthInfoApi
import com.example.chms_android.dao.HealthInfoDao
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.google.gson.reflect.TypeToken
import java.io.IOException

object HealthInfoRepo {
    private val healthInfoDao: HealthInfoDao get() = DatabaseProvider.getDatabase().healthInfoDao()

    fun addHealthInfo(healthInfo: HealthInfo, context: Context) {
        HealthInfoApi.addHealthInfo(healthInfo, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<HealthInfo>>() {},
                    onSuccess = { healthInfo ->
                        // 假设 you have a healthInfoDao 可供使用
                        healthInfoDao.insertHealthInfo(healthInfo)
                    },
                    onError = { message ->
                        // 处理具体的错误消息
                    },
                    successToastMessage = "addHealthInfo successful: 健康信息添加成功!",
                    errorToastMessage = "addHealthInfo failed"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                // 弹出接口请求失败的提示框
                ToastUtil.show(context, "Network error: ${e.message}", Toast.LENGTH_SHORT)
            }

        })
    }
}