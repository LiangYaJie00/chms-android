package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.api.DailyHealthReportApi
import com.example.chms_android.dao.DailyHealthReportDao
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailyHealthReportRepo {
    private val TAG = "DailyHealthReportRepo"
    private val dailyHealthReportDao: DailyHealthReportDao get() = DatabaseProvider.getDatabase().dailyHealthReportDao()

    fun addDailyHealthReport(dailyHealthReport: DailyHealthReport, context: Context) {
        DailyHealthReportApi.addDailyHealthReport(dailyHealthReport, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<DailyHealthReport>>() {},
                    onSuccess = { dailyHealthReport ->
                        dailyHealthReportDao.insertDailyHealthReport(dailyHealthReport)
                    },
                    onError = { message ->

                    },
                    successToastMessage = "本日日报上报成功",
                    errorToastMessage = "日报上报失败"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                // 弹出接口请求失败的提示框
                ToastUtil.show(context, "Network error: ${e.message}", Toast.LENGTH_SHORT)
            }
        })
    }
    
    /**
     * 添加或更新健康日报
     * 
     * @param dailyHealthReport 健康日报对象
     * @param context 上下文
     * @param onSuccess 成功回调，返回更新后的健康日报
     * @param onError 错误回调，返回错误信息
     */
    fun addOrUpdateDailyHealthReport(
        dailyHealthReport: DailyHealthReport, 
        context: Context,
        onSuccess: ((DailyHealthReport) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DailyHealthReportApi.addOrUpdateDailyHealthReport(dailyHealthReport, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<DailyHealthReport>>() {},
                    onSuccess = { updatedReport ->
                        // 保存到本地数据库
                        dailyHealthReportDao.insertDailyHealthReport(updatedReport)
                        // 调用成功回调
                        onSuccess?.invoke(updatedReport)
                        // 显示成功提示
                        ToastUtil.show(context, "健康日报保存/更新成功!", Toast.LENGTH_SHORT)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                        // 显示错误提示
                        ToastUtil.show(context, message, Toast.LENGTH_SHORT)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = "健康日报保存/更新失败！"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 调用错误回调
                onError?.invoke("网络请求失败: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
            }
        })
    }

    /**
     * 根据用户ID和报告日期获取健康报告
     * 
     * @param userId 用户ID
     * @param reportDate 报告日期，格式为 "yyyy-MM-dd"
     * @param context 上下文
     * @param onSuccess 成功回调，返回健康报告
     * @param onError 错误回调，返回错误信息
     */
    fun getReportByUserIdAndDate(
        userId: Int,
        reportDate: String,
        context: Context,
        onSuccess: ((DailyHealthReport) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DailyHealthReportApi.getReportByUserIdAndDate(userId, reportDate, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<DailyHealthReport>>() {},
                    onSuccess = { report ->
                        // 保存到本地数据库
                        dailyHealthReportDao.insertDailyHealthReport(report)
                        // 调用成功回调
                        onSuccess?.invoke(report)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = null
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 调用错误回调
                onError?.invoke("网络请求失败: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
            }
        })
    }

    /**
     * 获取某个用户的所有健康报告
     * 
     * @param userId 用户ID
     * @param context 上下文
     * @param onSuccess 成功回调，返回健康报告列表
     * @param onError 错误回调，返回错误信息
     */
    fun getReportsByUserId(
        userId: Int,
        context: Context,
        onSuccess: ((List<DailyHealthReport>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DailyHealthReportApi.getReportsByUserId(userId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<DailyHealthReport>>>() {},
                    onSuccess = { reports ->
                        // 保存到本地数据库
                        reports.forEach { report ->
                            dailyHealthReportDao.insertDailyHealthReport(report)
                        }
                        // 调用成功回调
                        onSuccess?.invoke(reports)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = null
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 调用错误回调
                onError?.invoke("网络请求失败: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
            }
        })
    }

    /**
     * 根据ID获取单个健康报告
     * 
     * @param reportId 报告ID
     * @param context 上下文
     * @param onSuccess 成功回调，返回健康报告
     * @param onError 错误回调，返回错误信息
     */
    fun getDailyHealthReportById(
        reportId: Int,
        context: Context,
        onSuccess: ((DailyHealthReport) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DailyHealthReportApi.getReportById(reportId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<DailyHealthReport>>() {},
                    onSuccess = { report ->
                        // 保存到本地数据库
                        dailyHealthReportDao.insertDailyHealthReport(report)
                        // 调用成功回调
                        onSuccess?.invoke(report)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = null
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 调用错误回调
                onError?.invoke("网络请求失败: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
            }
        })
    }

    /**
     * 根据ID获取本地数据库中的健康报告
     * 
     * @param reportId 报告ID
     * @return 健康报告，如果不存在则返回null
     */
    fun getLocalDailyHealthReportById(reportId: Int): DailyHealthReport? {
        return try {
            dailyHealthReportDao.getDailyHealthReportById(reportId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local report: ${e.message}")
            null
        }
    }

    /**
     * 根据用户ID和日期获取本地数据库中的健康报告
     * 
     * @param userId 用户ID
     * @param reportDate 报告日期，格式为 "yyyy-MM-dd"
     * @return 健康报告，如果不存在则返回null
     */
    fun getLocalDailyHealthReportByUserIdAndDate(userId: Int, reportDate: String): DailyHealthReport? {
        return try {
            dailyHealthReportDao.getDailyHealthReportByUserIdAndDate(userId, reportDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local report: ${e.message}")
            null
        }
    }

    /**
     * 检查用户当日是否已有健康报告
     * 
     * @param userId 用户ID
     * @param context 上下文
     * @param onSuccess 成功回调，返回布尔值表示是否存在报告
     * @param onError 错误回调，返回错误信息
     */
    fun checkTodayReport(
        userId: Int,
        context: Context,
        onSuccess: ((Boolean) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DailyHealthReportApi.checkTodayReport(userId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<Boolean>>() {},
                    onSuccess = { exists ->
                        // 调用成功回调
                        onSuccess?.invoke(exists)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                    },
                    // 不显示默认的Toast消息，由回调处理
                    successToastMessage = null,
                    errorToastMessage = null
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                // 调用错误回调
                onError?.invoke("网络请求失败: ${e.message}")
                // 使用网络工具类处理错误
                NetworkUtil.handleNetworkError(TAG, context, e)
                
                // 尝试从本地数据库检查
                try {
                    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val localReport = getLocalDailyHealthReportByUserIdAndDate(userId, todayDateStr)
                    // 如果本地有数据，则认为存在报告
                    onSuccess?.invoke(localReport != null)
                } catch (ex: Exception) {
                    Log.e(TAG, "Error checking local report: ${ex.message}")
                    onError?.invoke("检查本地报告失败: ${ex.message}")
                }
            }
        })
    }
}
