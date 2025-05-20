package com.example.chms_android.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.chms_android.data.User
import com.example.chms_android.login.activity.LoginActivity
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

    /**
     * 退出登录，清除用户信息和ID，并跳转到登录页面
     */
    fun logout(context: Context) {
        // 清除用户数据
        clearUser()
        clearUserId()

        // 清除token
        TokenUtil.clearToken(context)

        // 清除JPush别名和标签
        JPushHelper.deleteAlias(context)
        JPushHelper.cleanTags(context)

        // 清除角标
        JPushHelper.clearBadge(context)
        
        // 跳转到登录页面
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
        
        // 如果当前上下文是Activity，则结束它
        if (context is Activity) {
            context.finish()
        }
    }
}