package com.example.chms_android.features.home.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chms_android.R
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.databinding.ActivityDailyHealthReportBinding
import com.example.chms_android.databinding.ActivityDailyReportBinding
import com.example.chms_android.repo.DailyHealthReportRepo
import com.example.chms_android.utils.AccountUtil
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class DailyHealthReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyHealthReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDailyHealthReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAdhrSubmit.setOnClickListener {
            val dailyHealthReport = getDailyHealthReportData()
            DailyHealthReportRepo.addDailyHealthReport(dailyHealthReport, this)
        }

    }

    private fun getDailyHealthReportData(): DailyHealthReport {
        // Parse EditText data, handling potential conversion errors for each field

        val userId = AccountUtil(this).getUserId()

        val reportDateString = binding.adhrReportDate.text.toString()
        val reportDate = if (reportDateString.isNotBlank()) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(reportDateString)
        } else {
            null
        }

        val heartRate = binding.adhrHeartRate.text.toString().toIntOrNull()

        val bloodPressureSystolic = binding.adhrBloodPressureSystolic.text.toString().toIntOrNull()
        val bloodPressureDiastolic = binding.adhrBloodPressureDiastolic.text.toString().toIntOrNull()

        val bodyTemperatureString = binding.adhrBodyTemperature.text.toString()
        val bodyTemperature = bodyTemperatureString.toBigDecimalOrNull()

        val respiratoryRate = binding.adhrRespiratoryRate.text.toString().toIntOrNull()

        val weightString = binding.adhrWeight.text.toString()
        val weight = weightString.toBigDecimalOrNull()

        // Uncomment and modify the below lines if you want to calculate BMI
        // val bmiString = binding.adhrBmi.text.toString()
        // val bmi = bmiString.toBigDecimalOrNull()

        val bloodSugarString = binding.adhrBloodSugar.text.toString()
        val bloodSugar = bloodSugarString.toBigDecimalOrNull()

        val steps = binding.adhrSteps.text.toString().toIntOrNull()
        val exerciseDuration = binding.adhrExerciseDuration.text.toString().toIntOrNull()
        val sleepDuration = binding.adhrSleepDuration.text.toString().toIntOrNull()

        val sleepQuality = binding.adhrSleepQuality.text.toString()

        val caloriesIntake = binding.adhrCaloriesIntake.text.toString().toIntOrNull()

        val waterIntake = binding.adhrWaterIntake.text.toString().toIntOrNull()

        val emotionalState = binding.adhrEmotionalState.text.toString()
        val stressLevel = binding.adhrStressLevel.text.toString()

        val arterialBloodOxygenLevelString = binding.adhrArterialBloodOxygenLevel.text.toString()
        val arterialBloodOxygenLevel = arterialBloodOxygenLevelString.toBigDecimalOrNull()

        val venousBloodOxygenLevelString = binding.adhrVenousBloodOxygenLevel.text.toString()
        val venousBloodOxygenLevel = venousBloodOxygenLevelString.toBigDecimalOrNull()

        val notes = binding.adhrNotes.text.toString()

        // Construct and return the DailyHealthReport object, providing all data
        return DailyHealthReport(
            userId = 11,
            reportDate = reportDate,
            heartRate = heartRate,
            bloodPressureSystolic = bloodPressureSystolic,
            bloodPressureDiastolic = bloodPressureDiastolic,
            bodyTemperature = bodyTemperature,
            respiratoryRate = respiratoryRate,
            weight = weight,
            // bmi = bmi, // Uncomment if you decide to calculate BMI
            bloodSugar = bloodSugar,
            steps = steps,
            exerciseDuration = exerciseDuration,
            sleepDuration = sleepDuration,
            sleepQuality = sleepQuality,
            caloriesIntake = caloriesIntake,
            waterIntake = waterIntake,
            emotionalState = emotionalState,
            stressLevel = stressLevel,
            arterialBloodOxygenLevel = arterialBloodOxygenLevel,
            venousBloodOxygenLevel = venousBloodOxygenLevel,
            notes = notes,
            // Set createdAt and updatedAt to current timestamp if needed
            createdAt = Timestamp(System.currentTimeMillis()),
            updatedAt = Timestamp(System.currentTimeMillis())
        )
    }

}