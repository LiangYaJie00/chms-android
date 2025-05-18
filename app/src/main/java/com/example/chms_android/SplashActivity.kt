package com.example.chms_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chms_android.common.Constants
import com.example.chms_android.data.Role
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.databinding.ActivitySplashBinding
import com.example.chms_android.features_doctor.DoctorMainActivity
import com.example.chms_android.login.activity.LoginActivity
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.TokenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val TAG = "SplashActivity"
    
    // 最小显示时间，确保启动页至少显示这么长时间
    private val MIN_SPLASH_TIME = 1500L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 确保数据库初始化
        DatabaseProvider.initDatabase(applicationContext)
        
        // 设置全屏模式，让内容延伸到状态栏和导航栏
        setupFullScreenMode()
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化应用
        initializeApp()
    }
    
    private fun setupFullScreenMode() {
        // 使用 WindowCompat 让内容延伸到系统栏（状态栏和导航栏）下方
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 设置状态栏和导航栏为透明
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        
        // 获取 WindowInsetsController 来控制系统栏的外观
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        
        // 设置状态栏图标为亮色（白色），适合深色背景
        windowInsetsController.isAppearanceLightStatusBars = false
        
        // 设置导航栏图标为亮色（白色），适合深色背景
        windowInsetsController.isAppearanceLightNavigationBars = false
        
        // 可选：如果您想在用户向上滑动时隐藏系统栏
        // windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    
    private fun initializeApp() {
        val startTime = System.currentTimeMillis()
        
        // 使用协程在后台线程中执行网络检查
        CoroutineScope(Dispatchers.IO).launch {
            // 检查网络连接
            val isNetworkAvailable = NetworkUtil.isNetworkAvailable()
            
            // 如果网络可用，检查服务器连接
            val isServerReachable = if (isNetworkAvailable) {
                NetworkUtil.checkServerReachable(Constants.TEST_URL)
            } else {
                false
            }
            
            // 计算已经过去的时间
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = (MIN_SPLASH_TIME - elapsedTime).coerceAtLeast(0)
            
            // 等待最小显示时间
            if (remainingTime > 0) {
                Thread.sleep(remainingTime)
            }
            
            // 切换到主线程处理UI
            withContext(Dispatchers.Main) {
                when {
                    !isNetworkAvailable -> {
                        // 网络不可用
                        showNetworkErrorDialog("网络连接不可用", 
                            "请检查您的网络设置后重试。您可以选择继续使用应用的离线功能。")
                    }
                    !isServerReachable -> {
                        // 服务器不可达
                        showNetworkErrorDialog("服务器连接失败", 
                            "无法连接到服务器(${Constants.BASE_URL})。应用可能无法正常工作。您可以选择继续使用应用的离线功能。")
                    }
                    else -> {
                        // 一切正常，继续启动流程
                        proceedToNextScreen()
                    }
                }
            }
        }
    }
    
    private fun showNetworkErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("继续离线使用") { dialog, _ ->
                dialog.dismiss()
                proceedToNextScreen(isOfflineMode = true)
            }
            .setNegativeButton("退出") { _, _ -> 
                finish() 
            }
            .setCancelable(false)
            .show()
    }
    
    private fun proceedToNextScreen(isOfflineMode: Boolean = false) {
        // 检查用户登录状态
        if (AccountUtil(this).isUserLoggedIn() && TokenUtil.haveToken(this)) {
            // 获取用户角色
            val user = AccountUtil(this).getUser()
            val intent = when (user?.role) {
                Role.doctor -> Intent(this, DoctorMainActivity::class.java)
                else -> Intent(this, MainActivity::class.java) // 默认为consumer角色
            }
            
            if (isOfflineMode) {
                intent.putExtra("OFFLINE_MODE", true)
            }
            startActivity(intent)
        } else {
            // 用户未登录，进入登录界面
            val intent = Intent(this, LoginActivity::class.java)
            if (isOfflineMode) {
                intent.putExtra("OFFLINE_MODE", true)
            }
            startActivity(intent)
        }
        finish()
    }
}
