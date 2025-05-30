package com.example.chms_android.repo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.chms_android.MainActivity
import com.example.chms_android.api.AccountApi
import com.example.chms_android.dao.UserDao
import com.example.chms_android.data.Role
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.features_doctor.DoctorMainActivity
import com.example.chms_android.login.activity.LoginActivity
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.JPushHelper
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.utils.TokenUtil
import com.example.chms_android.vo.OperationResponse
import com.example.chms_android.vo.RegisterRequest
import com.example.chms_android.vo.UserResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import com.example.chms_android.vo.RespResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AccountRepo {
    private val userDao: UserDao get() = DatabaseProvider.getDatabase().userDao()

    // 调用登录方法
    fun login(email: String, password: String, context: Context) {
        AccountApi.login(email, password, context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                try {
                    // Gson 解析返回的数据
                    val gson = Gson()
                    val resultType = object : TypeToken<RespResult<UserResponse>>() {}.type
                    val result: RespResult<UserResponse> = gson.fromJson(response, resultType)

                    if (result.code == "200") {
                        val userResponse = result.data
                        // 保存token
                        TokenUtil.saveToken(userResponse.token, context)
                        userResponse.user.userId?.let { AccountUtil(context).saveUserId(it.toLong()) }
                        AccountUtil(context).saveUser(userResponse.user)
                        
                        // 设置JPush用户信息
                        userResponse.user.userId?.let { 
                            JPushHelper.setupUserPushProfile(
                                context, 
                                it.toString(), 
                                userResponse.user.role.toString()
                            )
                        }
                        
                        // 如果用户是医生角色，获取并保存医生ID
                        if (userResponse.user.role == Role.doctor && userResponse.user.userId != null) {
                            fetchAndSaveDoctorId(userResponse.user.userId, context)
                        }
                        
                        // 保存用户信息到本地数据库，并在成功后执行后续操作
                        GlobalScope.launch(Dispatchers.IO) { // 使用 IO 线程
                            try {
                                val user = userDao.getUserByEmail(userResponse.user.email)
                                if (user != null) {
                                    userDao.updateUser(userResponse.user)
                                } else {
                                    userDao.insertUser(userResponse.user)
                                }
                                // 使用主线程进行 UI 操作
                                withContext(Dispatchers.Main) {
                                    // 根据用户角色跳转到不同的主页面
                                    val intent = when (userResponse.user.role) {
                                        Role.doctor -> Intent(context, DoctorMainActivity::class.java)
                                        else -> Intent(context, MainActivity::class.java) // 默认为consumer角色
                                    }
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    // 弹出登录成功的提示框
                                    ToastUtil.show(context, "Login successful: Welcome ${userResponse.user.name}!", Toast.LENGTH_SHORT)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    // 显示错误信息
                                    ToastUtil.show(context, "Failed to save/update user: ${e.message}", Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    } else {
                        // 弹出登录失败的提示框
                        ToastUtil.show(context, "Login failed: ${result.message}", Toast.LENGTH_SHORT)
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

    // 调用注册方法
    fun register(registerRequest: RegisterRequest, context: Context) {
        AccountApi.register(registerRequest, context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                try {
                    // Gson 解析返回的数据
                    val gson = Gson()
                    val resultType = object : TypeToken<RespResult<UserResponse>>() {}.type
                    val result: RespResult<UserResponse> = gson.fromJson(response, resultType)

                    if (result.code == "200") {
                        val userResponse = result.data
                        // 保存token
                        TokenUtil.saveToken(userResponse.token, context)
                        userResponse.user.userId?.let { AccountUtil(context).saveUserId(it.toLong()) }
                        AccountUtil(context).saveUser(userResponse.user)
                        
                        // 设置JPush用户信息
                        userResponse.user.userId?.let { 
                            JPushHelper.setupUserPushProfile(
                                context, 
                                it.toString(), 
                                userResponse.user.role.toString()
                            )
                        }
                        
                        // 如果用户是医生角色，获取并保存医生ID
                        if (userResponse.user.role == Role.doctor && userResponse.user.userId != null) {
                            fetchAndSaveDoctorId(userResponse.user.userId, context)
                        }
                        
                        // 保存用户信息到本地数据库，并在成功后执行后续操作
                        GlobalScope.launch(Dispatchers.IO) { // 使用 IO 线程
                            try {
                                userDao.insertUser(userResponse.user)

                                // 使用主线程进行 UI 操作
                                withContext(Dispatchers.Main) {
                                    // 根据用户角色跳转到不同的主页面
                                    val intent = when (userResponse.user.role) {
                                        Role.doctor -> Intent(context, DoctorMainActivity::class.java)
                                        else -> Intent(context, MainActivity::class.java) // 默认为consumer角色
                                    }
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    // 弹出登录成功的提示框
                                    ToastUtil.show(context, "Register successful: Welcome ${userResponse.user.name}!", Toast.LENGTH_SHORT)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    // 显示错误信息
                                    ToastUtil.show(context, "Failed to save user: ${e.message}", Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    } else {
                        // 弹出登录失败的提示框
                        ToastUtil.show(context, "Register failed: ${result.message}", Toast.LENGTH_SHORT)
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

    // 调用发送验证码方法
    fun sendCode(email: String, context: Context) {
        AccountApi.sendCode(email, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                try {
                    // Gson 解析返回的数据
                    val gson = Gson()
                    val resultType = object : TypeToken<RespResult<OperationResponse>>() {}.type
                    val result: RespResult<OperationResponse> = gson.fromJson(response, resultType)

                    if (result.code == "200") {
                        // 弹出登录成功的提示框
                        ToastUtil.show(context, "SendCode successful: ${result.message}!", Toast.LENGTH_SHORT)
                    } else {
                        // 弹出登录失败的提示框
                        ToastUtil.show(context, "SendCode failed: ${result.message}", Toast.LENGTH_SHORT)
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

    // 调用密码更新方法
    fun updatePassword(email: String, password: String, code: String, context: Context) {
        AccountApi.updatePassword(email, password, code, context,
            object : OkhttpUtil.NetworkCallback {
                override fun onSuccess(response: String) {
                    try {
                        // Gson 解析返回的数据
                        val gson = Gson()
                        val resultType = object : TypeToken<RespResult<OperationResponse>>() {}.type
                        val result: RespResult<OperationResponse> = gson.fromJson(response, resultType)

                        if (result.code == "200") {
                            // 弹出登录成功的提示框
                            ToastUtil.show(context, "UpdatePassword successful: ${result.message}!", Toast.LENGTH_SHORT)
                            // 回到登录界面
                            val activity = context as Activity
                            activity.finish()
                        } else {
                            // 弹出登录失败的提示框
                            ToastUtil.show(context, "UpdatePassword failed: ${result.message}", Toast.LENGTH_SHORT)
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

    // 获取并保存医生ID的方法
    private fun fetchAndSaveDoctorId(userId: Int, context: Context) {
        DoctorRepo.getDoctorByUserId(
            userId = userId,
            context = context,
            onSuccess = { doctorDTO ->
                // 保存医生ID到AccountUtil
                doctorDTO.doctorId?.let { doctorId ->
                    AccountUtil(context).saveDoctorId(doctorId)
                    Log.d("AccountRepo", "Doctor ID saved: $doctorId for user ID: $userId")
                }
            },
            onError = { errorMsg ->
                Log.e("AccountRepo", "Failed to get doctor by user ID: $errorMsg")
                // 如果获取失败，可能是新注册的医生还没有对应的医生记录
                // 这种情况下，医生需要先完善个人资料
            }
        )
    }

}