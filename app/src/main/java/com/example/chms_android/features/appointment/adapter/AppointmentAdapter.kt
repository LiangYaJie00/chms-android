package com.example.chms_android.features.appointment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.R
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.data.AppointmentType
import com.example.chms_android.databinding.ItemAppointmentBinding
import com.example.chms_android.dto.AppointmentDTO

class AppointmentAdapter(
    private val appointments: List<AppointmentDTO>,
    private val listener: AppointmentListener,
    private val isPatient: Boolean
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {
    
    interface AppointmentListener {
        fun onViewAppointment(appointment: AppointmentDTO)
        fun onCancelAppointment(appointment: AppointmentDTO)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppointmentViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }
    
    override fun getItemCount(): Int = appointments.size
    
    inner class AppointmentViewHolder(private val binding: ItemAppointmentBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(appointment: AppointmentDTO) {
            // 设置基本信息
            if (isPatient) {
                binding.tvName.text = "医生: ${appointment.doctorName}"
            } else {
                binding.tvName.text = "患者: ${appointment.patientName}"
            }
            
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
            
            // 设置状态颜色
            val statusColor = when (appointment.status) {
                AppointmentStatus.PENDING -> R.color.colorWarning
                AppointmentStatus.CONFIRMED -> R.color.colorPrimary
                AppointmentStatus.COMPLETED -> R.color.colorSuccess
                AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED -> R.color.colorDanger
                else -> R.color.colorGray
            }
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, statusColor))
            
            // 设置取消按钮可见性
            if (appointment.status == AppointmentStatus.PENDING || 
                appointment.status == AppointmentStatus.CONFIRMED) {
                binding.btnCancel.visibility = View.VISIBLE
            } else {
                binding.btnCancel.visibility = View.GONE
            }
            
            // 设置点击事件
            binding.cardAppointment.setOnClickListener {
                listener.onViewAppointment(appointment)
            }
            
            binding.btnCancel.setOnClickListener {
                listener.onCancelAppointment(appointment)
            }
        }
    }
}