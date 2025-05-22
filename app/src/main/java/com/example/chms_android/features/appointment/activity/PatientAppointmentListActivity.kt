package com.example.chms_android.features.appointment.activity

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
import com.example.chms_android.features.appointment.adapter.AppointmentAdapter
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

class PatientAppointmentListActivity : AppCompatActivity(), AppointmentAdapter.AppointmentListener {
    private lateinit var binding: ActivityAppointmentListBinding
    private val appointments = mutableListOf<AppointmentDTO>()
    private lateinit var adapter: AppointmentAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        setupRecyclerView()
        setupCreateButton()
        loadAppointments()
    }
    
    override fun onResume() {
        super.onResume()
        loadAppointments()
    }
    
    private fun setupRecyclerView() {
        adapter = AppointmentAdapter(appointments, this, isPatient = true)
        binding.recyclerViewAppointments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAppointments.adapter = adapter
    }
    
    private fun setupCreateButton() {
        binding.fabCreateAppointment.setOnClickListener {
            val intent = Intent(this, CreateAppointmentActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadAppointments() {
        val patientId = AccountUtil(this).getUserId()
        if (patientId == null) {
            ToastUtil.show(this, "获取用户ID失败", Toast.LENGTH_SHORT)
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        
        AppointmentRepo.getAppointmentsByPatientId(
            patientId = patientId.toInt(),
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
        intent.putExtra("IS_PATIENT", true)
        startActivity(intent)
    }
    
    override fun onCancelAppointment(appointment: AppointmentDTO) {
        // 实现取消预约的逻辑
        val appointmentId = appointment.appointmentId ?: return
        
        AppointmentRepo.cancelAppointment(
            appointmentId = appointmentId,
            notes = "患者取消预约",
            context = this,
            onSuccess = {
                ToastUtil.show(this, "预约已取消", Toast.LENGTH_SHORT)
                loadAppointments()
            },
            onError = { errorMsg ->
                ToastUtil.show(this, "取消预约失败: $errorMsg", Toast.LENGTH_SHORT)
            }
        )
    }
}