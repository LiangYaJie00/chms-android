package com.example.chms_android.features.analysis.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.repo.ReportAnalysisRepo

class ReportAnalysisShowViewModel : ViewModel() {
    private val _reportAnalysisList = MutableLiveData<List<ReportAnalysis>>()
    val reportAnalysisList: LiveData<List<ReportAnalysis>> = _reportAnalysisList
    
    private val _loadStatus = MutableLiveData<LoadStatus>()
    val loadStatus: LiveData<LoadStatus> = _loadStatus
    
    companion object {
        private const val TAG = "ReportAnalysisShowVM"
    }
    
    /**
     * 加载用户的所有分析报告
     * 
     * @param userId 用户ID
     * @param context 上下文
     */
    fun loadUserReportAnalysis(userId: Int, context: Context) {
        _loadStatus.value = LoadStatus.Loading
        
        ReportAnalysisRepo.getUserReportAnalysis(
            userId,
            context,
            onSuccess = { analysisList ->
                _reportAnalysisList.postValue(analysisList)
                _loadStatus.postValue(LoadStatus.Success)
                Log.d(TAG, "Successfully loaded ${analysisList.size} report analysis")
            },
            onError = { message ->
                _loadStatus.postValue(LoadStatus.Error(message))
                Log.e(TAG, "Failed to load report analysis: $message")
            }
        )
    }
    
    /**
     * 加载状态
     */
    sealed class LoadStatus {
        object Loading : LoadStatus()
        object Success : LoadStatus()
        data class Error(val message: String) : LoadStatus()
    }
}