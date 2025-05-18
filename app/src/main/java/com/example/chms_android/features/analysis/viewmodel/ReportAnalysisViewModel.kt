package com.example.chms_android.features.analysis.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.repo.ReportAnalysisRepo

class ReportAnalysisViewModel : ViewModel() {
    
    private val _reportAnalysis = MutableLiveData<ReportAnalysis>()
    val reportAnalysis: LiveData<ReportAnalysis> = _reportAnalysis
    
    private val _loadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus> = _loadingStatus
    
    companion object {
        private const val TAG = "ReportAnalysisViewModel"
    }
    
    /**
     * 设置分析报告数据
     */
    fun setReportAnalysis(analysis: ReportAnalysis) {
        _reportAnalysis.value = analysis
    }
    
    /**
     * 加载分析报告
     * 
     * @param reportId 报告ID
     * @param context 上下文
     */
    fun loadReportAnalysis(reportId: Long, context: Context) {
        _loadingStatus.value = LoadingStatus.Loading
        
        ReportAnalysisRepo.getReportAnalysisByReportId(
            reportId,
            context,
            onSuccess = { analysis ->
                _reportAnalysis.postValue(analysis)
                _loadingStatus.postValue(LoadingStatus.Success)
                Log.d(TAG, "Successfully loaded report analysis: $analysis")
            },
            onError = { message ->
                _loadingStatus.postValue(LoadingStatus.Error(message))
                Log.e(TAG, "Failed to load report analysis: $message")
            }
        )
    }
    
    /**
     * 加载状态
     */
    sealed class LoadingStatus {
        object Loading : LoadingStatus()
        object Success : LoadingStatus()
        data class Error(val message: String) : LoadingStatus()
    }
}