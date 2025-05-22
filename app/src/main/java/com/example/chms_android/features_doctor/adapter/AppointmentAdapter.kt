package com.example.chms_android.features_doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chms_android.R
import com.example.chms_android.data.Appointment
import com.example.chms_android.data.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.Locale

class  AppointmentAdapter(
    private val context: Context,
    private var appointmentList: List<Appointment>,
    private val onAppointmentClickListener: OnAppointmentClickListener
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    interface OnAppointmentClickListener {
        fun onAppointmentClick(appointment: Appointment)
        fun onAcceptAppointment(appointment: Appointment)
        fun onRejectAppointment(appointment: Appointment)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientName: TextView = itemView.findViewById(R.id.tv_appointment_patient_name)
        val status: TextView = itemView.findViewById(R.id.tv_appointment_status)
        val date: TextView = itemView.findViewById(R.id.tv_appointment_date)
        val time: TextView = itemView.findViewById(R.id.tv_appointment_time)
        val reason: TextView = itemView.findViewById(R.id.tv_appointment_reason)
        val actionsLayout: LinearLayout = itemView.findViewById(R.id.ll_appointment_actions)
        val acceptButton: Button = itemView.findViewById(R.id.btn_appointment_accept)
        val rejectButton: Button = itemView.findViewById(R.id.btn_appointment_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointmentList[position]
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        holder.patientName.text = appointment.patientName ?: "未知患者"
        
        // 使用appointmentDate而不是date
        holder.date.text = appointment.appointmentDate?.let { dateFormat.format(it) } ?: "未知日期"
        
        // 格式化开始和结束时间
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTimeStr = appointment.startTime?.let { timeFormat.format(it) } ?: "--:--"
        val endTimeStr = appointment.endTime?.let { timeFormat.format(it) } ?: "--:--"
        holder.time.text = "$startTimeStr-$endTimeStr"
        
        holder.reason.text = "预约原因：${appointment.reason ?: "无"}"

        // 设置状态文本和颜色
        when (appointment.status) {
            AppointmentStatus.PENDING -> {
                holder.status.text = "待确认"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.primary))
                holder.actionsLayout.visibility = View.VISIBLE
            }
            AppointmentStatus.CONFIRMED -> {
                holder.status.text = "已确认"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
                holder.actionsLayout.visibility = View.GONE
            }
            AppointmentStatus.COMPLETED -> {
                holder.status.text = "已完成"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.blue))
                holder.actionsLayout.visibility = View.GONE
            }
            AppointmentStatus.CANCELLED -> {
                holder.status.text = "已取消"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.red))
                holder.actionsLayout.visibility = View.GONE
            }
            else -> {
                holder.status.text = "未知状态"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.actionsLayout.visibility = View.GONE
            }
        }

        // 设置点击事件
        holder.itemView.setOnClickListener {
            onAppointmentClickListener.onAppointmentClick(appointment)
        }

        holder.acceptButton.setOnClickListener {
            onAppointmentClickListener.onAcceptAppointment(appointment)
        }

        holder.rejectButton.setOnClickListener {
            onAppointmentClickListener.onRejectAppointment(appointment)
        }
    }

    override fun getItemCount(): Int = appointmentList.size

    fun updateAppointments(newAppointmentList: List<Appointment>) {
        appointmentList = newAppointmentList
        notifyDataSetChanged()
    }
}