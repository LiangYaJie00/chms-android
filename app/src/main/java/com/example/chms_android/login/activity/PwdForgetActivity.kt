package com.example.chms_android.login.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.chms_android.R
import com.example.chms_android.databinding.ActivityPwdForgetBinding
import com.example.chms_android.login.vm.PwdForgetActivityVM

class PwdForgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPwdForgetBinding
    private lateinit var viewModel: PwdForgetActivityVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPwdForgetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this).get(PwdForgetActivityVM::class.java)

        setListener()
    }

    private fun setListener() {
        binding.btnApfSendCode.setOnClickListener {
            val email = binding.edtApfEmail.text.toString()
            viewModel.getCode(email, this)
        }

        binding.btnApfResetPwd.setOnClickListener {
            val email = binding.edtApfEmail.text.toString()
            val password = binding.edtApfPassword.text.toString()
            val pwdConfirm = binding.edtApfPasswordConfirm.text.toString()
            val code = binding.edtApfCode.text.toString()
            viewModel.updatePwd(email, password, pwdConfirm, code, this)
        }
    }
}