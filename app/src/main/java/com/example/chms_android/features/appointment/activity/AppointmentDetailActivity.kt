package com.example.chms_android.features.appointment.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chms_android.R
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.data.AppointmentType
import com.example.chms_android.databinding.ActivityAppointmentDetailBinding
import com.example.chms_android.dto.AppointmentDTO
import com.example.chms_android.dto.AppointmentResponseDTO
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.repo.UserRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.TUIUtil
import com.example.chms_android.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AppointmentDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentDetailBinding
    private var appointmentId: Int = 0
    private var isPatient: Boolean = true
    private var appointment: AppointmentDTO? = null
    
    // 广播接收器，用于接收视频通话结束的广播
    private val callEndReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.example.chms_android.CALL_ENDED") {
                // 如果是医生，且预约状态为已确认，则自动将预约标记为已完成
                if (!isPatient && appointment?.status == AppointmentStatus.CONFIRMED) {
                    completeAppointment()
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)
        
        appointmentId = intent.getIntExtra("APPOINTMENT_ID", 0)
        isPatient = intent.getBooleanExtra("IS_PATIENT", true)
        
        if (appointmentId == 0) {
            ToastUtil.show(this, "预约ID无效", Toast.LENGTH_SHORT)
            finish()
            return
        }

        // 注册广播接收器
        LocalBroadcastManager.getInstance(this).registerReceiver(
            callEndReceiver,
            IntentFilter("com.example.chms_android.CALL_ENDED")
        )

        setupButtons()
        loadAppointmentDetails()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 注销广播接收器
        LocalBroadcastManager.getInstance(this).unregisterReceiver(callEndReceiver)
    }

    private fun setupButtons() {
        // 根据是否为患者设置不同的按钮
        if (isPatient) {
            binding.btnAccept.visibility = View.GONE
            binding.btnReject.visibility = View.GONE
            binding.etRejectReason.visibility = View.GONE
            binding.btnComplete.visibility = View.GONE
            
            binding.btnCancel.setOnClickListener {
                cancelAppointment()
            }
            
            binding.btnStartCall.setOnClickListener {
                startVideoCall()
            }
        } else {
            // 医生界面
            binding.btnAccept.setOnClickListener {
                updateAppointmentStatus(AppointmentStatus.CONFIRMED)
            }
            
            binding.btnReject.setOnClickListener {
                if (binding.etRejectReason.text.toString().trim().isEmpty()) {
                    ToastUtil.show(this, "请填写拒绝原因", Toast.LENGTH_SHORT)
                    return@setOnClickListener
                }
                
                updateAppointmentStatus(AppointmentStatus.REJECTED)
            }
            
            binding.btnCancel.setOnClickListener {
                cancelAppointment()
            }
            
            binding.btnStartCall.setOnClickListener {
                startVideoCall()
            }
            
            binding.btnComplete.setOnClickListener {
                completeAppointment()
            }
        }
    }
    
    private fun loadAppointmentDetails() {
        binding.progressBar.visibility = View.VISIBLE
        
        AppointmentRepo.getAppointmentById(
            appointmentId = appointmentId,
            context = this,
            onSuccess = { appointmentDTO ->
                binding.progressBar.visibility = View.GONE
                appointment = appointmentDTO
                
                displayAppointmentDetails(appointmentDTO)
                updateButtonsVisibility(appointmentDTO)
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                ToastUtil.show(this, "加载预约详情失败: $errorMsg", Toast.LENGTH_SHORT)
            }
        )
    }
    
    private fun displayAppointmentDetails(appointment: AppointmentDTO) {
        // 设置基本信息
        binding.tvPatientName.text = "患者: ${appointment.patientName}"
        binding.tvDoctorName.text = "医生: ${appointment.doctorName}"
        binding.tvDate.text = "日期: ${appointment.appointmentDate}"
        binding.tvTime.text = "时间: ${appointment.startTime} - ${appointment.endTime}"
        
        // 设置预约类型
        val typeText = when (appointment.appointmentType) {
            AppointmentType.ONLINE -> "线上预约"
            AppointmentType.OFFLINE -> "线下预约"
            else -> "未知类型"
        }
        binding.tvType.text = "类型: $typeText"
        
        // 设置预约状态
        val statusText = when (appointment.status) {
            AppointmentStatus.PENDING -> "待确认"
            AppointmentStatus.CONFIRMED -> "已确认"
            AppointmentStatus.COMPLETED -> "已完成"
            AppointmentStatus.CANCELLED -> "已取消"
            AppointmentStatus.REJECTED -> "已拒绝"
            else -> "未知状态"
        }
        binding.tvStatus.text = "状态: $statusText"
        
        // 设置预约原因
        binding.tvReason.text = "预约原因: ${appointment.reason ?: "无"}"
        
        // 设置拒绝原因或备注
        if (!appointment.notes.isNullOrEmpty()) {
            binding.tvNotes.visibility = View.VISIBLE
            binding.tvNotes.text = "备注: ${appointment.notes}"
        } else {
            binding.tvNotes.visibility = View.GONE
        }
    }
    
    private fun updateButtonsVisibility(appointment: AppointmentDTO) {
        // 根据预约状态和类型更新按钮可见性
        when (appointment.status) {
            AppointmentStatus.PENDING -> {
                if (isPatient) {
                    // 患者可以取消待确认的预约
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.btnStartCall.visibility = View.GONE
                    binding.btnComplete.visibility = View.GONE
                } else {
                    // 医生可以接受或拒绝待确认的预约
                    binding.btnAccept.visibility = View.VISIBLE
                    binding.btnReject.visibility = View.VISIBLE
                    binding.etRejectReason.visibility = View.VISIBLE
                    binding.btnCancel.visibility = View.GONE
                    binding.btnStartCall.visibility = View.GONE
                    binding.btnComplete.visibility = View.GONE
                }
            }
            AppointmentStatus.CONFIRMED -> {
                // 已确认的预约
                if (appointment.appointmentType == AppointmentType.ONLINE) {
                    // 线上预约，检查是否可以开始视频通话
                    val canStartCall = isAppointmentTimeValid(appointment)
                    binding.btnStartCall.visibility = if (canStartCall) View.VISIBLE else View.GONE
                } else {
                    // 线下预约不需要视频通话按钮
                    binding.btnStartCall.visibility = View.GONE
                }
                
                // 双方都可以取消已确认的预约
                binding.btnCancel.visibility = View.VISIBLE
                
                // 医生可以将预约标记为已完成
                if (!isPatient) {
                    binding.btnComplete.visibility = View.VISIBLE
                } else {
                    binding.btnComplete.visibility = View.GONE
                }
                
                // 隐藏接受/拒绝按钮
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.etRejectReason.visibility = View.GONE
            }
            AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED -> {
                // 已完成、已取消或已拒绝的预约不显示任何操作按钮
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.etRejectReason.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                binding.btnStartCall.visibility = View.GONE
                binding.btnComplete.visibility = View.GONE
            }
            else -> {
                // 其他状态隐藏所有按钮
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.etRejectReason.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                binding.btnStartCall.visibility = View.GONE
                binding.btnComplete.visibility = View.GONE
            }
        }
    }
    
    private fun isAppointmentTimeValid(appointment: AppointmentDTO): Boolean {
        // 检查预约时间是否有效（当前时间是否在预约时间前后15分钟内）
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            
            val appointmentDate = appointment.appointmentDate ?: return false
            val startTime = appointment.startTime ?: return false
            
            val dateTimeString = "$appointmentDate $startTime"
            val appointmentDateTime = dateTimeFormat.parse(dateTimeString) ?: return false
            
            val currentTime = Date()
            val calendar = Calendar.getInstance()
            
            // 设置预约时间
            calendar.time = appointmentDateTime
            
            // 预约时间前15分钟
            val beforeAppointment = Calendar.getInstance()
            beforeAppointment.time = appointmentDateTime
            beforeAppointment.add(Calendar.MINUTE, -15)
            
            // 预约时间后60分钟
            val afterAppointment = Calendar.getInstance()
            afterAppointment.time = appointmentDateTime
            afterAppointment.add(Calendar.MINUTE, 60)
            
            // 检查当前时间是否在预约时间前15分钟到预约时间后60分钟之间
            return currentTime.after(beforeAppointment.time) && currentTime.before(afterAppointment.time)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    private fun updateAppointmentStatus(status: AppointmentStatus) {
        val notes = if (status == AppointmentStatus.REJECTED) {
            binding.etRejectReason.text.toString().trim()
        } else {
            null
        }
        
        binding.progressBar.visibility = View.VISIBLE
        
        // 创建AppointmentResponseDTO对象
        val responseDTO = AppointmentResponseDTO(
            appointmentId = appointmentId,
            doctorId = AccountUtil(this).getUserId().toInt(),
            status = status,
            notes = notes
        )
        
        AppointmentRepo.updateAppointmentStatus(
            responseDTO = responseDTO,
            context = this,
            onSuccess = { updatedAppointment ->
                binding.progressBar.visibility = View.GONE
                
                // 更新本地数据
                appointment = updatedAppointment
                
                // 更新UI
                displayAppointmentDetails(updatedAppointment)
                updateButtonsVisibility(updatedAppointment)
                
                // 显示操作成功提示
                val message = when (status) {
                    AppointmentStatus.CONFIRMED -> "预约已确认"
                    AppointmentStatus.REJECTED -> "预约已拒绝"
                    else -> "预约状态已更新"
                }
                ToastUtil.show(this, message, Toast.LENGTH_SHORT)
            },
            onError = { errorMsg ->
                binding.progressBar.visibility = View.GONE
                ToastUtil.show(this, "更新预约状态失败: $errorMsg", Toast.LENGTH_SHORT)
            }
        )
    }
    
    private fun cancelAppointment() {
        AlertDialog.Builder(this)
            .setTitle("取消预约")
            .setMessage("确定要取消这个预约吗？")
            .setPositiveButton("确定") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE
                
                val notes = if (isPatient) "患者取消预约" else "医生取消预约"
                
                AppointmentRepo.cancelAppointment(
                    appointmentId = appointmentId,
                    notes = notes,
                    context = this,
                    onSuccess = { success ->
                        binding.progressBar.visibility = View.GONE
                        
                        if (success) {
                            ToastUtil.show(this, "预约已取消", Toast.LENGTH_SHORT)
                            
                            // 重新加载预约详情以获取最新状态
                            loadAppointmentDetails()
                        } else {
                            ToastUtil.show(this, "取消预约失败", Toast.LENGTH_SHORT)
                        }
                    },
                    onError = { errorMsg ->
                        binding.progressBar.visibility = View.GONE
                        ToastUtil.show(this, "取消预约失败: $errorMsg", Toast.LENGTH_SHORT)
                    }
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun startVideoCall() {
        // 检查预约是否是线上预约且状态为已确认
        if (appointment?.appointmentType != AppointmentType.ONLINE ||
            appointment?.status != AppointmentStatus.CONFIRMED) {
            ToastUtil.show(this, "无法发起视频通话", Toast.LENGTH_SHORT)
            return
        }

        // 检查预约时间是否有效
        if (!isAppointmentTimeValid(appointment!!)) {
            ToastUtil.show(this, "当前不在预约时间范围内，无法发起视频通话", Toast.LENGTH_SHORT)
            return
        }

        try {
            if (isPatient) {
                // 患者呼叫医生
                // 从数据库获取医生信息
                appointment?.doctorId?.let { doctorId ->
                    UserRepo.getUserById(
                        userId = doctorId,
                        context = this,
                        onSuccess = { user ->
                            // 使用TUIUtil启动视频通话
                            TUIUtil.startVideo(user)

                            // 记录通话开始
                            logCallStart()
                        },
                        onError = { errorMsg ->
                            ToastUtil.show(this, "获取医生信息失败: $errorMsg", Toast.LENGTH_SHORT)
                        }
                    )
                }
            } else {
                // 医生呼叫患者
                // 从数据库获取患者信息
                appointment?.patientId?.let { patientId ->
                    UserRepo.getUserById(
                        userId = patientId,
                        context = this,
                        onSuccess = { user ->
                            // 使用TUIUtil启动视频通话
                            TUIUtil.startVideo(user)

                            // 记录通话开始
                            logCallStart()
                        },
                        onError = { errorMsg ->
                            ToastUtil.show(this, "获取患者信息失败: $errorMsg", Toast.LENGTH_SHORT)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AppointmentDetail", "启动通话失败", e)
            ToastUtil.show(this, "启动通话失败: ${e.message}", Toast.LENGTH_SHORT)
        }
    }

    // 记录通话开始的辅助方法
    private fun logCallStart() {
        // 可以在这里添加记录通话开始的逻辑
        // 例如更新数据库、发送通知等
        Log.i("AppointmentDetail", "通话已开始，预约ID: $appointmentId")

        // 更新预约状态为已确认（因为没有IN_PROGRESS状态）
        val responseDTO = AppointmentResponseDTO(
            appointmentId = appointmentId,
            status = AppointmentStatus.CONFIRMED,  // 使用已确认状态，因为没有进行中状态
            notes = "通话已开始"
        )

        AppointmentRepo.updateAppointmentStatus(
            responseDTO = responseDTO,
            context = this,
            onSuccess = { /* 可以在这里处理成功回调 */ },
            onError = { /* 可以在这里处理错误回调 */ }
        )
    }

    private fun completeAppointment() {
        AlertDialog.Builder(this)
            .setTitle("完成预约")
            .setMessage("确定要将此预约标记为已完成吗？")
            .setPositiveButton("确定") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE
                
                // 创建AppointmentResponseDTO对象
                val responseDTO = AppointmentResponseDTO(
                    appointmentId = appointmentId,
                    doctorId = AccountUtil(this).getUserId().toInt(),
                    status = AppointmentStatus.COMPLETED,
                    notes = "医生已确认完成预约"
                )
                
                AppointmentRepo.updateAppointmentStatus(
                    responseDTO = responseDTO,
                    context = this,
                    onSuccess = { updatedAppointment ->
                        binding.progressBar.visibility = View.GONE
                        
                        // 更新本地数据
                        appointment = updatedAppointment
                        
                        // 更新UI
                        displayAppointmentDetails(updatedAppointment)
                        updateButtonsVisibility(updatedAppointment)
                        
                        // 显示操作成功提示
                        ToastUtil.show(this, "预约已完成", Toast.LENGTH_SHORT)
                    },
                    onError = { errorMsg ->
                        binding.progressBar.visibility = View.GONE
                        ToastUtil.show(this, "更新预约状态失败: $errorMsg", Toast.LENGTH_SHORT)
                    }
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
