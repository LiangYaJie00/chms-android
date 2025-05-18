package com.example.chms_android.features.health.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.repo.DailyHealthReportRepo
import com.example.chms_android.utils.AccountUtil
import java.sql.Timestamp
import java.util.Date
import android.util.Log
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.repo.ReportAnalysisRepo

class DailyHealthReportViewModel : ViewModel() {
    
    // 健康日报数据 - 修改为可空类型
    private val _dailyHealthReport = MutableLiveData<DailyHealthReport?>()
    val dailyHealthReport: LiveData<DailyHealthReport?> = _dailyHealthReport
    
    // 加载状态
    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus> = _loadStatus
    
    // 保存状态
    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus
    
    // 报告是否已提交
    private val _isReportSubmitted = MutableLiveData<Boolean>(false)
    val isReportSubmitted: LiveData<Boolean> = _isReportSubmitted

    // 是否已经有了本日的日报
    private val _hasTodayReport = MutableLiveData<Boolean>()
    val hasTodayReport: LiveData<Boolean> = _hasTodayReport

    // 添加分析报告相关状态
    private val _hasAnalysisReport = MutableLiveData<Boolean>(false)
    val hasAnalysisReport: LiveData<Boolean> = _hasAnalysisReport

    private val _analysisStatus = MutableLiveData<AnalysisStatus>()
    val analysisStatus: LiveData<AnalysisStatus> = _analysisStatus
    
    // 设置报告提交状态
    fun setReportSubmitted(submitted: Boolean) {
        _isReportSubmitted.value = submitted
    }
    
    // 加载健康日报数据
    fun loadDailyHealthReport(context: Context, reportId: Int) {
        _loadStatus.value = LoadStatus.Loading
        
        if (reportId > 0) {
            // 先尝试从本地数据库获取数据
            val localReport = DailyHealthReportRepo.getLocalDailyHealthReportById(reportId)
            
            // 如果本地有数据，先显示本地数据
            if (localReport != null) {
                _dailyHealthReport.postValue(localReport)
                _loadStatus.postValue(LoadStatus.Success)
                // 如果本地数据存在，说明之前已经提交过
                _isReportSubmitted.postValue(true)
            }
            
            // 然后再从网络获取最新数据
            DailyHealthReportRepo.getDailyHealthReportById(reportId, context, 
                onSuccess = { report ->
                    _dailyHealthReport.postValue(report)
                    _loadStatus.postValue(LoadStatus.Success)
                    // 设置报告已提交状态
                    _isReportSubmitted.postValue(true)
                },
                onError = { message ->
                    // 如果网络请求失败但本地有数据，保持使用本地数据，只显示错误消息
                    if (localReport != null) {
                        _loadStatus.postValue(LoadStatus.Error("网络请求失败，显示本地缓存数据: $message"))
                    } else {
                        // 如果本地也没有数据，则创建一个空的健康日报
                        _loadStatus.postValue(LoadStatus.Error(message))
                        createEmptyDailyHealthReport(context)
                    }
                }
            )
        } else {
            // 创建一个新的空健康日报
            createEmptyDailyHealthReport(context)
            _loadStatus.postValue(LoadStatus.Success)
            // 新创建的报告未提交
            _isReportSubmitted.postValue(false)
        }
    }
    
    // 保存健康日报
    fun saveDailyHealthReport(context: Context, report: DailyHealthReport) {
        _saveStatus.value = SaveStatus.Loading
        
        DailyHealthReportRepo.addOrUpdateDailyHealthReport(report, context,
            onSuccess = { savedReport ->
                _dailyHealthReport.postValue(savedReport)
                _saveStatus.postValue(SaveStatus.Success)
                // 设置报告已提交状态
                _isReportSubmitted.postValue(true)
            },
            onError = { message ->
                _saveStatus.postValue(SaveStatus.Error(message))
            }
        )
    }
    
    // 创建空的健康日报
    private fun createEmptyDailyHealthReport(context: Context) {
        val userId = AccountUtil(context).getUserId()
        val emptyReport = DailyHealthReport(
            userId = userId.toInt(),
            reportDate = Date(), // 始终使用当前日期
            createdAt = Timestamp(System.currentTimeMillis()),
            updatedAt = Timestamp(System.currentTimeMillis())
        )
        _dailyHealthReport.postValue(emptyReport)
    }

    // 添加新的方法来获取当天报告
    fun fetchTodayReport(userId: Int, todayDateStr: String, context: Context) {
        _loadStatus.value = LoadStatus.Loading
        
        DailyHealthReportRepo.getReportByUserIdAndDate(
            userId,
            todayDateStr,
            context,
            onSuccess = { report ->
                _dailyHealthReport.postValue(report)
                _loadStatus.postValue(LoadStatus.Success)
                // 设置报告已提交状态
                _isReportSubmitted.postValue(true)
            },
            onError = { message ->
                _loadStatus.postValue(LoadStatus.Error(message))
                // 设置报告未提交状态
                _isReportSubmitted.postValue(false)
            }
        )
    }

    // 检查当天是否已有报告
    fun checkTodayReport(userId: Int, context: Context) {
        DailyHealthReportRepo.checkTodayReport(
            userId,
            context,
            onSuccess = { exists ->
                _hasTodayReport.postValue(exists)
                // 如果已存在报告，设置报告已提交状态
                if (exists) {
                    _isReportSubmitted.postValue(true)
                }
            },
            onError = { message ->
                Log.e("DailyHealthReportVM", "Error checking today report: $message")
                // 默认为false，表示没有报告
                _hasTodayReport.postValue(false)
            }
        )
    }

    // 检查是否已有分析报告
    fun checkReportAnalysisExists(reportId: Int, context: Context) {
        // 首先检查报告本身的isAnalysed字段
        val report = dailyHealthReport.value
        if (report != null && report.isAnalysed) {
            _hasAnalysisReport.postValue(true)
            return
        }

        // 如果报告字段显示未分析，再检查本地数据库中是否有分析结果
        val localAnalysis = ReportAnalysisRepo.getLocalReportAnalysisByReportId(reportId.toLong())
        _hasAnalysisReport.postValue(localAnalysis != null)
    }

    // 分析健康报告
    fun analyzeHealthReport(report: DailyHealthReport, context: Context) {
        _analysisStatus.value = AnalysisStatus.Loading

        ReportAnalysisRepo.analyzeDailyHealthReport(
            report,
            context,
            onStart = {
                _analysisStatus.postValue(AnalysisStatus.Loading)
            },
            onSuccess = { analysis ->
                _analysisStatus.postValue(AnalysisStatus.Success(analysis))
                _hasAnalysisReport.postValue(true)

                // 从后端获取最新的报告（包含更新后的isAnalysed字段）
                DailyHealthReportRepo.getDailyHealthReportById(
                    report.reportId,
                    context,
                    onSuccess = { updatedReport ->
                        // 更新ViewModel中的报告数据
                        _dailyHealthReport.postValue(updatedReport)
                    },
                    onError = { message ->
                        Log.e("DailyHealthReportVM", "Failed to get updated report: $message")
                    }
                )
            },
            onError = { message ->
                _analysisStatus.postValue(AnalysisStatus.Error(message))
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

    // 分析状态
    sealed class AnalysisStatus {
        object Loading : AnalysisStatus()
        data class Success(val analysis: ReportAnalysis) : AnalysisStatus()
        data class Error(val message: String) : AnalysisStatus()
    }
}
