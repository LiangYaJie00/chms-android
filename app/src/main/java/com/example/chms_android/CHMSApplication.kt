package com.example.chms_android

import android.app.Application
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LifecycleObserver
import cn.jpush.android.api.JPushInterface
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.service.AppointmentNotificationService
import com.example.chms_android.utils.JPushHelper
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.NotificationPermissionUtil

class CHMSApplication : Application(), DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "CHMSApplication"
        private lateinit var instance: CHMSApplication
        
        fun getInstance(): CHMSApplication {
            return instance
        }

        /**
         * 获取应用程序上下文
         */
        fun getAppContext(): Context {
            return instance.applicationContext
        }
    }
    
    override fun onCreate() {
        super<Application>.onCreate()
        
        instance = this
        
        Log.d(TAG, "Application onCreate")
        
        // 初始化数据库
        DatabaseProvider.initDatabase(applicationContext)
        
        // 初始化JPush
        initJPush()

        // 注册生命周期观察者
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // 注册Activity生命周期回调，用于检查通知权限
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // 当Activity创建时，检查是否是主Activity
                if (activity is AppCompatActivity && activity.javaClass.simpleName == "MainActivity") {
                    // 在主Activity创建时检查通知权限
                    if (!NotificationPermissionUtil.hasNotificationPermission(activity)) {
                        Log.d(TAG, "检测到通知权限未开启，将在MainActivity中请求")
                    }
                }
            }
            
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        // 启动预约通知服务
        val notificationServiceIntent = Intent(this, AppointmentNotificationService::class.java)
        startService(notificationServiceIntent)
    }
    
    // 初始化JPush
    private fun initJPush() {
        try {
            // 打印设备信息，有助于调试
            Log.d(TAG, "Device manufacturer: ${Build.MANUFACTURER}")
            Log.d(TAG, "Device model: ${Build.MODEL}")
            Log.d(TAG, "Android version: ${Build.VERSION.RELEASE}")
            
            // 设置调试模式
            JPushInterface.setDebugMode(true) // 发布时改为false
            
            // 初始化JPush
            JPushInterface.init(this)
            Log.d(TAG, "JPush initialized")
            
            // 获取Registration ID
            val regId = JPushInterface.getRegistrationID(this)
            if (regId.isNotEmpty()) {
                Log.d(TAG, "JPush Registration ID: $regId")
            } else {
                Log.d(TAG, "JPush Registration ID not received yet")
            }
            
            // 使用JPushHelper监控注册状态
            JPushHelper.monitorRegistrationID(this, 10, 5000) { regId ->
                if (regId != null) {
                    Log.d(TAG, "JPush registration successful: $regId")
                } else {
                    Log.e(TAG, "JPush registration failed after multiple attempts")
                    // 如果注册失败，尝试重新初始化
                    JPushInterface.init(this)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "JPush initialization error", e)
        }
    }

    // 使用新的生命周期回调方法
    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "Application entered background")
        // 应用进入后台，可以执行一些清理工作
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "Application destroyed")
        // 应用被销毁，关闭OkHttpClient
        OkhttpUtil.shutdown()
    }
    
    override fun onTerminate() {
        Log.d(TAG, "Application terminated")
        // 应用终止时关闭OkHttpClient
        OkhttpUtil.shutdown()
        super.onTerminate()
    }
}