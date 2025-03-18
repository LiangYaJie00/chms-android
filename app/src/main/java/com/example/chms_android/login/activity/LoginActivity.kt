package com.example.chms_android.login.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.MainActivity
import com.example.chms_android.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 数据库初始化操作
        DatabaseProvider.initDatabase(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.login_status_bar)

        DoctorRepo.getAllNetDoctors(this)
        CommunityRepo.getAllNetCommunity(this)
        WindowUtil(this).handleStatusBarHeight()

        checkLoginStatus()

        // 调整视图的padding以适应系统窗口插图
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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