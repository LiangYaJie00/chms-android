package com.example.chms_android.features.health.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.databinding.FragmentDailyHealthReportViewBinding
import com.example.chms_android.features.health.viewmodel.DailyHealthReportViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class DailyHealthReportViewFragment : Fragment() {
    
    private var _binding: FragmentDailyHealthReportViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DailyHealthReportViewModel
    private var reportId: Int = 0
    
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
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}