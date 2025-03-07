package com.example.chms_android.login.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.chms_android.repo.AccountRepo
import com.example.chms_android.vo.UserResponse

class LoginActivityVM: ViewModel() {
    fun login(email: String, password: String, context: Context) {
        AccountRepo.login(email, password, context);
    }

    fun register() {

    }

    fun sendCode() {

    }

    fun updatePassword() {

    }
}