package com.example.chms_android

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.OkhttpUtil

class CHMSApplication : Application(), DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "CHMSApplication"
        private lateinit var instance: CHMSApplication
        
        fun getInstance(): CHMSApplication {
            return instance
        }
    }
    
    override fun onCreate() {
        super<Application>.onCreate()
        
        instance = this
        
        Log.d(TAG, "Application onCreate")
        
        // 初始化数据库
        DatabaseProvider.initDatabase(applicationContext)
        
        // 注册生命周期观察者
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
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