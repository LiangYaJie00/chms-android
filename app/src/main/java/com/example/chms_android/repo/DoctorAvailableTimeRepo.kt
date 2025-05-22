package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import com.example.chms_android.api.DoctorAvailableTimeApi
import com.example.chms_android.dao.DoctorAvailableTimeDao
import com.example.chms_android.data.DoctorAvailableTime
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.dto.DoctorAvailableTimeDTO
import com.example.chms_android.dto.DoctorAvailableTimeRequestDTO
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DoctorAvailableTimeRepo {
    private const val TAG = "DoctorAvailableTimeRepo"
    
    private val doctorAvailableTimeDao: DoctorAvailableTimeDao by lazy {
        DatabaseProvider.getDatabase().doctorAvailableTimeDao()
    }
    
    /**
     * 添加医生可用时间段
     */
    fun addAvailableTimes(
        requestDTO: DoctorAvailableTimeRequestDTO,
        context: Context,
        onSuccess: ((Boolean) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAvailableTimeApi.addAvailableTimes(requestDTO, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<Boolean>>() {},
                    onSuccess = { result ->
                        onSuccess?.invoke(result)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    successToastMessage = "添加可用时间段成功",
                    errorToastMessage = "添加可用时间段失败"
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }
    
    /**
     * 删除医生可用时间段
     */
    fun deleteAvailableTime(
        timeId: Int,
        context: Context,
        onSuccess: ((Boolean) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAvailableTimeApi.deleteAvailableTime(timeId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<Boolean>>() {},
                    onSuccess = { result ->
                        // 从本地数据库删除对应记录
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                doctorAvailableTimeDao.updateBookingStatus(timeId, true) // 标记为已预约，相当于删除
                            } catch (e: Exception) {
                                Log.e(TAG, "删除本地时间段失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(result)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    successToastMessage = "删除时间段成功",
                    errorToastMessage = "删除时间段失败"
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }
    
    /**
     * 删除医生某天的所有可用时间段
     */
    fun deleteAvailableTimesByDate(
        doctorId: Int,
        date: String,
        context: Context,
        onSuccess: ((Boolean) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAvailableTimeApi.deleteAvailableTimesByDate(doctorId, date, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<Boolean>>() {},
                    onSuccess = { result ->
                        // 从本地数据库删除对应日期的记录
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val dateObj = dateFormat.parse(date)
                                
                                dateObj?.let {
                                    val availableTimes = doctorAvailableTimeDao.getAvailableTimesByDoctorIdAndDate(doctorId, it)
                                    for (time in availableTimes) {
                                        time.timeId?.let { id ->
                                            doctorAvailableTimeDao.updateBookingStatus(id, true) // 标记为已预约，相当于删除
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "删除本地日期时间段失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(result)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    successToastMessage = "删除日期时间段成功",
                    errorToastMessage = "删除日期时间段失败"
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }
    
    /**
     * 根据ID查询医生可用时间段
     */
    fun getAvailableTimeById(
        timeId: Int,
        context: Context,
        onSuccess: ((DoctorAvailableTimeDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 先尝试从本地数据库获取
        GlobalScope.launch(Dispatchers.Main) {
            val localTime = withContext(Dispatchers.IO) {
                doctorAvailableTimeDao.getAvailableTimeById(timeId)
            }
            
            // 无论是否有本地数据，都从网络获取最新数据
            DoctorAvailableTimeApi.getAvailableTimeById(timeId, context, object : OkhttpUtil.NetworkCallback {
                override fun onSuccess(response: String) {
                    ResponseHandler.processApiResponse(
                        response,
                        context,
                        object : TypeToken<RespResult<DoctorAvailableTimeDTO>>() {},
                        onSuccess = { availableTimeDTO ->
                            // 更新本地数据库
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    doctorAvailableTimeDao.insertAvailableTime(availableTimeDTO.toEntity())
                                } catch (e: Exception) {
                                    Log.e(TAG, "保存时间段到本地失败: ${e.message}", e)
                                }
                            }
                            
                            onSuccess?.invoke(availableTimeDTO)
                        },
                        onError = { errorMsg ->
                            onError?.invoke(errorMsg)
                        }
                    )
                }
                
                override fun onFailure(e: IOException) {
                    if (localTime == null) {
                        // 只有在没有本地数据的情况下才显示错误
                        NetworkUtil.handleNetworkError(TAG, context, e)
                        onError?.invoke("网络请求失败: ${e.message}")
                    } else {
                        // 如果有本地数据，转换为DTO并返回
                        // 这里需要实现从Entity到DTO的转换
                        // onSuccess?.invoke(convertToDTO(localTime))
                    }
                }
            })
        }
    }
    
    /**
     * 查询医生某天的所有可用时间段
     */
    fun getAvailableTimesByDate(
        doctorId: Int,
        date: String,
        context: Context,
        onSuccess: ((List<DoctorAvailableTimeDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAvailableTimeApi.getAvailableTimesByDate(doctorId, date, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<DoctorAvailableTimeDTO>>>() {},
                    onSuccess = { availableTimeDTOs ->
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val entities = availableTimeDTOs.map { it.toEntity() }
                                doctorAvailableTimeDao.insertAvailableTimes(entities)
                            } catch (e: Exception) {
                                Log.e(TAG, "保存时间段列表到本地失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(availableTimeDTOs)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    }
                )
            }
            
            override fun onFailure(e: IOException) {
                // 网络请求失败，尝试从本地获取数据
                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateObj = dateFormat.parse(date)
                        
                        if (dateObj != null) {
                            val localTimes = withContext(Dispatchers.IO) {
                                doctorAvailableTimeDao.getAvailableTimesByDoctorIdAndDate(doctorId, dateObj)
                            }
                            
                            if (localTimes.isNotEmpty()) {
                                // 如果本地有数据，转换为DTO并返回
                                // val dtos = localTimes.map { convertToDTO(it) }
                                // onSuccess?.invoke(dtos)
                                return@launch
                            }
                        }
                        
                        // 如果本地没有数据，显示错误
                        NetworkUtil.handleNetworkError(TAG, context, e)
                        onError?.invoke("网络请求失败: ${e.message}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "获取本地时间段失败: ${ex.message}", ex)
                        onError?.invoke("获取时间段失败: ${ex.message}")
                    }
                }
            }
        })
    }
    
    /**
     * 查询医生某天的所有未被预约的时间段
     */
    fun getAvailableTimesByDateNotBooked(
        doctorId: Int,
        date: String,
        context: Context,
        onSuccess: ((List<DoctorAvailableTimeDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAvailableTimeApi.getAvailableTimesByDateNotBooked(doctorId, date, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<DoctorAvailableTimeDTO>>>() {},
                    onSuccess = { availableTimeDTOs ->
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val entities = availableTimeDTOs.map { it.toEntity() }
                                doctorAvailableTimeDao.insertAvailableTimes(entities)
                            } catch (e: Exception) {
                                Log.e(TAG, "保存未预约时间段到本地失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(availableTimeDTOs)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    }
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }
    
    /**
     * 检查时间段是否可用
     */
    fun isTimeSlotAvailable(
        doctorId: Int,
        date: String,
        startTime: String,
        endTime: String,
        context: Context,
        onSuccess: ((Boolean) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAvailableTimeApi.isTimeSlotAvailable(doctorId, date, startTime, endTime, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<Boolean>>() {},
                    onSuccess = { isAvailable ->
                        onSuccess?.invoke(isAvailable)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    }
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }
    
    // 辅助方法：将Entity转换为DTO
    private fun convertToDTO(entity: DoctorAvailableTime): DoctorAvailableTimeDTO {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        return DoctorAvailableTimeDTO(
            timeId = entity.timeId,
            doctorId = entity.doctorId,
            availableDate = entity.availableDate?.let { dateFormat.format(it) },
            startTime = entity.startTime?.let { timeFormat.format(it) },
            endTime = entity.endTime?.let { timeFormat.format(it) },
            isBooked = entity.isBooked,
            createTime = entity.createTime,
            updateTime = entity.updateTime
        )
    }
}