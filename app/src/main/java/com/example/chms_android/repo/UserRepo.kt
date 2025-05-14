package com.example.chms_android.repo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.chms_android.MainActivity
import com.example.chms_android.api.UserApi
import com.example.chms_android.dao.UserDao
import com.example.chms_android.data.User
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
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
    private val TAG = "UserRepo"
    private val userDao: UserDao get() = DatabaseProvider.getDatabase().userDao()

    fun updateUser(
        user: User, 
        context: Context,
        onSuccess: ((User) -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        UserApi.updateUser(user, context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<User>>() {},
                    onSuccess = { updatedUser ->
                        // 更新用户信息到本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                userDao.updateUser(updatedUser)
                                AccountUtil(context).saveUser(updatedUser)
                                EventBus.getDefault().post(updatedUser)
                                
                                // 在主线程调用成功回调
                                withContext(Dispatchers.Main) {
                                    onSuccess?.invoke(updatedUser)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    val errorMsg = "Failed to save user data: ${e.message}"
                                    Log.e(TAG, "Error updating user in database: $errorMsg", e)
                                    onFailure?.invoke(errorMsg)
                                }
                            }
                        }
                    },
                    onError = { errorMsg ->
                        onFailure?.invoke(errorMsg)
                    },
                    successToastMessage = "用户信息更新成功",
                    errorToastMessage = "更新失败"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                // 弹出接口请求失败的提示框
                val errorMsg = "网络错误: ${e.message}"
                ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
                onFailure?.invoke(errorMsg)
            }
        })
    }

}