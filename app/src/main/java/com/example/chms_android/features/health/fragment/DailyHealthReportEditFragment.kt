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
import android.graphics.Rect
import android.view.ViewTreeObserver
import android.widget.TextView
import java.util.Random

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
        
        // 获取ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(DailyHealthReportViewModel::class.java)
        
        // 初始化加载对话框
        loadingDialog = LoadingDialog(requireContext())
        
        // 设置视图
        setupViews()
        
        // 设置观察者
        setupObservers()
        
        // 设置监听器
        setupListeners()
        
        // 设置生命体征标题的长按事件（仅在上报模式下有效）
        setupDebugFeatures()
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

    /**
     * 设置调试功能
     */
    private fun setupDebugFeatures() {
        try {
            // 获取生命体征标题
            val vitalSignsTitle = binding.root.findViewById<TextView>(R.id.tv_vital_signs_title)
            
            // 获取当前模式
            val entryMode = arguments?.getInt("entryMode", 0) ?: 0
            
            // 只在上报模式下启用此功能（entryMode = 0 表示从上报入口进入）
            if (entryMode == 0) {
                vitalSignsTitle?.setOnLongClickListener {
                    generateTestData()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("DailyHealthReportEdit", "Error setting up debug features", e)
        }
    }

    /**
     * 生成测试数据
     */
    private fun generateTestData() {
        try {
            // 使用随机数生成器
            val random = Random()
            
            // 生成随机心率 (60-100)
            val heartRate = 60 + random.nextInt(41)
            binding.edtHeartRate.setText(heartRate.toString())
            
            // 生成随机收缩压 (90-140)
            val systolic = 90 + random.nextInt(51)
            binding.edtBloodPressureSystolic.setText(systolic.toString())
            
            // 生成随机舒张压 (60-90)
            val diastolic = 60 + random.nextInt(31)
            binding.edtBloodPressureDiastolic.setText(diastolic.toString())
            
            // 生成随机体温 (36.0-37.5)
            val temperature = 36.0f + (random.nextFloat() * 1.5f)
            val temperatureFormatted = String.format("%.1f", temperature)
            binding.edtBodyTemperature.setText(temperatureFormatted)
            
            // 生成随机呼吸频率 (12-20)
            val respiratoryRate = 12 + random.nextInt(9)
            binding.edtRespiratoryRate.setText(respiratoryRate.toString())
            
            // 生成随机动脉血氧 (95-100)
            val arterialOxygen = 95 + random.nextInt(6)
            binding.edtArterialBloodOxygenLevel.setText(arterialOxygen.toString())
            
            // 生成随机静脉血氧 (70-80)
            val venousOxygen = 70 + random.nextInt(11)
            binding.edtVenousBloodOxygenLevel.setText(venousOxygen.toString())
            
            // 生成随机体重 (50-90)
            val weight = 50 + random.nextInt(41)
            binding.edtWeight.setText(weight.toString())
            
            // 生成随机血糖 (3.9-6.1)
            val bloodSugar = 3.9f + (random.nextFloat() * 2.2f)
            val bloodSugarFormatted = String.format("%.1f", bloodSugar)
            binding.edtBloodSugar.setText(bloodSugarFormatted)
            
            // 生成随机步数 (3000-10000)
            val steps = 3000 + random.nextInt(7001)
            binding.edtSteps.setText(steps.toString())
            
            // 生成随机运动时长 (15-120分钟)
            val exerciseDuration = 15 + random.nextInt(106)
            binding.edtExerciseDuration.setText(exerciseDuration.toString())
            
            // 生成随机睡眠时长 (5-9小时，0-59分钟)
            val sleepHours = 5 + random.nextInt(5)
            val sleepMinutes = random.nextInt(60)
            binding.edtSleepDurationHours.setText(sleepHours.toString())
            binding.edtSleepDurationMinutes.setText(sleepMinutes.toString())
            
            // 随机选择睡眠质量
            val sleepQualityOptions = arrayOf("差", "一般", "良好")
            val sleepQualityIndex = random.nextInt(3)
            val sleepQualityView = binding.edtSleepQuality
            
            when (sleepQualityView) {
                is Spinner -> {
                    sleepQualityView.setSelection(sleepQualityIndex)
                }
                is AutoCompleteTextView -> {
                    sleepQualityView.setText(sleepQualityOptions[sleepQualityIndex])
                }
                is EditText -> {
                    sleepQualityView.setText(sleepQualityOptions[sleepQualityIndex])
                }
            }
            
            // 生成随机卡路里摄入 (1500-3000)
            val calories = 1500 + random.nextInt(1501)
            binding.edtCaloriesIntake.setText(calories.toString())
            
            // 生成随机饮水量 (1000-3000ml)
            val waterIntake = 1000 + random.nextInt(2001)
            binding.edtWaterIntake.setText(waterIntake.toString())
            
            // 随机情绪状态
            val emotionalStates = arrayOf("平静", "愉快", "焦虑", "疲惫", "兴奋")
            val emotionalState = emotionalStates[random.nextInt(emotionalStates.size)]
            binding.edtEmotionalState.setText(emotionalState)
            
            // 随机压力水平
            val stressLevelOptions = arrayOf("低", "中", "高")
            val stressLevelIndex = random.nextInt(3)
            val stressLevelView = binding.edtStressLevel
            
            when (stressLevelView) {
                is Spinner -> {
                    stressLevelView.setSelection(stressLevelIndex)
                }
                is AutoCompleteTextView -> {
                    stressLevelView.setText(stressLevelOptions[stressLevelIndex])
                }
                is EditText -> {
                    stressLevelView.setText(stressLevelOptions[stressLevelIndex])
                }
            }
            
            // 随机备注
            val noteOptions = arrayOf(
                "今天感觉不错，精力充沛。",
                "有点疲惫，可能是昨晚睡眠不足。",
                "今天运动后感觉很舒适。",
                "工作压力有点大，需要放松。",
                "饮食规律，心情愉快。",
                "天气变化，有点不适应。",
                ""  // 空备注的可能性
            )
            val note = noteOptions[random.nextInt(noteOptions.size)]
            binding.edtNotes.setText(note)
            
            // 显示提示
            ToastUtil.show(requireContext(), "已生成测试数据")
        } catch (e: Exception) {
            Log.e("DailyHealthReportEdit", "Error generating test data", e)
            ToastUtil.show(requireContext(), "生成测试数据失败: ${e.message}")
        }
    }
}
