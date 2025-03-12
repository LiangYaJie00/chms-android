package com.example.chms_android.features.home.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.data.Doctor
import com.example.chms_android.databinding.ActivityDoctorsBinding
import com.example.chms_android.features.home.adapter.DoctorShowsAdapter
import com.example.chms_android.features.home.vm.DoctorsActivityVM
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.ToastUtil

class DoctorsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorsBinding
    private lateinit var viewModel: DoctorsActivityVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDoctorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel = ViewModelProvider(this).get(DoctorsActivityVM::class.java)

        initRecyclerView()

    }

    private fun initRecyclerView() {
        initDoctorList()
        binding.recyclerViewAdDoctors.layoutManager = LinearLayoutManager(this)
        viewModel.doctorList.observe(this, Observer { doctorList ->
            binding.recyclerViewAdDoctors.adapter = DoctorShowsAdapter(this, doctorList)
        })
    }

    private fun initDoctorList() {
        DoctorRepo.getAllDoctorsFromDb(
            onComplete = { doctors ->
                // 在这里处理取得的医生列表
                viewModel.setDoctorList(doctors)
            },
            onError = { error ->
                // 在这里处理错误
                ToastUtil.show(this, "Error fetching doctors: ${error.message}", Toast.LENGTH_SHORT)
            }
        )
    }
}