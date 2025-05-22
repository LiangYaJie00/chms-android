package com.example.chms_android.features_doctor.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityAppointmentListBinding
import com.example.chms_android.dto.AppointmentDTO
import com.example.chms_android.features.appointment.activity.AppointmentDetailActivity
import com.example.chms_android.features.appointment.adapter.AppointmentAdapter
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class DoctorAppointmentListActivity : AppCompatActivity(), AppointmentAdapter.AppointmentListener {
    private lateinit var binding: ActivityAppointmentListBinding
    private val appointments = mutableListOf<AppointmentDTO>()
    private lateinit var adapter: AppointmentAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        setupTitleBar()
        setupRecyclerView()
        setupCreateButton()
        loadAppointments()
    }
    
    override fun onResume() {
        super.onResume()
        loadAppointments()
    }
    
    private fun setupTitleBar() {
        binding.titleBar.setTitleText("我的预约")
    }
    
    private fun setupRecyclerView() {
        adapter = AppointmentAdapter(appointments, this, isPatient = false)
        binding.recyclerViewAppointments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAppointments.adapter = adapter
    }
    
    private fun setupCreateButton() {
        // 医生不需要创建预约按钮，而是设置可用时间段按钮
        binding.fabCreateAppointment.setImageResource(android.R.drawable.ic_menu_my_calendar)
        binding.fabCreateAppointment.setOnClickListener {
            val intent = Intent(this, DoctorAvailableTimeActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadAppointments() {
        val doctorId = AccountUtil(this).getUserId()
        if (doctorId == null) {
            ToastUtil.show(this, "获取用户ID失败", Toast.LENGTH_SHORT)
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        
        AppointmentRepo.getAppointmentsByDoctorId(
            doctorId = doctorId.toInt(),
            context = this,
            onSuccess = { appointmentList ->
                binding.progressBar.visibility = View.GONE
                
                appointments.clear()
                appointments.addAll(appointmentList)
                adapter.notifyDataSetChanged()
                
                updateEmptyView()
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                ToastUtil.show(this, "加载预约列表失败: $errorMsg", Toast.LENGTH_SHORT)
                
                updateEmptyView()
            }
        )
    }
    
    private fun updateEmptyView() {
        if (appointments.isEmpty()) {
            binding.tvEmptyView.visibility = View.VISIBLE
            binding.recyclerViewAppointments.visibility = View.GONE
        } else {
            binding.tvEmptyView.visibility = View.GONE
            binding.recyclerViewAppointments.visibility = View.VISIBLE
        }
    }
    
    override fun onViewAppointment(appointment: AppointmentDTO) {
        val intent = Intent(this, AppointmentDetailActivity::class.java)
        intent.putExtra("APPOINTMENT_ID", appointment.appointmentId)
        intent.putExtra("IS_PATIENT", false)
        startActivity(intent)
    }
    
    override fun onCancelAppointment(appointment: AppointmentDTO) {
        // 医生不能直接取消预约，而是在详情页面处理
        onViewAppointment(appointment)
    }
}