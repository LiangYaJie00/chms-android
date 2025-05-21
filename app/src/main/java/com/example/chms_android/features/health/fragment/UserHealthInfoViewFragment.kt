package com.example.chms_android.features.health.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.databinding.FragmentHealthInfoViewBinding
import com.example.chms_android.features.health.viewmodel.UserHealthInfoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class UserHealthInfoViewFragment : Fragment() {
    private var _binding: FragmentHealthInfoViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserHealthInfoViewModel
    private var userId: Int? = null

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: Int): UserHealthInfoViewFragment {
            val fragment = UserHealthInfoViewFragment()
            val args = Bundle()
            args.putInt(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getInt(ARG_USER_ID, -1)
        if (userId == -1) userId = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHealthInfoViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        loadUserHealthInfo()
    }

    private fun setupViewModel() {
        viewModel = UserHealthInfoViewModel()
    }

    private fun loadUserHealthInfo() {
        userId?.let { id ->
            viewModel.getUserHealthInfo(id, requireContext()) { healthInfo ->
                updateUI(healthInfo)
            }
        }
    }

    private fun updateUI(healthInfo: HealthInfo) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}