package com.example.chms_android.repo

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.chms_android.MainActivity
import com.example.chms_android.api.UserApi
import com.example.chms_android.dao.UserDao
import com.example.chms_android.data.User
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.example.chms_android.vo.UserResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.IOException

object UserRepo {
    private val userDao: UserDao get() = DatabaseProvider.getDatabase().userDao()

    fun updateUser(user: User, context: Context) {
        UserApi.updateUser(user, context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                try {
                    // Gson 解析返回的数据
                    val gson = Gson()
                    val resultType = object : TypeToken<RespResult<User>>() {}.type
                    val result: RespResult<User> = gson.fromJson(response, resultType)

                    if (result.code == "200") {
                        val user = result.data
                        // 更新用户信息到本地数据库，并在成功后执行后续操作
                        GlobalScope.launch(Dispatchers.IO) { // 使用 IO 线程
                            try {
                                userDao.updateUser(user)
                                AccountUtil(context).saveUser(user)
                                EventBus.getDefault().post(user)
                                // 使用主线程进行 UI 操作
                                withContext(Dispatchers.Main) {
                                    // 弹出登录成功的提示框
//                                    ToastUtil.show(context, "UpdateUser successful: 更新成功!", Toast.LENGTH_SHORT)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    // 显示错误信息
                                    ToastUtil.show(context, "Failed to update user: ${e.message}", Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    } else {
                        ToastUtil.show(context, "UpdateUse failed: ${result.message}", Toast.LENGTH_SHORT)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtil.show(context, "Failed to parse response", Toast.LENGTH_SHORT)
                }
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                // 弹出接口请求失败的提示框
                ToastUtil.show(context, "Network error: ${e.message}", Toast.LENGTH_SHORT)
            }

        })
    }

}