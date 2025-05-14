package com.example.chms_android.login.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.MainActivity
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.databinding.ActivityLoginBinding
import com.example.chms_android.login.dialog.LoginBottomDialog
import com.example.chms_android.login.dialog.RegisterBottomDialog
import com.example.chms_android.login.vm.LoginActivityVM
import com.example.chms_android.repo.CommunityRepo
import com.example.chms_android.repo.DoctorRepo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.TokenUtil
import com.example.chms_android.utils.WindowUtil

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginActivityVM

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 数据库初始化操作
        DatabaseProvider.initDatabase(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置状态栏和导航栏透明
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // 让内容延伸到系统栏区域
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 设置状态栏图标为白色（适合深色背景）
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false // 状态栏图标为白色
            isAppearanceLightNavigationBars = false // 导航栏图标为白色
        }

        DoctorRepo.getAllNetDoctors(this)
        CommunityRepo.getAllNetCommunity(this)
        WindowUtil(this).handleStatusBarHeight()

        checkLoginStatus()

        // 不再需要调整padding，因为我们希望内容延伸到系统栏区域
        // 但我们可能需要为某些特定视图添加内边距，以避免被状态栏遮挡

        viewModel = ViewModelProvider(this).get(LoginActivityVM::class.java)

        initListener()
    }

    private fun checkLoginStatus() {
        if (AccountUtil(this).isUserLoggedIn() && TokenUtil.haveToken(this)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // 设置监听器
    private fun initListener() {
        // 登录
        binding.btnLogin.setOnClickListener {
            val loginBottomDialog = LoginBottomDialog()
            loginBottomDialog.show(supportFragmentManager, "LoginBottomDialog")
        }
        // 注册
        binding.btnRegister.setOnClickListener {
            val registerBottomDialog = RegisterBottomDialog()
            registerBottomDialog.show(supportFragmentManager, "RegisterBottomDialog")
        }
    }

}