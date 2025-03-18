package com.example.chms_android.features.home.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chms_android.R
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.databinding.ActivityHealthInfoSubmitBinding
import com.example.chms_android.repo.HealthInfoRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.DateUtil
import com.example.chms_android.utils.ToastUtil
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthInfoSubmitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHealthInfoSubmitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthInfoSubmitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAhisSubmit.setOnClickListener {
            val healthInfo = getHealthInfoData()
            HealthInfoRepo.addHealthInfo(healthInfo, this)
        }

    }

    private fun getHealthInfoData(): HealthInfo {
        // 体重、身高
        val weightText = binding.edtAhisWeight.text.toString()
        val heightText = binding.edtAhisHeight.text.toString()

        val weight: BigDecimal? = weightText.toBigDecimalOrNull()
        val height: BigDecimal? = heightText.toBigDecimalOrNull()

        // Calculate BMI if weight and height are present
        val bmi = if (weight != null && height != null && height != BigDecimal.ZERO) {
            // 将 height（以厘米为单位）转换为米，并计算 BMI
            weight.divide((height.divide(BigDecimal(100)).pow(2)), 2, BigDecimal.ROUND_HALF_UP)
        } else {
            null
        }

        // 收缩压、舒张压
        val systolicPressureText = binding.edtAhisSystolicPressure.text.toString()
        val diastolicPressureText = binding.edtAhisDiastolicPressure.text.toString()

        val systolicPressure: Int? = systolicPressureText.toIntOrNull()
        val diastolicPressure: Int? = diastolicPressureText.toIntOrNull()

        // 最近体检日期
        val checkupDateText = binding.edtAhisRecentCheckupDate.text.toString()

        // 日期格式为 "yyyy-MM-dd"
        val checkupDate: Date? = checkupDateText.toDateOrNull("yyyy-MM-dd")

        val userId = AccountUtil(this).getUserId()

        return HealthInfo(
            healthId = 0,
            userId = 11,
            weight = weight,
            height = height,
            bmi = bmi,
            bloodPressureSystolic = systolicPressure,
            bloodPressureDiastolic = diastolicPressure,
            lastMedicalCheckup = checkupDate,
            allergies = binding.edtAhisAllergyHistory.text.toString(),
            medicalHistory = binding.edtAhisMedicalHistory.text.toString(),
            familyMedicalHistory = binding.edtAhisFamilyHistory.text.toString(),
            currentConditions = binding.edtAhisCurrentDisease.text.toString(),
            medications = binding.edtAhisMedicationStatus.text.toString(),
            vaccinationRecords = binding.edtAhisVaccinationRecord.text.toString(),
            lifestyleHabits = binding.edtAhisLifestyle.text.toString(),
            exerciseFrequency = binding.edtAhisExerciseFrequency.text.toString(),
            dietaryPreferences = binding.edtAhisDietaryHabits.text.toString(),
            createdAt = DateUtil.getCurrentTimestamp(), // Assuming current date as created time
            updatedAt = DateUtil.getCurrentTimestamp() // Assuming current date as updated time
        )
    }

    fun String.toBigDecimalOrNull(): BigDecimal? {
        return try {
            if (this.isNotEmpty()) BigDecimal(this) else null
        } catch (e: NumberFormatException) {
            // 记录或处理异常
            e.printStackTrace()
            null
        }
    }

    fun String.toIntOrNull(): Int? {
        return try {
            if (this.isNotEmpty()) this.toInt() else null
        } catch (e: NumberFormatException) {
            // 记录或处理异常
            e.printStackTrace()
            null
        }
    }

    fun String.toDateOrNull(format: String): Date? {
        return try {
            if (this.isNotEmpty()) {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                dateFormat.parse(this)
            } else {
                null
            }
        } catch (e: Exception) {
            // 记录或处理异常
            e.printStackTrace()
            null
        }
    }
}