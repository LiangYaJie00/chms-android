package com.example.chms_android.utils

import android.content.Context
import com.example.chms_android.data.User
import com.google.gson.Gson

class AccountUtil(context: Context) {
    private val spUtil: SPUtil = SPUtil(context)
    private val gson = Gson()

    companion object {
        private const val USER_ID_KEY = "USER_ID_KEY"
        private const val USER_DATA_KEY = "USER_DATA_KEY"
        private const val DEFAULT_USER_ID = -1L // Assuming userId is of type Long
    }

    /**
     * 保存用户ID
     */
    fun saveUserId(userId: Long) {
        spUtil.put(USER_ID_KEY, userId)
    }

    /**
     * 获取保存的用户ID
     */
    fun getUserId(): Long {
        return spUtil.get(USER_ID_KEY, Long::class.java, DEFAULT_USER_ID)
    }

    /**
     * 检查用户是否已登录
     */
    fun isUserLoggedIn(): Boolean {
        return getUserId() != DEFAULT_USER_ID
    }

    /**
     * 清除用户ID
     */
    fun clearUserId() {
        spUtil.put(USER_ID_KEY, DEFAULT_USER_ID)
    }

    /**
     * 保存用户信息
     */
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        spUtil.put(USER_DATA_KEY, userJson)
    }

    /**
     * 获取保存的用户信息
     */
    fun getUser(): User? {
        val userJson = spUtil.get(USER_DATA_KEY, String::class.java, "")
        return if (userJson.isNotEmpty()) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null // 或者返回一个默认的 User 对象
        }
    }

    /**
     * 清除用户信息
     */
    fun clearUser() {
        spUtil.put(USER_DATA_KEY, "")
        clearUserId()
    }
}