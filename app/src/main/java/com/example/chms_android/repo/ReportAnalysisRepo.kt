package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.api.ReportAnalysisApi
import com.example.chms_android.dao.ReportAnalysisDao
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ReportAnalysisRepo {
    private val TAG = "ReportAnalysisRepo"
    private val reportAnalysisDao: ReportAnalysisDao get() = DatabaseProvider.getDatabase().reportAnalysisDao()

    /**
     * 根据报告ID获取分析报告
     * 
     * @param reportId 报告ID
     * @param context 上下文
     * @param onSuccess 成功回调，返回分析报告
     * @param onError 错误回调，返回错误信息
     */
    fun getReportAnalysisByReportId(
        reportId: Long,
        context: Context,
        onSuccess: ((ReportAnalysis) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        ReportAnalysisApi.getReportAnalysisByReportId(reportId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<ReportAnalysis>>() {},
                    onSuccess = { analysis ->
                        // 保存到本地数据库
                        reportAnalysisDao.insertReportAnalysis(analysis)
                        // 调用成功回调
                        onSuccess?.invoke(analysis)
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
                
                // 尝试从本地数据库获取
                try {
                    val localAnalysis = getLocalReportAnalysisByReportId(reportId)
                    if (localAnalysis != null) {
                        onSuccess?.invoke(localAnalysis)
                    } else {
                        onError?.invoke("本地无缓存数据")
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Error getting local analysis: ${ex.message}")
                    onError?.invoke("获取本地分析报告失败: ${ex.message}")
                }
            }
        })
    }
    
    /**
     * 分析健康日报（直接发送日报数据进行分析）
     * 注意：此接口响应时间较长，约需10秒
     * 
     * @param dailyHealthReport 健康日报对象
     * @param context 上下文
     * @param onStart 开始分析回调
     * @param onSuccess 成功回调，返回分析报告
     * @param onError 错误回调，返回错误信息
     */
    fun analyzeDailyHealthReport(
        dailyHealthReport: DailyHealthReport,
        context: Context,
        onStart: (() -> Unit)? = null,
        onSuccess: ((ReportAnalysis) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 调用开始分析回调
        onStart?.invoke()
        
        // 先检查本地是否已有分析结果
        val localAnalysis = getLocalReportAnalysisByReportId(dailyHealthReport.reportId.toLong())
        if (localAnalysis != null) {
            Log.i(TAG, "Found local analysis result, using cached data")
            onSuccess?.invoke(localAnalysis)
            return
        }
        
        // 显示正在分析的提示
        ToastUtil.show(context, "正在进行AI分析，请耐心等待...", Toast.LENGTH_LONG)
        
        ReportAnalysisApi.analyzeDailyHealthReport(dailyHealthReport, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<ReportAnalysis>>() {},
                    onSuccess = { analysis ->
                        // 保存分析结果到本地数据库
                        reportAnalysisDao.insertReportAnalysis(analysis)
                        
                        // 从后端获取最新的DailyHealthReport（包含更新后的isAnalysed字段）
                        DailyHealthReportRepo.getDailyHealthReportById(
                            dailyHealthReport.reportId,
                            context,
                            onSuccess = { updatedReport ->
                                Log.i(TAG, "Updated report with isAnalysed=${updatedReport.isAnalysed}")
                            },
                            onError = { message ->
                                Log.e(TAG, "Failed to get updated report: $message")
                            }
                        )
                        
                        // 调用成功回调
                        onSuccess?.invoke(analysis)
                    },
                    onError = { message ->
                        // 调用错误回调
                        onError?.invoke(message)
                    },
                    successToastMessage = "AI分析完成",
                    errorToastMessage = "AI分析失败，请稍后重试"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error during AI analysis: ${e.message}")
                // 调用错误回调
                onError?.invoke("AI分析请求失败: ${e.message}")
                // 显示错误提示
                ToastUtil.show(context, "AI分析请求失败，请检查网络连接后重试", Toast.LENGTH_SHORT)
            }
        })
    }

    /**
     * 从本地数据库获取分析报告
     * 
     * @param reportId 报告ID
     * @return 分析报告，如果不存在则返回null
     */
    fun getLocalReportAnalysisByReportId(reportId: Long): ReportAnalysis? {
        return try {
            reportAnalysisDao.getReportAnalysisByReportId(reportId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local analysis: ${e.message}")
            null
        }
    }

    /**
     * 获取用户的所有分析报告
     * 
     * @param userId 用户ID
     * @param context 上下文
     * @param onSuccess 成功回调，返回分析报告列表
     * @param onError 错误回调，返回错误信息
     */
    fun getUserReportAnalysis(
        userId: Int,
        context: Context,
        onSuccess: ((List<ReportAnalysis>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 使用协程在IO线程执行数据库操作
        CoroutineScope(Dispatchers.IO).launch {
            // 1. 尝试从本地获取数据
            val localAnalysisList = getLocalAnalysisList(userId)
            
            // 2. 如果本地有数据，先返回
            if (localAnalysisList.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    onSuccess?.invoke(localAnalysisList)
                }
            }
            
            // 3. 从网络获取最新数据
            withContext(Dispatchers.Main) {
                fetchRemoteAnalysisList(userId, context, localAnalysisList, onSuccess, onError)
            }
        }
    }

    // 从本地数据库获取分析报告列表
    private fun getLocalAnalysisList(userId: Int): List<ReportAnalysis> {
        return try {
            reportAnalysisDao.getReportAnalysisByUserId(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local analysis list: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // 从网络获取分析报告列表
    private fun fetchRemoteAnalysisList(
        userId: Int,
        context: Context,
        localAnalysisList: List<ReportAnalysis>,
        onSuccess: ((List<ReportAnalysis>) -> Unit)?,
        onError: ((String) -> Unit)?
    ) {
        ReportAnalysisApi.getUserReportAnalysis(userId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<ReportAnalysis>>>() {},
                    onSuccess = { analysisList ->
                        // 保存到本地数据库
                        saveAnalysisListToLocal(analysisList, onSuccess)
                    },
                    onError = { message ->
                        // 如果网络请求失败，但本地有数据，不返回错误
                        if (localAnalysisList.isEmpty()) {
                            onError?.invoke(message)
                        }
                    },
                    successToastMessage = null,
                    errorToastMessage = null
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Network error: ${e.message}")
                NetworkUtil.handleNetworkError(TAG, context, e)
                
                // 如果网络请求失败，但本地有数据，不返回错误
                if (localAnalysisList.isEmpty()) {
                    onError?.invoke("网络请求失败: ${e.message}")
                }
            }
        })
    }

    // 保存分析报告列表到本地数据库
    private fun saveAnalysisListToLocal(
        analysisList: List<ReportAnalysis>,
        onSuccess: ((List<ReportAnalysis>) -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (analysis in analysisList) {
                    reportAnalysisDao.insertReportAnalysis(analysis)
                }
                
                // 切换到主线程返回结果
                withContext(Dispatchers.Main) {
                    onSuccess?.invoke(analysisList)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving analysis list: ${e.message}")
                e.printStackTrace()
                
                // 即使保存失败，也返回网络获取的数据
                withContext(Dispatchers.Main) {
                    onSuccess?.invoke(analysisList)
                }
            }
        }
    }

}
