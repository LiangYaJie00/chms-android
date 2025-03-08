package com.example.chms_android.login.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.chms_android.repo.AccountRepo
import com.example.chms_android.utils.ToastUtil

class PwdForgetActivityVM: ViewModel() {

    fun updatePwd(email: String, pwd: String, pwdConfirm: String, code: String, context: Context) {
        if (email.isNullOrEmpty()) {
            ToastUtil.show(context, "邮箱不能为空")
        } else if (pwd.isNullOrEmpty()) {
            ToastUtil.show(context, "密码不能为空")
        } else if (code.isNullOrEmpty()) {
            ToastUtil.show(context, "验证码不能为空")
        } else {
            if (pwd.equals(pwdConfirm)) {
                AccountRepo.updatePassword(email, pwd, code, context)
            } else {
                ToastUtil.show(context, "两次密码不相同，请重新输入")
            }
        }
    }

    fun getCode(email: String, context: Context) {
        if (email.isNullOrEmpty()) {
            ToastUtil.show(context, "邮箱不能为空")
            return
        }
        AccountRepo.sendCode(email, context)
    }
}