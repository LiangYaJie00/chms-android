package com.example.chms_android.features.health.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.R
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.databinding.FragmentDailyHealthReportEditBinding
import com.example.chms_android.features.health.viewmodel.DailyHealthReportViewModel
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.ToastUtil
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.util.Log
import android.widget.EditText

class DailyHealthReportEditFragment : Fragment() {
    
    private var _binding: FragmentDailyHealthReportEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DailyHealthReportViewModel
    private var reportId: Int = 0
    private lateinit var loadingDialog: LoadingDialog
    
    companion object {
        fun newInstance(reportId: Int): DailyHealthReportEditFragment {
            val fragment = DailyHealthReportEditFragment()
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
        try {
            _binding = FragmentDailyHealthReportEditBinding.inflate(inflater, container, false)
            return binding.root
        }catch (e: Exception) {
            // 记录详细错误信息
            val errorMsg = "布局绑定失败: ${e.message}"
            android.util.Log.e("DailyHealthReportEdit", errorMsg, e)

            // 创建一个简单的替代视图
            val textView = android.widget.TextView(requireContext())
            textView.text = "加载界面失败，请重试"
            textView.gravity = android.view.Gravity.CENTER
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            return textView
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity()).get(DailyHealthReportViewModel::class.java)
        loadingDialog = LoadingDialog(requireContext())
        
        // 设置观察者和监听器
        setupViews()
        setupObservers()
        setupListeners()
    }
    
    private fun setupViews() {
        try {
            // 设置压力水平下拉选择器
            val stressLevelOptions = arrayOf("低", "中", "高")
            
            // 获取控件引用
            val stressLevelView = binding.edtStressLevel
            val sleepQualityView = binding.edtSleepQuality
            
            // 设置压力水平适配器
            when (stressLevelView) {
                is Spinner -> {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        stressLevelOptions
                    )
                    stressLevelView.adapter = adapter
                }
                is AutoCompleteTextView -> {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        stressLevelOptions
                    )
                    stressLevelView.setAdapter(adapter)
                }
            }
            
            // 设置睡眠质量下拉选择器
            val sleepQualityOptions = arrayOf("差", "一般", "良好")
            
            // 设置睡眠质量适配器
            when (sleepQualityView) {
                is Spinner -> {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        sleepQualityOptions
                    )
                    sleepQualityView.adapter = adapter
                }
                is AutoCompleteTextView -> {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        sleepQualityOptions
                    )
                    sleepQualityView.setAdapter(adapter)
                }
            }
            
            // 设置当前日期并禁用日期选择
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            binding.edtReportDate.setText(currentDate)
            binding.edtReportDate.isEnabled = false
        } catch (e: Exception) {
            Log.e("DailyHealthReportEdit", "Error in setupViews", e)
        }
    }
    
    private fun setupObservers() {
        try {
            viewModel.dailyHealthReport.observe(viewLifecycleOwner) { report ->
                try {
                    report?.let {
                        // 不再从报告中获取日期，而是始终使用当天日期
                        // 其他字段保持不变
                        binding.edtHeartRate.setText(it.heartRate?.toString() ?: "")
                        binding.edtBloodPressureSystolic.setText(it.bloodPressureSystolic?.toString() ?: "")
                        binding.edtBloodPressureDiastolic.setText(it.bloodPressureDiastolic?.toString() ?: "")
                        binding.edtBodyTemperature.setText(it.bodyTemperature?.toString() ?: "")
                        binding.edtRespiratoryRate.setText(it.respiratoryRate?.toString() ?: "")
                        binding.edtWeight.setText(it.weight?.toString() ?: "")
                        binding.edtBloodSugar.setText(it.bloodSugar?.toString() ?: "")
                        binding.edtSteps.setText(it.steps?.toString() ?: "")
                        binding.edtExerciseDuration.setText(it.exerciseDuration?.toString() ?: "")
                        it.sleepDuration?.let { minutes ->
                            val hours = minutes / 60
                            val mins = minutes % 60
                            binding.edtSleepDurationHours.setText(hours.toString())
                            binding.edtSleepDurationMinutes.setText(mins.toString())
                        } ?: run {
                            binding.edtSleepDurationHours.setText("")
                            binding.edtSleepDurationMinutes.setText("")
                        }
                        
                        // 获取控件引用
                        val sleepQualityView = binding.edtSleepQuality
                        val stressLevelView = binding.edtStressLevel
                        
                        // 根据控件类型设置睡眠质量
                        when (sleepQualityView) {
                            is Spinner -> {
                                val sleepQualityOptions = arrayOf("差", "一般", "良好")
                                val position = sleepQualityOptions.indexOf(it.sleepQuality)
                                if (position >= 0) {
                                    sleepQualityView.setSelection(position)
                                }
                            }
                            is AutoCompleteTextView -> {
                                sleepQualityView.setText(it.sleepQuality ?: "", false)
                            }
                            is EditText -> {
                                sleepQualityView.setText(it.sleepQuality ?: "")
                            }
                        }
                        
                        binding.edtCaloriesIntake.setText(it.caloriesIntake?.toString() ?: "")
                        binding.edtWaterIntake.setText(it.waterIntake?.toString() ?: "")
                        binding.edtEmotionalState.setText(it.emotionalState ?: "")
                        
                        // 根据控件类型设置压力水平
                        when (stressLevelView) {
                            is Spinner -> {
                                val stressLevelOptions = arrayOf("低", "中", "高")
                                val position = stressLevelOptions.indexOf(it.stressLevel)
                                if (position >= 0) {
                                    stressLevelView.setSelection(position)
                                }
                            }
                            is AutoCompleteTextView -> {
                                stressLevelView.setText(it.stressLevel ?: "", false)
                            }
                            is EditText -> {
                                stressLevelView.setText(it.stressLevel ?: "")
                            }
                        }
                        
                        binding.edtArterialBloodOxygenLevel.setText(it.arterialBloodOxygenLevel?.toString() ?: "")
                        binding.edtVenousBloodOxygenLevel.setText(it.venousBloodOxygenLevel?.toString() ?: "")
                        binding.edtNotes.setText(it.notes ?: "")
                    }
                } catch (e: Exception) {
                    Log.e("DailyHealthReportEdit", "Error updating UI with report data", e)
                }
            }
            
            viewModel.saveStatus.observe(viewLifecycleOwner) { status ->
                when (status) {
                    is DailyHealthReportViewModel.SaveStatus.Loading -> {
                        loadingDialog.show()
                    }
                    is DailyHealthReportViewModel.SaveStatus.Success -> {
                        if (loadingDialog.isShowing) {
                            loadingDialog.dismiss()
                        }
                        ToastUtil.show(requireContext(), "健康日报保存成功")
                    }
                    is DailyHealthReportViewModel.SaveStatus.Error -> {
                        if (loadingDialog.isShowing) {
                            loadingDialog.dismiss()
                        }
                        ToastUtil.show(requireContext(), "保存失败: ${status.message}")
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            Log.e("DailyHealthReportEdit", "Error in setupObservers", e)
        }
    }
    
    private fun setupListeners() {
        // 移除日期选择器的点击监听器，因为日期不可选
        
        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveDailyHealthReport()
        }
    }
    
    // 移除showDatePicker方法，因为不再需要日期选择器

    private fun saveDailyHealthReport() {
        try {
            // 获取当前健康日报数据
            val currentReport = viewModel.dailyHealthReport.value ?: DailyHealthReport()
            
            // 使用当前日期
            val reportDate = Date() // 当前日期
            
            // 解析睡眠时长，将小时和分钟分别获取并转换为总分钟数
            val hoursStr = binding.edtSleepDurationHours.text.toString()
            val minutesStr = binding.edtSleepDurationMinutes.text.toString()
            
            val sleepDurationMinutes = if (hoursStr.isNotEmpty() || minutesStr.isNotEmpty()) {
                val hours = hoursStr.toIntOrNull() ?: 0
                val minutes = minutesStr.toIntOrNull() ?: 0
                hours * 60 + minutes
            } else null
            
            // 获取控件引用
            val sleepQualityView = binding.edtSleepQuality
            val stressLevelView = binding.edtStressLevel
            
            // 根据控件类型获取睡眠质量
            val sleepQuality = when (sleepQualityView) {
                is Spinner -> {
                    if (sleepQualityView.selectedItemPosition >= 0) 
                        sleepQualityView.selectedItem.toString() 
                    else null
                }
                is AutoCompleteTextView -> {
                    sleepQualityView.text.toString().takeIf { it.isNotEmpty() }
                }
                is EditText -> {
                    sleepQualityView.text.toString().takeIf { it.isNotEmpty() }
                }
                else -> null
            }
            
            // 根据控件类型获取压力水平
            val stressLevel = when (stressLevelView) {
                is Spinner -> {
                    if (stressLevelView.selectedItemPosition >= 0) 
                        stressLevelView.selectedItem.toString() 
                    else null
                }
                is AutoCompleteTextView -> {
                    stressLevelView.text.toString().takeIf { it.isNotEmpty() }
                }
                is EditText -> {
                    stressLevelView.text.toString().takeIf { it.isNotEmpty() }
                }
                else -> null
            }
            
            // 构建更新后的健康日报对象
            val updatedReport = currentReport.copy(
                reportId = currentReport.reportId,
                userId = currentReport.userId,
                reportDate = reportDate,
                heartRate = binding.edtHeartRate.text.toString().toIntOrNull(),
                bloodPressureSystolic = binding.edtBloodPressureSystolic.text.toString().toIntOrNull(),
                bloodPressureDiastolic = binding.edtBloodPressureDiastolic.text.toString().toIntOrNull(),
                bodyTemperature = binding.edtBodyTemperature.text.toString().toBigDecimalOrNull(),
                respiratoryRate = binding.edtRespiratoryRate.text.toString().toIntOrNull(),
                weight = binding.edtWeight.text.toString().toBigDecimalOrNull(),
                bloodSugar = binding.edtBloodSugar.text.toString().toBigDecimalOrNull(),
                steps = binding.edtSteps.text.toString().toIntOrNull(),
                exerciseDuration = binding.edtExerciseDuration.text.toString().toIntOrNull(),
                sleepDuration = sleepDurationMinutes,
                sleepQuality = sleepQuality,
                caloriesIntake = binding.edtCaloriesIntake.text.toString().toIntOrNull(),
                waterIntake = binding.edtWaterIntake.text.toString().toIntOrNull(),
                emotionalState = binding.edtEmotionalState.text.toString().takeIf { it.isNotEmpty() },
                stressLevel = stressLevel,
                arterialBloodOxygenLevel = binding.edtArterialBloodOxygenLevel.text.toString().toBigDecimalOrNull(),
                venousBloodOxygenLevel = binding.edtVenousBloodOxygenLevel.text.toString().toBigDecimalOrNull(),
                notes = binding.edtNotes.text.toString().takeIf { it.isNotEmpty() },
                createdAt = currentReport.createdAt ?: Timestamp(System.currentTimeMillis()),
                updatedAt = Timestamp(System.currentTimeMillis())
            )
            
            // 保存更新后的健康日报
            viewModel.saveDailyHealthReport(requireContext(), updatedReport)
            
        } catch (e: Exception) {
            Log.e("DailyHealthReportEdit", "Error in saveDailyHealthReport", e)
            ToastUtil.show(requireContext(), "保存失败: ${e.message}")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
