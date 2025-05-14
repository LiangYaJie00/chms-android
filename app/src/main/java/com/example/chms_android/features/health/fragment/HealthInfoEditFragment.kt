package com.example.chms_android.features.health.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.databinding.FragmentHealthInfoEditBinding
import com.example.chms_android.features.health.viewmodel.HealthInfoViewModel
import com.example.chms_android.ui.components.dialog.LoadingDialog
import com.example.chms_android.utils.DateUtil
import com.example.chms_android.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.math.BigDecimal

class HealthInfoEditFragment : Fragment() {
    
    private var _binding: FragmentHealthInfoEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HealthInfoViewModel
    private lateinit var loadingDialog: LoadingDialog
    private val calendar = Calendar.getInstance()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthInfoEditBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity()).get(HealthInfoViewModel::class.java)
        loadingDialog = LoadingDialog(requireContext())
        
        // 设置观察者
        setupObservers()
        
        // 设置监听器
        setupListeners()
        
        // 加载健康信息数据
        viewModel.loadHealthInfo(requireContext())
    }
    
    private fun setupObservers() {
        viewModel.healthInfo.observe(viewLifecycleOwner) { healthInfo ->
            if (healthInfo != null) {
                // 填充表单
                binding.edtHeight.setText(healthInfo.height?.toString() ?: "")
                binding.edtWeight.setText(healthInfo.weight?.toString() ?: "")
                binding.edtSystolicPressure.setText(healthInfo.bloodPressureSystolic?.toString() ?: "")
                binding.edtDiastolicPressure.setText(healthInfo.bloodPressureDiastolic?.toString() ?: "")
                binding.edtHeartRate.setText(healthInfo.heartRate?.toString() ?: "")
                binding.edtArterialOxygen.setText(healthInfo.arterialBloodOxygenLevel?.toString() ?: "")
                binding.edtVenousOxygen.setText(healthInfo.venousBloodOxygenLevel?.toString() ?: "")
                
                // 处理日期
                healthInfo.lastMedicalCheckup?.let { date ->
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.edtCheckupDate.setText(format.format(date))
                    // 更新日历对象以便日期选择器显示正确的日期
                    calendar.time = date
                } ?: run {
                    binding.edtCheckupDate.setText("")
                }
                
                binding.edtAllergies.setText(healthInfo.allergies ?: "")
                binding.edtMedicalHistory.setText(healthInfo.medicalHistory ?: "")
                binding.edtFamilyHistory.setText(healthInfo.familyMedicalHistory ?: "")
                binding.edtCurrentConditions.setText(healthInfo.currentConditions ?: "")
                binding.edtMedications.setText(healthInfo.medications ?: "")
                binding.edtVaccinationRecords.setText(healthInfo.vaccinationRecords ?: "")
                binding.edtLifestyleHabits.setText(healthInfo.lifestyleHabits ?: "")
                binding.edtExerciseFrequency.setText(healthInfo.exerciseFrequency ?: "")
                binding.edtDietaryPreferences.setText(healthInfo.dietaryPreferences ?: "")
            }
        }
        
        viewModel.saveStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is HealthInfoViewModel.SaveStatus.Loading -> {
                    loadingDialog.show()
                }
                is HealthInfoViewModel.SaveStatus.Success -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    ToastUtil.show(requireContext(), "保存成功")
                }
                is HealthInfoViewModel.SaveStatus.Error -> {
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    ToastUtil.show(requireContext(), "保存失败: ${status.message}")
                }
                else -> {}
            }
        }
    }
    
    private fun setupListeners() {
        // 日期选择器
        binding.edtCheckupDate.setOnClickListener {
            showDatePicker()
        }
        
        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveHealthInfo()
        }
    }
    
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun updateDateInView() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.edtCheckupDate.setText(format.format(calendar.time))
    }
    
    private fun saveHealthInfo() {
        // 获取表单数据
        val height = binding.edtHeight.text.toString().toBigDecimalOrNull()
        val weight = binding.edtWeight.text.toString().toBigDecimalOrNull()
        val systolicPressure = binding.edtSystolicPressure.text.toString().toIntOrNull()
        val diastolicPressure = binding.edtDiastolicPressure.text.toString().toIntOrNull()
        val heartRate = binding.edtHeartRate.text.toString().toIntOrNull()
        val arterialOxygen = binding.edtArterialOxygen.text.toString().toBigDecimalOrNull()
        val venousOxygen = binding.edtVenousOxygen.text.toString().toBigDecimalOrNull()
        
        // 计算BMI
        val bmi = if (height != null && weight != null && height != BigDecimal.ZERO) {
            val heightInMeters = height.divide(BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)
            weight.divide(heightInMeters.multiply(heightInMeters), 2, BigDecimal.ROUND_HALF_UP)
        } else {
            null
        }
        
        val checkupDateStr = binding.edtCheckupDate.text.toString()
        val checkupDate = if (checkupDateStr.isNotEmpty()) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(checkupDateStr)
        } else {
            null
        }
        
        val allergies = binding.edtAllergies.text.toString()
        val medicalHistory = binding.edtMedicalHistory.text.toString()
        val familyHistory = binding.edtFamilyHistory.text.toString()
        val currentConditions = binding.edtCurrentConditions.text.toString()
        val medications = binding.edtMedications.text.toString()
        val vaccinationRecords = binding.edtVaccinationRecords.text.toString()
        val lifestyleHabits = binding.edtLifestyleHabits.text.toString()
        val exerciseFrequency = binding.edtExerciseFrequency.text.toString()
        val dietaryPreferences = binding.edtDietaryPreferences.text.toString()
        
        // 创建健康信息对象
        val healthInfo = HealthInfo(
            // 使用现有的healthId，如果有的话
            healthId = viewModel.healthInfo.value?.healthId ?: 0,
            userId = viewModel.healthInfo.value?.userId ?: 0,
            weight = weight,
            height = height,
            bmi = bmi,
            bloodPressureSystolic = systolicPressure,
            bloodPressureDiastolic = diastolicPressure,
            heartRate = heartRate,
            arterialBloodOxygenLevel = arterialOxygen,
            venousBloodOxygenLevel = venousOxygen,
            lastMedicalCheckup = checkupDate,
            allergies = allergies.ifEmpty { null },
            medicalHistory = medicalHistory.ifEmpty { null },
            familyMedicalHistory = familyHistory.ifEmpty { null },
            currentConditions = currentConditions.ifEmpty { null },
            medications = medications.ifEmpty { null },
            vaccinationRecords = vaccinationRecords.ifEmpty { null },
            lifestyleHabits = lifestyleHabits.ifEmpty { null },
            exerciseFrequency = exerciseFrequency.ifEmpty { null },
            dietaryPreferences = dietaryPreferences.ifEmpty { null },
            createdAt = viewModel.healthInfo.value?.createdAt ?: DateUtil.getCurrentTimestamp(),
            updatedAt = DateUtil.getCurrentTimestamp()
        )
        
        // 保存健康信息
        viewModel.saveHealthInfo(requireContext(), healthInfo)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}