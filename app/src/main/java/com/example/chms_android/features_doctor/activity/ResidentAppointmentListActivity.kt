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
import com.example.chms_android.features.appointment.activity.CreateAppointmentActivity
import com.example.chms_android.features.appointment.adapter.AppointmentAdapter
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.ToastUtil

/**
 * 显示特定医生与特定居民之间的预约记录
 */
class ResidentAppointmentListActivity : AppCompatActivity(), AppointmentAdapter.AppointmentListener {
    private lateinit var binding: ActivityAppointmentListBinding
    private val appointments = mutableListOf<AppointmentDTO>()
    private lateinit var adapter: AppointmentAdapter
    
    private var residentId: Int = 0
    private var residentName: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        // 获取传递的居民ID和姓名
        residentId = intent.getIntExtra("userId", 0)
        residentName = intent.getStringExtra("userName")
        
        if (residentId <= 0) {
            ToastUtil.show(this, "居民ID无效", Toast.LENGTH_SHORT)
            finish()
            return
        }
        
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
        val titleText = if (residentName.isNullOrEmpty()) {
            "居民预约记录"
        } else {
            "$residentName 的预约记录"
        }
        binding.titleBar.setTitleText(titleText)
    }
    
    private fun setupRecyclerView() {
        adapter = AppointmentAdapter(appointments, this, isPatient = false)
        binding.recyclerViewAppointments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAppointments.adapter = adapter
    }
    
    private fun setupCreateButton() {
        // 设置为添加预约按钮
        binding.fabCreateAppointment.setImageResource(android.R.drawable.ic_input_add)
        binding.fabCreateAppointment.setOnClickListener {
            val doctorId = AccountUtil(this).getUserId()?.toInt() ?: return@setOnClickListener
            
            // 跳转到创建预约页面，并传递医生ID和居民ID
            val intent = Intent(this, CreateAppointmentActivity::class.java).apply {
                putExtra("DOCTOR_ID", doctorId)
                putExtra("PATIENT_ID", residentId)
                // 默认为线下预约
                putExtra("APPOINTMENT_TYPE", 0)
            }
            startActivity(intent)
        }
    }
    
    private fun loadAppointments() {
        val doctorId = AccountUtil(this).getUserId()
        if (doctorId == null) {
            ToastUtil.show(this, "获取医生ID失败", Toast.LENGTH_SHORT)
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        
        // 使用新添加的方法获取特定医生和居民之间的预约
        AppointmentRepo.getAppointmentsByDoctorAndPatient(
            doctorId = doctorId.toInt(),
            patientId = residentId,
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
            binding.tvEmptyView.text = "暂无预约记录"
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