package com.example.chms_android.login.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chms_android.data.Role
import com.example.chms_android.repo.AccountRepo
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RegisterRequest

class RegisterDialogVM: ViewModel() {
    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> get() = _currentPage
    private var vmEmail: String? = null
    private var vmPwd: String? = null
    private var vmCode: String? = null

    fun goToPreviousPage() {
        _currentPage.value = 0
    }

    // 跳转到下一步页面（RegStepTwoFragment）
    fun goToNextPage(email: String, pwd: String, pwdConfirm: String, code: String, context: Context) {
        if (email.isNullOrEmpty()) {
            ToastUtil.show(context, "邮箱不能为空")
        } else if (pwd.isNullOrEmpty()) {
            ToastUtil.show(context, "密码不能为空")
        } else if (code.isNullOrEmpty()) {
            ToastUtil.show(context, "验证码不能为空")
        } else {
            if (pwd.equals(pwdConfirm)) {
                vmEmail = email
                vmPwd = pwd
                vmCode = code
                _currentPage.value = 1
            } else {
                ToastUtil.show(context, "两次密码不相同，请重新输入")
            }
        }
    }

    fun getCode(email: String, context: Context): Boolean {
        if (email.isNullOrEmpty()) {
            ToastUtil.show(context, "邮箱不能为空")
            return false
        }
        AccountRepo.sendCode(email, context)
        return true
    }

    fun register(name: String, phoneStr: String, role: Role, context: Context) {
        val phone: Long = phoneStr.toLong()
        val registerRequest = RegisterRequest(
            username = name,
            email = vmEmail!!,
            password = vmPwd!!,
            phone = phone,
            code = vmCode!!,
            role = role
        )
        AccountRepo.register(registerRequest, context)
    }
}