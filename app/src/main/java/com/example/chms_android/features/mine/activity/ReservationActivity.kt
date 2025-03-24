package com.example.chms_android.features.mine.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chms_android.R
import com.example.chms_android.data.Doctor
import com.example.chms_android.databinding.ActivityReservationBinding
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.TUIUtil
import com.example.chms_android.utils.ToastUtil

class ReservationActivity : AppCompatActivity() {
    private val TAG = "ReservationActivity"
    private lateinit var binding: ActivityReservationBinding
    private lateinit var doctorList: List<Doctor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initDoctorList()

        setListener()
    }

    private fun setListener() {
        binding.btnArStartVideo.setOnClickListener {
            try {
                val len = doctorList.size
                TUIUtil.startVideo(doctorList[len-1])
            } catch (e: Exception) {
                Log.e(TAG, e.message + "")
            }
        }
    }

    private fun initDoctorList() {
        DoctorRepo.getAllDoctorsFromDb(
            onComplete = { doctors ->
                // 在这里处理取得的医生列表
                doctorList = doctors
            },
            onError = { error ->
                // 在这里处理错误
                ToastUtil.show(this, "Error fetching doctors: ${error.message}", Toast.LENGTH_SHORT)
            }
        )
    }
}