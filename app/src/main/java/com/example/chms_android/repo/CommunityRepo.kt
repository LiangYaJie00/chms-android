package com.example.chms_android.repo

import android.content.Context
import android.widget.Toast
import com.example.chms_android.api.CommunityApi
import com.example.chms_android.dao.CommunityDao
import com.example.chms_android.data.Community
import com.example.chms_android.data.Doctor
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

object CommunityRepo {
    private val communityDao: CommunityDao get() = DatabaseProvider.getDatabase().communityDao()

    fun getAllNetCommunity(context: Context) {
        CommunityApi.getAllCommunity(context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                try {
                    // Gson 解析返回的数据
                    val gson = Gson()
                    val resultType = object : TypeToken<RespResult<List<Community>>>() {}.type
                    val result: RespResult<List<Community>> = gson.fromJson(response, resultType)

                    if (result.code == "200") {
                        val communityList = result.data
                        // 更新用户信息到本地数据库，并在成功后执行后续操作
                        GlobalScope.launch(Dispatchers.IO) { // 使用 IO 线程
                            try {
                                communityDao.insertCommunities(communityList)

                                // 使用主线程进行 UI 操作
                                withContext(Dispatchers.Main) {
                                    // 弹出登录成功的提示框
                                    ToastUtil.show(context, "getAllCommunity successful: 更新成功!", Toast.LENGTH_SHORT)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    // 显示错误信息
                                    ToastUtil.show(context, "Failed to getAllCommunity user: ${e.message}", Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    } else {
                        ToastUtil.show(context, "getAllNetDoctors failed: ${result.message}", Toast.LENGTH_SHORT)
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

    fun getAllCommunityFromDb(onComplete: (List<Community>) -> Unit, onError: (Throwable) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val communities = communityDao.getAllCommunity()
                withContext(Dispatchers.Main) {
                    onComplete(communities)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    onError(exception)
                }
            }
        }
    }
}