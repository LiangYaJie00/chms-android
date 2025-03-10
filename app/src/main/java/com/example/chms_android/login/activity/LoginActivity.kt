package com.example.chms_android.login.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.R
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.databinding.ActivityLoginBinding
import com.example.chms_android.login.dialog.LoginBottomDialog
import com.example.chms_android.login.dialog.RegisterBottomDialog
import com.example.chms_android.login.vm.LoginActivityVM

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

        // 调整视图的padding以适应系统窗口插图
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this).get(LoginActivityVM::class.java)

        initListener()
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