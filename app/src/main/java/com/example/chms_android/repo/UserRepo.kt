package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.api.UserApi
import com.example.chms_android.dao.UserDao
import com.example.chms_android.data.Role
import com.example.chms_android.data.User
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
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

    /**
     * 批量保存用户数据到本地数据库
     * 
     * @param users 用户列表
     * @param onSuccess 成功回调，返回用户列表
     * @param onError 错误回调，返回错误信息
     */
    private fun saveUsersToLocalDb(
        users: List<User>,
        onSuccess: ((List<User>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 使用批量插入方法一次性插入所有用户
                userDao.insertUsers(users)
                
                // 在主线程调用成功回调
                withContext(Dispatchers.Main) {
                    onSuccess?.invoke(users)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = "保存用户数据失败: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    onError?.invoke(errorMsg)
                }
            }
        }
    }

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

    /**
     * 获取所有居民用户
     * 
     * @param context 上下文
     * @param onSuccess 成功回调，返回居民用户列表
     * @param onError 错误回调，返回错误信息
     */
    fun getAllConsumerUsers(
        context: Context,
        onSuccess: ((List<User>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        UserApi.getAllConsumerUsers(context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<List<User>>>() {},
                    onSuccess = { users ->
                        // 使用优化后的批量保存方法
                        saveUsersToLocalDb(
                            users = users,
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    errorToastMessage = "获取居民用户失败"
                )
            }

            override fun onFailure(e: IOException) {
                val errorMsg = "网络请求失败: ${e.message}"
                Log.e(TAG, errorMsg, e)
                onError?.invoke(errorMsg)
            }
        })
    }
    
    /**
     * 获取所有医生用户
     * 
     * @param context 上下文
     * @param onSuccess 成功回调，返回医生用户列表
     * @param onError 错误回调，返回错误信息
     */
    fun getAllDoctorUsers(
        context: Context,
        onSuccess: ((List<User>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        UserApi.getAllDoctorUsers(context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<List<User>>>() {},
                    onSuccess = { users ->
                        // 使用优化后的批量保存方法
                        saveUsersToLocalDb(
                            users = users,
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    errorToastMessage = "获取医生用户失败"
                )
            }

            override fun onFailure(e: IOException) {
                val errorMsg = "网络请求失败: ${e.message}"
                Log.e(TAG, errorMsg, e)
                onError?.invoke(errorMsg)
            }
        })
    }
    
    /**
     * 根据角色和社区获取用户
     * 
     * @param role 角色名称
     * @param community 社区名称（可选）
     * @param context 上下文
     * @param onSuccess 成功回调，返回用户列表
     * @param onError 错误回调，返回错误信息
     */
    fun getUsersByRoleAndCommunity(
        role: String,
        community: String?,
        context: Context,
        onSuccess: ((List<User>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        UserApi.getUsersByRoleAndCommunity(role, community, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<List<User>>>() {},
                    onSuccess = { users ->
                        // 使用优化后的批量保存方法
                        saveUsersToLocalDb(
                            users = users,
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    errorToastMessage = "获取用户失败"
                )
            }

            override fun onFailure(e: IOException) {
                val errorMsg = "网络请求失败: ${e.message}"
                Log.e(TAG, errorMsg, e)
                onError?.invoke(errorMsg)
            }
        })
    }
    
    /**
     * 从本地数据库获取所有居民用户
     * 
     * @param onSuccess 成功回调，返回居民用户列表
     * @param onError 错误回调，返回错误信息
     */
    fun getAllConsumerUsersFromDb(
        onSuccess: ((List<User>) -> Unit),
        onError: ((String) -> Unit)
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val users = userDao.getAllConsumerUsers()
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(users)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = "获取本地居民用户数据失败: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    onError.invoke(errorMsg)
                }
            }
        }
    }
    
    /**
     * 从本地数据库获取所有医生用户
     * 
     * @param onSuccess 成功回调，返回医生用户列表
     * @param onError 错误回调，返回错误信息
     */
    fun getAllDoctorUsersFromDb(
        onSuccess: ((List<User>) -> Unit),
        onError: ((String) -> Unit)
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val users = userDao.getAllDoctorUsers()
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(users)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = "获取本地医生用户数据失败: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    onError.invoke(errorMsg)
                }
            }
        }
    }

    /**
     * 从本地数据库获取指定角色和社区的用户
     * 
     * @param role 角色名称
     * @param community 社区名称
     * @param onSuccess 成功回调，返回用户列表
     * @param onError 错误回调，返回错误信息
     */
    fun getUsersByRoleAndCommunityFromDb(
        role: String,
        community: String,
        onSuccess: ((List<User>) -> Unit),
        onError: ((String) -> Unit)
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 将字符串角色转换为Role枚举
                val roleEnum = try {
                    Role.valueOf(role)
                } catch (e: IllegalArgumentException) {
                    withContext(Dispatchers.Main) {
                        onError("无效的角色参数: $role")
                    }
                    return@launch
                }
                
                val users = userDao.getUsersByRoleAndCommunity(roleEnum, community)
                withContext(Dispatchers.Main) {
                    onSuccess(users)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = "获取本地用户数据失败: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    onError(errorMsg)
                }
            }
        }
    }
}
