package com.example.chms_android.features.home.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.chms_android.R
import com.example.chms_android.data.Doctor
import com.example.chms_android.databinding.ActivityDoctorDetailBinding

class DoctorDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDoctorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 接收传递过来的 Doctor 数据
        val doctor: Doctor? = intent.getParcelableExtra("doctorData")
        val avatar: Int? = intent.getIntExtra("doctorAvatar", -1)
        if (doctor != null) {
            // 在这里使用 doctor 对象
            Glide.with(this)
                .load(avatar)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16)))
                .into(binding.ivActddAvatar)

            binding.tvActddName.text = doctor.name
            binding.tvActddSpecialization.text = doctor.specialization
            binding.tvActddEmail.text = doctor.email
            binding.tvActddPhone.text = doctor.phoneNumber
            binding.tvActddCommunityName.text = doctor.communityName
        }

    }
}