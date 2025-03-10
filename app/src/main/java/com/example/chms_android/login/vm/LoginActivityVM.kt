package com.example.chms_android.login.vm

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.chms_android.MainActivity
import com.example.chms_android.common.Constants
import com.example.chms_android.repo.AccountRepo
import com.example.chms_android.vo.UserResponse

class LoginActivityVM: ViewModel() {
    fun login(email: String, password: String, context: Context) {
        if (Constants.IS_TESTING) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        } else {
            AccountRepo.login(email, password, context);
        }
    }

    fun register() {

    }

    fun sendCode() {

    }

    fun updatePassword() {

    }
}