package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.api.DoctorApi
import com.example.chms_android.dao.DoctorDao
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

object DoctorRepo {
    private val TAG = "DoctorRepo"
    private val doctorDao: DoctorDao get() = DatabaseProvider.getDatabase().doctorDao()

    fun getAllNetDoctors(context: Context) {
        DoctorApi.getAllDoctors(context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                try {
                    // Gson 解析返回的数据
                    val gson = Gson()
                    val resultType = object : TypeToken<RespResult<List<Doctor>>>() {}.type
                    val result: RespResult<List<Doctor>> = gson.fromJson(response, resultType)

                    if (result.code == "200") {
                        val doctorList = result.data
                        // 更新用户信息到本地数据库，并在成功后执行后续操作
                        GlobalScope.launch(Dispatchers.IO) { // 使用 IO 线程
                            try {
                                doctorDao.insertDoctors(doctorList)

                                // 使用主线程进行 UI 操作
                                withContext(Dispatchers.Main) {
                                    // 弹出登录成功的提示框
                                    ToastUtil.show(context, "getAllNetDoctors successful: 更新成功!", Toast.LENGTH_SHORT)
                                    Log.i(TAG, "getAllNetDoctors successful: 更新成功!")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    // 显示错误信息
                                    ToastUtil.show(context, "Failed to getAllNetDoctors user: ${e.message}", Toast.LENGTH_SHORT)
                                    Log.e(TAG, "Failed to getAllNetDoctors user: ${e.message}")
                                }
                            }
                        }
                    } else {
                        ToastUtil.show(context, "getAllNetDoctors failed: ${result.message}", Toast.LENGTH_SHORT)
                        Log.e(TAG, "getAllNetDoctors failed: ${result.message}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtil.show(context, "Failed to parse response", Toast.LENGTH_SHORT)
                    Log.e(TAG, "Failed to parse response")
                }
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                // 弹出接口请求失败的提示框
                ToastUtil.show(context, "Network error: ${e.message}", Toast.LENGTH_SHORT)
            }
        })
    }

    // 新增一个方法从数据库获取所有医生
    fun getAllDoctorsFromDb(onComplete: (List<Doctor>) -> Unit, onError: (Throwable) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val doctors = doctorDao.getAllDoctors()
                withContext(Dispatchers.Main) {
                    onComplete(doctors)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    onError(exception)
                }
            }
        }
    }

    // 新增方法来获取前7个医生
    fun getFirstSevenDoctorsFromDb(onComplete: (List<Doctor>) -> Unit, onError: (Throwable) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val doctors = doctorDao.getFirstSevenDoctors()
                withContext(Dispatchers.Main) {
                    onComplete(doctors)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    onError(exception)
                }
            }
        }
    }
}