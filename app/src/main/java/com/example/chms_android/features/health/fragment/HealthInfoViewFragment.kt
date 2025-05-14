package com.example.chms_android.features.health.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.databinding.FragmentHealthInfoViewBinding
import com.example.chms_android.features.health.viewmodel.HealthInfoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class HealthInfoViewFragment : Fragment() {
    
    private var _binding: FragmentHealthInfoViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HealthInfoViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthInfoViewBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity()).get(HealthInfoViewModel::class.java)
        
        // 设置观察者，当数据变化时更新UI
        setupObservers()
        
        // 加载健康信息数据
        viewModel.loadHealthInfo(requireContext())
    }
    
    private fun setupObservers() {
        viewModel.healthInfo.observe(viewLifecycleOwner) { healthInfo ->
            if (healthInfo != null) {
                // 更新UI显示健康信息
                binding.tvHeight.text = healthInfo.height?.toString() ?: "未填写"
                binding.tvWeight.text = healthInfo.weight?.toString() ?: "未填写"
                
                // 显示BMI
                binding.tvBmi.text = healthInfo.bmi?.toString() ?: "未知"
                
                // 血压
                if (healthInfo.bloodPressureSystolic != null && healthInfo.bloodPressureDiastolic != null) {
                    binding.tvBloodPressure.text = "${healthInfo.bloodPressureSystolic}/${healthInfo.bloodPressureDiastolic}"
                } else {
                    binding.tvBloodPressure.text = "未填写"
                }
                
                // 心率
                binding.tvHeartRate.text = healthInfo.heartRate?.toString() ?: "未填写"
                
                // 血氧
                binding.tvArterialOxygen.text = healthInfo.arterialBloodOxygenLevel?.toString() ?: "未填写"
                binding.tvVenousOxygen.text = healthInfo.venousBloodOxygenLevel?.toString() ?: "未填写"
                
                // 最近体检日期
                healthInfo.lastMedicalCheckup?.let { date ->
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.tvLastCheckup.text = format.format(date)
                } ?: run {
                    binding.tvLastCheckup.text = "未填写"
                }
                
                binding.tvAllergies.text = healthInfo.allergies ?: "无"
                binding.tvMedicalHistory.text = healthInfo.medicalHistory ?: "无"
                binding.tvFamilyHistory.text = healthInfo.familyMedicalHistory ?: "无"
                binding.tvCurrentConditions.text = healthInfo.currentConditions ?: "无"
                binding.tvMedications.text = healthInfo.medications ?: "无"
                binding.tvVaccinationRecords.text = healthInfo.vaccinationRecords ?: "未填写"
                binding.tvLifestyleHabits.text = healthInfo.lifestyleHabits ?: "未填写"
                binding.tvExerciseFrequency.text = healthInfo.exerciseFrequency ?: "未填写"
                binding.tvDietaryPreferences.text = healthInfo.dietaryPreferences ?: "未填写"
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}