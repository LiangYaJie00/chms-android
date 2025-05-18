package com.example.chms_android.features.analysis.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.R
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.databinding.ActivityReportAnalysisBinding
import com.example.chms_android.features.analysis.viewmodel.ReportAnalysisViewModel
import com.example.chms_android.repo.ReportAnalysisRepo
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.TextFormatUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportAnalysisBinding
    private lateinit var viewModel: ReportAnalysisViewModel
    private lateinit var loadingDialog: LoadingDialog
    
    // 报告ID，如果通过Intent传递
    private var reportId: Long = 0
    // 分析报告对象，如果通过Intent传递
    private var reportAnalysis: ReportAnalysis? = null
    
    companion object {
        private const val TAG = "ReportAnalysisActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(ReportAnalysisViewModel::class.java)
        
        // 初始化加载对话框
        initLoadingDialog()
        
        // 获取Intent传递的数据
        getIntentData()
        
        // 设置观察者
        setupObservers()
        
        // 加载数据
        loadData()
    }
    
    private fun initLoadingDialog() {
        loadingDialog = LoadingDialog(this)
        loadingDialog.setMessage("正在加载分析报告...")
    }
    
    private fun getIntentData() {
        // 尝试获取reportId
        reportId = intent.getLongExtra("reportId", 0)
        
        // 尝试获取ReportAnalysis对象
        if (intent.hasExtra("reportAnalysis")) {
            reportAnalysis = intent.getParcelableExtra("reportAnalysis")
        }
        
        Log.d(TAG, "Intent data: reportId=$reportId, reportAnalysis=${reportAnalysis != null}")
    }
    
    private fun setupObservers() {
        // 观察分析报告数据
        viewModel.reportAnalysis.observe(this) { analysis ->
            if (analysis != null) {
                // 更新UI显示分析报告
                updateUI(analysis)
            }
        }
        
        // 观察加载状态
        viewModel.loadingStatus.observe(this) { status ->
            when (status) {
                is ReportAnalysisViewModel.LoadingStatus.Loading -> {
                    if (!loadingDialog.isShowing) {
                        loadingDialog.show()
                    }
                }
                is ReportAnalysisViewModel.LoadingStatus.Success -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                }
                is ReportAnalysisViewModel.LoadingStatus.Error -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    // 显示错误信息
                    showError(status.message)
                }
            }
        }
    }
    
    private fun loadData() {
        when {
            // 如果传递了ReportAnalysis对象，优先使用它
            reportAnalysis != null -> {
                // 先显示传递的ReportAnalysis
                viewModel.setReportAnalysis(reportAnalysis!!)
                
                // 然后通过reportId获取最新的ReportAnalysis
                val analysisReportId = reportAnalysis!!.reportId
                if (analysisReportId > 0) {
                    viewModel.loadReportAnalysis(analysisReportId, this)
                }
            }
            
            // 如果传递了reportId，通过它获取ReportAnalysis
            reportId > 0 -> {
                // 先尝试从本地数据库获取
                val localAnalysis = ReportAnalysisRepo.getLocalReportAnalysisByReportId(reportId)
                if (localAnalysis != null) {
                    // 如果本地有数据，先显示本地数据
                    viewModel.setReportAnalysis(localAnalysis)
                }
                
                // 然后从网络获取最新数据
                viewModel.loadReportAnalysis(reportId, this)
            }
            
            // 如果既没有传递ReportAnalysis也没有传递reportId，显示错误
            else -> {
                showError("未找到分析报告数据")
            }
        }
    }
    
    private fun updateUI(analysis: ReportAnalysis) {
        // 更新UI显示分析报告内容，处理加粗和颜色
        val answer = analysis.answer ?: "暂无分析内容"
        binding.tvReportContent.text = TextFormatUtil.formatBoldText(this, answer, R.color.primary)
        
        // 更新报告标题
        updateReportTitle(analysis)
        
        // 可以根据需要添加更多UI更新逻辑
    }
    
    private fun updateReportTitle(analysis: ReportAnalysis) {
        // 尝试从分析报告中获取日期
        val dateStr = when {
            // 优先使用专门的报告日期字段
            analysis.reportDate != null -> {
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                dateFormat.format(analysis.reportDate)
            }
            // 其次使用创建时间
            analysis.createdAt != null -> {
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                dateFormat.format(analysis.createdAt)
            }
            // 最后使用当前日期
            else -> {
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                dateFormat.format(Date())
            }
        }
        
        // 设置标题文本
        binding.tvReportTitle.text = "健康报告（$dateStr）"
    }
    
    private fun showError(message: String) {
        // 显示错误信息，可以使用Toast或其他方式
        binding.tvReportContent.text = "加载失败: $message"
    }
}
