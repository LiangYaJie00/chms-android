package com.example.chms_android.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.data.AppointmentType
import com.example.chms_android.receiver.AppointmentNotificationReceiver
import com.example.chms_android.repo.AppointmentRepo
import com.example.chms_android.utils.AccountUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AppointmentNotificationService : Service() {
    private val TAG = "AppointmentNotifService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "预约通知服务已创建")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "预约通知服务已启动")
        
        // 启动定期检查预约的协程
        serviceScope.launch {
            while (true) {
                checkUpcomingAppointments()
                // 每小时检查一次
                delay(TimeUnit.HOURS.toMillis(1))
            }
        }
        
        return START_STICKY
    }
    
    private suspend fun checkUpcomingAppointments() {
        Log.d(TAG, "检查即将到来的预约")
        
        // 获取当前日期
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        
        // 获取今天的所有预约
        AppointmentRepo.getAppointmentsByDate(
            doctorId = AccountUtil(this).getUserId().toInt(),
            date = currentDate,
            context = this,
            onSuccess = { appointments ->
                Log.d(TAG, "今天有 ${appointments.size} 个预约")
                
                // 筛选出线上预约且状态为已确认的预约
                val onlineAppointments = appointments.filter { 
                    it.appointmentType == AppointmentType.ONLINE && 
                    it.status == AppointmentStatus.CONFIRMED 
                }
                
                Log.d(TAG, "今天有 ${onlineAppointments.size} 个线上预约")
                
                // 为每个预约设置通知
                for (appointment in onlineAppointments) {
                    try {
                        val appointmentDate = appointment.appointmentDate ?: continue
                        val startTime = appointment.startTime ?: continue
                        
                        val dateTimeString = "$appointmentDate $startTime"
                        val appointmentDateTime = dateTimeFormat.parse(dateTimeString) ?: continue
                        
                        // 计算预约时间前15分钟
                        val notificationTime = Calendar.getInstance()
                        notificationTime.time = appointmentDateTime
                        notificationTime.add(Calendar.MINUTE, -15)
                        
                        // 如果通知时间已经过了，则跳过
                        if (notificationTime.time.before(Date())) {
                            continue
                        }
                        
                        // 设置通知
                        scheduleNotification(appointment.appointmentId ?: 0, notificationTime.timeInMillis)
                        
                        Log.d(TAG, "为预约 ${appointment.appointmentId} 设置了通知，时间: ${dateTimeFormat.format(notificationTime.time)}")
                    } catch (e: Exception) {
                        Log.e(TAG, "处理预约 ${appointment.appointmentId} 时出错: ${e.message}")
                    }
                }
            },
            onError = { errorMsg ->
                Log.e(TAG, "获取预约列表失败: $errorMsg")
            }
        )
    }
    
    private fun scheduleNotification(appointmentId: Int, notificationTime: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(this, AppointmentNotificationReceiver::class.java).apply {
            putExtra("APPOINTMENT_ID", appointmentId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            appointmentId, // 使用预约ID作为请求码，确保每个预约有唯一的PendingIntent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 设置精确闹钟
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "预约通知服务已销毁")
    }
}