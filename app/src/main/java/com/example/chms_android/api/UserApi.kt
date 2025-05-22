package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.common.Constants
import com.example.chms_android.data.User
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson

object UserApi {

    // 用户信息更新
    fun updateUser(user: User, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson = Gson()
        val jsonBody = gson.toJson(user)

        OkhttpUtil.postRequest("/user/updateUser", jsonBody, context, callback, needsToken = true, useJson = true)
    }
    
    /**
     * 获取所有角色为consumer的用户
     */
    fun getAllConsumerUsers(context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/user/consumers", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 获取所有角色为doctor的用户
     */
    fun getAllDoctorUsers(context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/user/doctors", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 根据角色和社区获取用户
     * @param role 角色名称（consumer, doctor, manager）
     * @param community 社区名称（可选）
     */
    fun getUsersByRoleAndCommunity(
        role: String, 
        community: String?, 
        context: Context, 
        callback: OkhttpUtil.NetworkCallback
    ) {
        val url = if (community != null) {
            "/user/search?role=$role&community=$community"
        } else {
            "/user/search?role=$role"
        }
        
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }

    /**
     * 根据userId获取用户信息
     * @param userId 用户ID
     * @param context 上下文
     * @param callback 网络回调
     */
    fun getUserById(userId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "/user/getUserById/$userId"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
}
