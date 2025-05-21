package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.api.HealthInfoApi
import com.example.chms_android.dao.HealthInfoDao
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.google.gson.reflect.TypeToken
import java.io.IOException

object HealthInfoRepo {
    private val TAG = "HealthInfoRepo"
    private val healthInfoDao: HealthInfoDao get() = DatabaseProvider.getDatabase().healthInfoDao()

    fun addHealthInfo(healthInfo: HealthInfo, context: Context) {
        HealthInfoApi.addHealthInfo(healthInfo, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<HealthInfo>>() {},
                    onSuccess = { healthInfo ->
                        // 日报保存到本地
                        healthInfoDao.insertHealthInfo(healthInfo)
                    },
                    onError = { message ->
                        // 处理具体的错误消息
                        ToastUtil.show(context, message, Toast.LENGTH_SHORT)
                    },
                    successToastMessage = "健康日报上报成功!",
                    errorToastMessage = "日报上报失败！"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 弹出接口请求失败的提示框
                ToastUtil.show(context, "接口请求失败！", Toast.LENGTH_SHORT)
            }
        })
    }

    /**
     * 添加或更新健康信息
     * 
     * @param healthInfo 健康信息对象
     * @param context 上下文
     * @param onSuccess 成功回调，返回更新后的健康信息
     * @param onError 错误回调，返回错误信息
     */
    fun addOrUpdateHealthInfo(
        healthInfo: HealthInfo, 
        context: Context,
        onSuccess: ((HealthInfo) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        HealthInfoApi.addOrUpdateHealthInfo(healthInfo, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<HealthInfo>>() {},
                    onSuccess = { updatedHealthInfo ->
                        // 保存到本地数据库
                        healthInfoDao.insertHealthInfo(updatedHealthInfo)
                        // 调用成功回调
                        onSuccess?.invoke(updatedHealthInfo)
                        // 显示成功提示
                        ToastUtil.show(context, "健康信息保存/更新成功!", Toast.LENGTH_SHORT)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                        // 显示错误提示
                        ToastUtil.show(context, message, Toast.LENGTH_SHORT)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = "健康信息保存/更新失败！"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                // 调用错误回调
                onError?.invoke("网络请求失败: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
            }
        })
    }

    /**
     * 获取当前用户的健康信息
     * 
     * @param context 上下文
     * @param onSuccess 成功回调，返回健康信息
     * @param onError 错误回调，返回错误信息
     */
    fun getHealthInfoOfMine(
        context: Context,
        onSuccess: ((HealthInfo) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        HealthInfoApi.getHealthInfoOfMine(context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<HealthInfo>>() {},
                    onSuccess = { healthInfo ->
                        // 保存到本地数据库
                        healthInfoDao.insertHealthInfo(healthInfo)
                        // 调用成功回调
                        onSuccess?.invoke(healthInfo)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                        // 显示错误提示
                        ToastUtil.show(context, "获取健康信息失败: $message", Toast.LENGTH_SHORT)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = null
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
                // 尝试从本地数据库获取
                try {
                    val userId = AccountUtil(context).getUserId().toInt()
                    val localHealthInfo = healthInfoDao.getHealthInfoByUserId(userId)
                    if (localHealthInfo != null) {
                        Log.i(TAG, "Using cached health info data")
                        onSuccess?.invoke(localHealthInfo)
                    } else {
                        // 调用错误回调
                        onError?.invoke("网络请求失败: ${e.message}")
                        // 本地也没有数据
                        ToastUtil.show(context, "无法获取健康信息，请检查网络连接", Toast.LENGTH_SHORT)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    ToastUtil.show(context, "获取健康信息失败: ${ex.message}", Toast.LENGTH_SHORT)
                }
            }
        })
    }

    /**
     * 通过用户ID获取健康信息
     * 
     * @param userId 用户ID
     * @param context 上下文
     * @param onSuccess 成功回调，返回健康信息
     * @param onError 错误回调，返回错误信息
     */
    fun getHealthInfoByUserId(
        userId: Int,
        context: Context,
        onSuccess: ((HealthInfo) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        HealthInfoApi.getHealthInfoByUserId(userId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<HealthInfo>>() {},
                    onSuccess = { healthInfo ->
                        // 保存到本地数据库
                        healthInfoDao.insertHealthInfo(healthInfo)
                        // 调用成功回调
                        onSuccess?.invoke(healthInfo)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                        // 显示错误提示
                        ToastUtil.show(context, "获取用户健康信息失败: $message", Toast.LENGTH_SHORT)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = null
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
                // 尝试从本地数据库获取
                try {
                    val userIdInt = userId.toInt()
                    val localHealthInfo = healthInfoDao.getHealthInfoByUserId(userIdInt)
                    if (localHealthInfo != null) {
                        Log.i(TAG, "Using cached health info data for user $userId")
                        onSuccess?.invoke(localHealthInfo)
                    } else {
                        // 调用错误回调
                        onError?.invoke("网络请求失败: ${e.message}")
                        // 本地也没有数据
                        ToastUtil.show(context, "无法获取该用户健康信息，请检查网络连接", Toast.LENGTH_SHORT)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    onError?.invoke("获取健康信息失败: ${ex.message}")
                    ToastUtil.show(context, "获取用户健康信息失败: ${ex.message}", Toast.LENGTH_SHORT)
                }
            }
        })
    }
}