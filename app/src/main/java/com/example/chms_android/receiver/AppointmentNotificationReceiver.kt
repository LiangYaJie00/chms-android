package com.example.chms_android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.chms_android.features.appointment.activity.AppointmentDetailActivity
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.utils.JPushHelper
import org.json.JSONObject

class AppointmentNotificationReceiver : BroadcastReceiver() {
    private val TAG = "AppointmentNotifReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        val appointmentId = intent.getIntExtra("APPOINTMENT_ID", 0)
        
        if (appointmentId == 0) {
            Log.e(TAG, "收到无效的预约ID")
            return
        }
        
        Log.d(TAG, "收到预约通知: 预约ID = $appointmentId")
        
        // 获取预约详情
        AppointmentRepo.getAppointmentById(
            appointmentId = appointmentId,
            context = context,
            onSuccess = { appointment ->
                // 发送通知给患者
                val patientId = appointment.patientId
                if (patientId != null) {
                    val patientMessage = "您有一个线上预约即将开始，医生: ${appointment.doctorName}，时间: ${appointment.appointmentDate} ${appointment.startTime}"
                    
                    // 构建通知的额外信息
                    val patientExtras = JSONObject().apply {
                        put("type", "appointment")
                        put("appointmentId", appointmentId)
                        put("isPatient", true)
                    }.toString()
                    
                    // 使用极光推送发送通知
                    JPushHelper.sendNotificationToUser(
                        context,
                        patientId.toString(),
                        "预约提醒",
                        patientMessage,
                        patientExtras
                    )
                    
                    Log.d(TAG, "已发送通知给患者: $patientId")
                }
                
                // 发送通知给医生
                val doctorId = appointment.doctorId
                if (doctorId != null) {
                    val doctorMessage = "您有一个线上预约即将开始，患者: ${appointment.patientName}，时间: ${appointment.appointmentDate} ${appointment.startTime}"
                    
                    // 构建通知的额外信息
                    val doctorExtras = JSONObject().apply {
                        put("type", "appointment")
                        put("appointmentId", appointmentId)
                        put("isPatient", false)
                    }.toString()
                    
                    // 使用极光推送发送通知
                    JPushHelper.sendNotificationToUser(
                        context,
                        doctorId.toString(),
                        "预约提醒",
                        doctorMessage,
                        doctorExtras
                    )
                    
                    Log.d(TAG, "已发送通知给医生: $doctorId")
                }
            },
            onError = { errorMsg ->
                Log.e(TAG, "获取预约详情失败: $errorMsg")
            }
        )
    }
}