package com.example.chms_android.features.health.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.data.ReportAnalysis
import com.example.chms_android.databinding.FragmentDailyHealthReportViewBinding
import com.example.chms_android.features.health.viewmodel.DailyHealthReportViewModel
import com.example.chms_android.features.analysis.activity.ReportAnalysisActivity
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.Locale

class DailyHealthReportViewFragment : Fragment() {
    
    private var _binding: FragmentDailyHealthReportViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DailyHealthReportViewModel
    private var reportId: Int = 0
    // 添加加载弹窗
    private lateinit var loadingDialog: LoadingDialog

    companion object {
        fun newInstance(reportId: Int): DailyHealthReportViewFragment {
            val fragment = DailyHealthReportViewFragment()
            val args = Bundle()
            args.putInt("reportId", reportId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reportId = it.getInt("reportId", 0)
        }
        // 初始化加载弹窗
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.setMessage("正在进行AI分析，请耐心等待...")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyHealthReportViewBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity()).get(DailyHealthReportViewModel::class.java)

        // 设置观察者，当数据变化时更新UI
        setupObservers()
        
        // 设置AI分析按钮点击事件
        setupAiAnalysisButton()
    }

    private fun setupAiAnalysisButton() {
        // 检查是否已有分析报告
        checkExistingAnalysis()
        
        binding.btnAiAnalysis.setOnClickListener {
            val currentReportId = viewModel.dailyHealthReport.value?.reportId ?: 0
            if (currentReportId > 0) {
                if (viewModel.hasAnalysisReport.value == true) {
                    // 如果已有分析报告，直接跳转到分析报告页面
                    navigateToReportSummary(currentReportId)
                } else {
                    // 请求AI分析
                    requestAiAnalysis(currentReportId)
                }
            } else {
                ToastUtil.show(requireContext(), "无法分析，报告ID无效")
            }
        }
    }

    private fun checkExistingAnalysis() {
        val reportId = viewModel.dailyHealthReport.value?.reportId ?: 0
        if (reportId > 0) {
            // 检查是否已有分析报告
            viewModel.checkReportAnalysisExists(reportId, requireContext())
        }
    }

    private fun requestAiAnalysis(reportId: Int) {
        val report = viewModel.dailyHealthReport.value
        if (report != null) {
            // 显示加载弹窗
            loadingDialog.show()
            
            viewModel.analyzeHealthReport(report, requireContext())
        }
    }

    private fun navigateToReportSummary(reportId: Int) {
        val intent = Intent(requireContext(), ReportAnalysisActivity::class.java).apply {
            putExtra("reportId", reportId.toLong())
        }
        startActivity(intent)
    }

    // 如果有分析报告对象，可以直接传递
    private fun navigateToReportSummaryWithAnalysis(analysis: ReportAnalysis) {
        val intent = Intent(requireContext(), ReportAnalysisActivity::class.java).apply {
            putExtra("reportAnalysis", analysis)
        }
        startActivity(intent)
    }
    
    private fun setupObservers() {
        viewModel.dailyHealthReport.observe(viewLifecycleOwner) { report ->
            report?.let {
                // 更新UI显示健康日报数据
                binding.tvReportDate.text = it.reportDate?.let { date ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                } ?: "未记录"
                
                binding.tvHeartRate.text = it.heartRate?.toString() ?: "未记录"
                binding.tvBloodPressure.text = if (it.bloodPressureSystolic != null && it.bloodPressureDiastolic != null) {
                    "${it.bloodPressureSystolic}/${it.bloodPressureDiastolic}"
                } else {
                    "未记录"
                }
                binding.tvBodyTemperature.text = it.bodyTemperature?.toString() ?: "未记录"
                binding.tvRespiratoryRate.text = it.respiratoryRate?.toString() ?: "未记录"
                binding.tvWeight.text = it.weight?.toString() ?: "未记录"
                binding.tvBloodSugar.text = it.bloodSugar?.toString() ?: "未记录"
                binding.tvSteps.text = it.steps?.toString() ?: "未记录"
                binding.tvExerciseDuration.text = it.exerciseDuration?.toString() ?: "未记录"
                binding.tvSleepDuration.text = it.sleepDuration?.let { minutes ->
                    val hours = minutes / 60
                    val mins = minutes % 60
                    if (mins > 0) {
                        "$hours 小时 $mins 分钟"
                    } else {
                        "$hours 小时"
                    }
                } ?: "未记录"
                binding.tvSleepQuality.text = when(it.sleepQuality) {
                    "差" -> "差"
                    "一般" -> "一般"
                    "良好" -> "良好"
                    else -> "未记录"
                }
                binding.tvCaloriesIntake.text = it.caloriesIntake?.toString() ?: "未记录"
                binding.tvWaterIntake.text = it.waterIntake?.toString() ?: "未记录"
                binding.tvEmotionalState.text = it.emotionalState ?: "未记录"
                binding.tvStressLevel.text = it.stressLevel ?: "未记录"
                binding.tvArterialBloodOxygenLevel.text = it.arterialBloodOxygenLevel?.toString() ?: "未记录"
                binding.tvVenousBloodOxygenLevel.text = it.venousBloodOxygenLevel?.toString() ?: "未记录"
                binding.tvNotes.text = it.notes ?: "未记录"
                setupAiAnalysisButton()
            }
        }
        
        viewModel.loadStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is DailyHealthReportViewModel.LoadStatus.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.GONE
                }
                is DailyHealthReportViewModel.LoadStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                }
                is DailyHealthReportViewModel.LoadStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                    // 可以显示错误信息
                }
            }
        }

        // 添加分析状态观察
        viewModel.hasAnalysisReport.observe(viewLifecycleOwner) { hasReport ->
            updateAnalysisButtonText(hasReport)
        }

        viewModel.analysisStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is DailyHealthReportViewModel.AnalysisStatus.Loading -> {
                    // 显示加载中
                    binding.btnAiAnalysis.isEnabled = false
                    binding.btnAiAnalysis.text = "分析中..."
                    // 确保加载弹窗显示
                    if (!loadingDialog.isShowing) {
                        loadingDialog.show()
                    }
                }
                is DailyHealthReportViewModel.AnalysisStatus.Success -> {
                    // 隐藏加载弹窗
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    
                    // 分析完成，更新按钮状态
                    binding.btnAiAnalysis.isEnabled = true
                    updateAnalysisButtonText(true)
                    
                    // 显示确认对话框
                    showAnalysisCompleteDialog(status.analysis.reportId.toInt())
                }
                is DailyHealthReportViewModel.AnalysisStatus.Error -> {
                    // 隐藏加载弹窗
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    
                    // 显示错误
                    binding.btnAiAnalysis.isEnabled = true
                    updateAnalysisButtonText(false)
                    ToastUtil.show(requireContext(), "分析失败: ${status.message}")
                }
                else -> {}
            }
        }
    }

    private fun updateAnalysisButtonText(hasReport: Boolean) {
        binding.btnAiAnalysis.text = if (hasReport) "查看分析报告" else "AI健康分析"
    }

    private fun showAnalysisCompleteDialog(reportId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("分析完成")
            .setMessage("AI健康分析已完成，是否查看详细报告？")
            .setPositiveButton("查看") { _, _ ->
                navigateToReportSummary(reportId)
            }
            .setNegativeButton("稍后查看", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // 确保弹窗关闭，避免内存泄漏
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        _binding = null
    }
}