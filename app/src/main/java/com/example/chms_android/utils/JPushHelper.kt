package com.example.chms_android.utils

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import cn.jpush.android.api.BasicPushNotificationBuilder
import cn.jpush.android.api.JPushInterface
import com.example.chms_android.R
import com.example.chms_android.features.appointment.activity.AppointmentDetailActivity
import org.json.JSONObject

/**
 * JPush 工具类，提供常用的推送相关功能
 */
object JPushHelper {
    private const val TAG = "JPushHelper"
    
    /**
     * 设置别名
     * @param context 上下文
     * @param alias 别名，通常使用用户ID
     * @param sequence 操作序列号，用于区分不同的操作
     */
    fun setAlias(context: Context, alias: String, sequence: Int = 1) {
        JPushInterface.setAlias(context, sequence, alias)
        Log.d(TAG, "设置别名: $alias, 序列号: $sequence")
    }
    
    /**
     * 删除别名
     * @param context 上下文
     * @param sequence 操作序列号，用于区分不同的操作
     */
    fun deleteAlias(context: Context, sequence: Int = 2) {
        JPushInterface.deleteAlias(context, sequence)
        Log.d(TAG, "删除别名, 序列号: $sequence")
    }
    
    /**
     * 设置标签
     * @param context 上下文
     * @param tags 标签集合
     * @param sequence 操作序列号，用于区分不同的操作
     */
    fun setTags(context: Context, tags: Set<String>, sequence: Int = 3) {
        JPushInterface.setTags(context, sequence, tags)
        Log.d(TAG, "设置标签: $tags, 序列号: $sequence")
    }
    
    /**
     * 添加标签
     * @param context 上下文
     * @param tags 要添加的标签集合
     * @param sequence 操作序列号，用于区分不同的操作
     */
    fun addTags(context: Context, tags: Set<String>, sequence: Int = 4) {
        JPushInterface.addTags(context, sequence, tags)
        Log.d(TAG, "添加标签: $tags, 序列号: $sequence")
    }
    
    /**
     * 删除标签
     * @param context 上下文
     * @param tags 要删除的标签集合
     * @param sequence 操作序列号，用于区分不同的操作
     */
    fun deleteTags(context: Context, tags: Set<String>, sequence: Int = 5) {
        JPushInterface.deleteTags(context, sequence, tags)
        Log.d(TAG, "删除标签: $tags, 序列号: $sequence")
    }
    
    /**
     * 清除所有标签
     * @param context 上下文
     * @param sequence 操作序列号，用于区分不同的操作
     */
    fun cleanTags(context: Context, sequence: Int = 6) {
        JPushInterface.cleanTags(context, sequence)
        Log.d(TAG, "清除所有标签, 序列号: $sequence")
    }
    
    /**
     * 获取注册ID
     * @param context 上下文
     * @return 注册ID
     */
    fun getRegistrationID(context: Context): String {
        val regId = JPushInterface.getRegistrationID(context)
        Log.d(TAG, "获取注册ID: $regId")
        return regId
    }
    
    /**
     * 检查推送是否可用
     * @param context 上下文
     * @return 推送是否可用
     */
    fun isPushStopped(context: Context): Boolean {
        val stopped = JPushInterface.isPushStopped(context)
        Log.d(TAG, "推送是否停止: $stopped")
        return stopped
    }
    
    /**
     * 恢复推送
     * @param context 上下文
     */
    fun resumePush(context: Context) {
        JPushInterface.resumePush(context)
        Log.d(TAG, "恢复推送")
    }
    
    /**
     * 停止推送
     * @param context 上下文
     */
    fun stopPush(context: Context) {
        JPushInterface.stopPush(context)
        Log.d(TAG, "停止推送")
    }

    /**
     * 设置用户推送信息（别名和标签）
     * @param context 上下文
     * @param userId 用户ID，用作别名
     * @param userRole 用户角色，用于设置标签
     */
    fun setupUserPushProfile(context: Context, userId: String, userRole: String) {
        // 设置JPush别名为用户ID，与后端格式保持一致
        setAlias(context, "user_$userId")
        
        // 根据用户角色设置标签
        val tags = HashSet<String>()
        tags.add("user_role_$userRole") // 例如 "user_role_patient" 或 "user_role_doctor"
        setTags(context, tags)
        
        Log.d(TAG, "设置用户推送信息 - 用户ID: user_$userId, 角色: $userRole")
    }

    /**
     * 监控JPush注册状态
     * @param context 上下文
     * @param maxAttempts 最大尝试次数
     * @param delayMillis 每次尝试之间的延迟时间（毫秒）
     * @param callback 注册结果回调
     */
    fun monitorRegistrationID(
        context: Context, 
        maxAttempts: Int = 5, 
        delayMillis: Long = 10000,
        callback: ((String?) -> Unit)? = null
    ) {
        var attempts = 0
        val handler = Handler(Looper.getMainLooper())
        
        val checkRunnable = object : Runnable {
            override fun run() {
                attempts++
                val regId = JPushInterface.getRegistrationID(context)
                
                Log.d(TAG, "JPush Registration ID check attempt $attempts: $regId")
                
                if (regId.isNotEmpty()) {
                    // 成功获取到Registration ID
                    Log.d(TAG, "Successfully obtained JPush Registration ID: $regId")
                    callback?.invoke(regId)
                } else if (attempts < maxAttempts) {
                    // 继续尝试
                    Log.d(TAG, "Will retry in ${delayMillis/1000} seconds (attempt ${attempts+1}/$maxAttempts)")
                    handler.postDelayed(this, delayMillis)
                } else {
                    // 达到最大尝试次数
                    Log.e(TAG, "Failed to get JPush Registration ID after $maxAttempts attempts")
                    callback?.invoke(null)
                }
            }
        }
        
        // 开始第一次检查
        handler.post(checkRunnable)
    }

    /**
     * 清除应用角标
     * @param context 上下文
     */
    fun clearBadge(context: Context) {
        try {
            // 清除JPush的角标计数
            JPushInterface.setBadgeNumber(context, 0)
            Log.d(TAG, "清除应用角标")
            
            // 对于不同厂商的设备，可能需要额外的处理
            val manufacturer = Build.MANUFACTURER.toLowerCase()
            when {
                // 小米设备
                manufacturer.contains("xiaomi") -> {
                    val intent = Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE")
                    intent.putExtra("android.intent.extra.update_application_component_name", context.packageName)
                    intent.putExtra("android.intent.extra.update_application_message_text", "0")
                    context.sendBroadcast(intent)
                }
                // 华为设备
                manufacturer.contains("huawei") -> {
                    try {
                        val bundle = Bundle()
                        bundle.putString("package", context.packageName)
                        bundle.putString("class", "com.example.chms_android.MainActivity")
                        bundle.putInt("badgenumber", 0)
                        context.contentResolver.call(
                            Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                            "change_badge", null, bundle
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "清除华为角标失败", e)
                    }
                }
                // 三星设备
                manufacturer.contains("samsung") -> {
                    val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
                    intent.putExtra("badge_count", 0)
                    intent.putExtra("badge_count_package_name", context.packageName)
                    intent.putExtra("badge_count_class_name", "com.example.chms_android.MainActivity")
                    context.sendBroadcast(intent)
                }
                // OPPO设备
                manufacturer.contains("oppo") -> {
                    try {
                        val intent = Intent("com.oppo.unsettledevent")
                        intent.putExtra("pakeageName", context.packageName)
                        intent.putExtra("number", 0)
                        intent.putExtra("upgradeNumber", 0)
                        context.sendBroadcast(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "清除OPPO角标失败", e)
                    }
                }
                // vivo设备
                manufacturer.contains("vivo") -> {
                    try {
                        val intent = Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM")
                        intent.putExtra("packageName", context.packageName)
                        intent.putExtra("className", "com.example.chms_android.MainActivity")
                        intent.putExtra("notificationNum", 0)
                        context.sendBroadcast(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "清除vivo角标失败", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清除应用角标失败", e)
        }
    }

    /**
     * 设置接收推送时不显示角标
     * @param context 上下文
     */
    fun disableBadge(context: Context) {
        try {
            // 设置JPush不显示角标
            JPushInterface.setDefaultPushNotificationBuilder(null)
            
            // 创建自定义通知样式
            val builder = BasicPushNotificationBuilder(context)
            builder.statusBarDrawable = R.drawable.ic_notification // 使用自定义通知图标
            builder.notificationFlags = Notification.FLAG_AUTO_CANCEL
            builder.notificationDefaults = (Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            builder.developerArg0 = "0" // 这个参数会影响角标，设为"0"表示不设置角标
            
            // 设置为默认通知样式
            JPushInterface.setPushNotificationBuilder(1, builder)
            JPushInterface.setDefaultPushNotificationBuilder(builder)
            
            Log.d(TAG, "已设置推送不显示角标")
        } catch (e: Exception) {
            Log.e(TAG, "设置推送不显示角标失败", e)
        }
    }

//    /**
//     * 监控JPush注册ID的获取
//     *
//     * @param context 上下文
//     * @param maxRetries 最大重试次数
//     * @param delayMillis 每次重试的延迟时间（毫秒）
//     * @param callback 回调函数，成功时返回registrationID，失败时返回null
//     */
//    fun monitorRegistrationID(
//        context: Context,
//        maxRetries: Int = 10,
//        delayMillis: Long = 1000,
//        callback: (String?) -> Unit
//    ) {
//        var retries = 0
//
//        fun checkRegistrationID() {
//            val registrationID = JPushInterface.getRegistrationID(context)
//
//            if (registrationID.isNotEmpty()) {
//                // 成功获取到registrationID
//                Log.d(TAG, "获取到JPush注册ID: $registrationID")
//                callback(registrationID)
//            } else if (retries < maxRetries) {
//                // 继续重试
//                retries++
//                Log.d(TAG, "尝试获取JPush注册ID: 第${retries}次")
//
//                // 延迟后再次检查
//                Handler(context.mainLooper).postDelayed({
//                    checkRegistrationID()
//                }, delayMillis)
//            } else {
//                // 达到最大重试次数，返回失败
//                Log.e(TAG, "无法获取JPush注册ID，已达到最大重试次数")
//                callback(null)
//            }
//        }
//
//        // 开始检查
//        checkRegistrationID()
//    }
    
    /**
     * 向指定用户发送通知
     * 
     * @param context 上下文
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param extras 额外信息（JSON字符串）
     */
    fun sendNotificationToUser(
        context: Context,
        userId: String,
        title: String,
        content: String,
        extras: String? = null
    ) {
        // 注意：这个方法在实际应用中应该通过服务器API调用JPush的推送接口
        // 这里仅作为示例，实际实现需要在服务器端完成
        
        Log.d(TAG, "向用户 $userId 发送通知: $title - $content")
        Log.d(TAG, "额外信息: $extras")
        
        // 在实际应用中，这里应该调用服务器API
        // 由于这里无法直接调用JPush的推送API，所以只记录日志
    }
    
    /**
     * 处理接收到的通知
     * 
     * @param context 上下文
     * @param title 通知标题
     * @param content 通知内容
     * @param extras 额外信息
     * @return 是否成功处理
     */
    fun handleReceivedNotification(
        context: Context,
        title: String?,
        content: String?,
        extras: String?
    ): Boolean {
        Log.d(TAG, "收到通知: $title - $content")
        Log.d(TAG, "额外信息: $extras")
        
        try {
            if (extras.isNullOrEmpty()) {
                return false
            }
            
            val extrasJson = JSONObject(extras)
            val type = extrasJson.optString("type")
            
            if (type == "appointment") {
                val appointmentId = extrasJson.optInt("appointmentId", 0)
                val isPatient = extrasJson.optBoolean("isPatient", true)
                
                if (appointmentId > 0) {
                    // 打开预约详情页面
                    val intent = Intent(context, AppointmentDetailActivity::class.java).apply {
                        putExtra("APPOINTMENT_ID", appointmentId)
                        putExtra("IS_PATIENT", isPatient)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                }
            }
            
            return false
        } catch (e: Exception) {
            Log.e(TAG, "处理通知时出错: ${e.message}")
            return false
        }
    }
}
