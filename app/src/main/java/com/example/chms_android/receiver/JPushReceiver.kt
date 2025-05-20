package com.example.chms_android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.api.NotificationMessage
import com.example.chms_android.MainActivity
import com.example.chms_android.features.analysis.activity.ReportAnalysisActivity
import com.example.chms_android.features.health.activity.HealthInfoActivity
import com.example.chms_android.features.home.activity.DoctorDetailActivity
import com.example.chms_android.utils.JPushHelper
import org.json.JSONException
import org.json.JSONObject

class JPushReceiver : BroadcastReceiver() {
    private val TAG = "JPushReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val bundle = intent.extras
            Log.d(TAG, "onReceive - action: ${intent.action}, extras: $bundle")

            when (intent.action) {
                // 用户注册SDK的 intent
                cn.jpush.android.api.JPushInterface.ACTION_REGISTRATION_ID -> {
                    val regId = bundle?.getString(JPushInterface.EXTRA_REGISTRATION_ID)
                    Log.d(TAG, "Registration ID: $regId")
                }

                // 接收自定义消息的 intent
                cn.jpush.android.api.JPushInterface.ACTION_MESSAGE_RECEIVED -> {
                    Log.d(TAG, "收到自定义消息: ${bundle?.getString(JPushInterface.EXTRA_MESSAGE)}")
                    processCustomMessage(context, bundle)
                }

                // 接收通知的 intent
                cn.jpush.android.api.JPushInterface.ACTION_NOTIFICATION_RECEIVED -> {
                    Log.d(TAG, "收到通知: ${bundle?.getString(JPushInterface.EXTRA_ALERT)}")
                    // 可以在这里处理通知接收事件
                }

                // 用户点击通知的 intent
                cn.jpush.android.api.JPushInterface.ACTION_NOTIFICATION_OPENED -> {
                    Log.d(TAG, "用户点击打开了通知")
                    // 在这里可以根据通知内容打开特定的Activity
                    openNotification(context, bundle)
                }

                // 接收网络变化的 intent
                cn.jpush.android.api.JPushInterface.ACTION_CONNECTION_CHANGE -> {
                    val connected = bundle?.getBoolean(JPushInterface.EXTRA_CONNECTION_CHANGE, false) ?: false
                    Log.d(TAG, "网络连接状态变化: $connected")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "JPush receiver error", e)
        }
    }

    // 处理自定义消息
    private fun processCustomMessage(context: Context, bundle: Bundle?) {
        bundle?.let {
            val message = it.getString(JPushInterface.EXTRA_MESSAGE)
            val extras = it.getString(JPushInterface.EXTRA_EXTRA)
            
            Log.d(TAG, "自定义消息内容: $message, 额外信息: $extras")
            
            // 这里可以发送本地广播或者使用EventBus通知应用内的其他组件
            // 例如使用EventBus:
            // EventBus.getDefault().post(MessageEvent(message, extras))
        }
    }

    // 处理通知点击事件
    private fun openNotification(context: Context, bundle: Bundle?) {
        bundle?.let {
            val extras = it.getString(JPushInterface.EXTRA_EXTRA)
            
            try {
                // 解析额外信息
                val extrasJson = extras?.let { JSONObject(it) }
                
                // 根据通知内容决定打开哪个Activity
                val type = extrasJson?.optString("type")
                
                // 创建Intent并启动相应的Activity
                val intent = when (type) {
                    "healthReport" -> {
                        // 处理健康报告通知
                        val reportId = extrasJson.optString("reportId", "0").toInt()
                        Intent(context, ReportAnalysisActivity::class.java).apply {
                            putExtra("reportId", reportId.toLong())
                        }
                    }
                    "advice" -> {
                        // 处理医生建议通知
                        val adviceId = extrasJson.optString("adviceId", "0").toInt()
                        Intent(context, DoctorDetailActivity::class.java).apply {
                            putExtra("adviceId", adviceId)
                        }
                    }
                    "health_report" -> Intent(context, HealthInfoActivity::class.java)
                    "doctor_message" -> Intent(context, DoctorDetailActivity::class.java)
                    else -> Intent(context, MainActivity::class.java)
                }
                
                // 添加标志，确保Activity在新的任务栈中启动
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                
                // 可以将额外信息传递给Activity
                intent.putExtra("NOTIFICATION_EXTRAS", extras)
                
                context.startActivity(intent)
                
                // 清除角标
                try {
                    JPushHelper.clearBadge(context)
                } catch (e: Exception) {
                    Log.e(TAG, "清除角标失败", e)
                }
            } catch (e: JSONException) {
                Log.e(TAG, "解析通知额外信息失败", e)
                // 如果解析失败，打开主Activity
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "找不到指定的Activity类", e)
                // 如果找不到指定的Activity，打开主Activity
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }
        }
    }
}