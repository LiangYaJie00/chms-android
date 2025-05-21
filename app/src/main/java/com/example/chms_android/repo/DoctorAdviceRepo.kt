package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import com.example.chms_android.api.DoctorAdviceApi
import com.example.chms_android.common.Constants
import com.example.chms_android.dao.DoctorAdviceDao
import com.example.chms_android.data.DoctorAdvice
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.dto.DoctorAdviceDTO
import com.example.chms_android.utils.GsonUtil
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.vo.RespResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.lang.reflect.Type
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale
import java.util.TimeZone

object DoctorAdviceRepo {
    private const val TAG = "DoctorAdviceRepo"
    
    private val doctorAdviceDao: DoctorAdviceDao by lazy {
        DatabaseProvider.getDatabase().doctorAdviceDao()
    }
    
    /**
     * 根据ID获取医生建议
     */
    fun getAdviceById(
        adviceId: Int,
        context: Context,
        onSuccess: ((DoctorAdviceDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAdviceApi.getAdviceById(adviceId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<DoctorAdviceDTO>>() {},
                    onSuccess = { adviceDTO ->
                        // 保存到本地数据库
                        val entity = adviceDTO.toEntity()
                        doctorAdviceDao.insertDoctorAdvice(entity)
                        
                        // 回调
                        onSuccess?.invoke(adviceDTO)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    }
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                
                // 尝试从本地数据库获取
                try {
                    val localAdvice = doctorAdviceDao.getDoctorAdviceById(adviceId)
                    if (localAdvice != null) {
                        Log.i(TAG, "Using cached doctor advice data")
                        onSuccess?.invoke(localAdvice.toDTO())
                    } else {
                        onError?.invoke("网络请求失败，本地无缓存数据")
                    }
                } catch (ex: Exception) {
                    onError?.invoke("获取医生建议失败: ${ex.message}")
                }
            }
        })
    }
    
    /**
     * 根据医生ID获取建议列表
     */
    fun getAdvicesByDoctorId(
        doctorId: Int,
        context: Context,
        onSuccess: ((List<DoctorAdviceDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAdviceApi.getAdvicesByDoctorId(doctorId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<DoctorAdviceDTO>>>() {},
                    onSuccess = { adviceDTOs ->
                        // 保存到本地数据库
                        val entities = adviceDTOs.map { it.toEntity() }
                        doctorAdviceDao.insertDoctorAdvices(entities)
                        
                        // 回调
                        onSuccess?.invoke(adviceDTOs)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    }
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                
                // 尝试从本地数据库获取
                try {
                    val localAdvices = doctorAdviceDao.getDoctorAdvicesByDoctorId(doctorId)
                    if (localAdvices.isNotEmpty()) {
                        Log.i(TAG, "Using cached doctor advice data")
                        onSuccess?.invoke(localAdvices.map { it.toDTO() })
                    } else {
                        onError?.invoke("网络请求失败，本地无缓存数据")
                    }
                } catch (ex: Exception) {
                    onError?.invoke("获取医生建议失败: ${ex.message}")
                }
            }
        })
    }
    
    /**
     * 根据居民ID获取建议列表
     */
    fun getAdvicesByResidentId(
        residentId: Int,
        context: Context,
        onSuccess: ((List<DoctorAdviceDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAdviceApi.getAdvicesByResidentId(residentId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<DoctorAdviceDTO>>>() {},
                    onSuccess = { adviceDTOs ->
                        // 保存到本地数据库
                        val entities = adviceDTOs.map { it.toEntity() }
                        doctorAdviceDao.insertDoctorAdvices(entities)
                        
                        // 回调
                        onSuccess?.invoke(adviceDTOs)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    }
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                
                // 尝试从本地数据库获取
                try {
                    val localAdvices = doctorAdviceDao.getDoctorAdvicesByResidentId(residentId)
                    if (localAdvices.isNotEmpty()) {
                        Log.i(TAG, "Using cached doctor advice data")
                        onSuccess?.invoke(localAdvices.map { it.toDTO() })
                    } else {
                        onError?.invoke("网络请求失败，本地无缓存数据")
                    }
                } catch (ex: Exception) {
                    onError?.invoke("获取医生建议失败: ${ex.message}")
                }
            }
        })
    }
    
    /**
     * 新增或更新医生建议
     */
    fun addOrUpdateAdvice(
        adviceDTO: DoctorAdviceDTO,
        context: Context,
        onSuccess: ((DoctorAdviceDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAdviceApi.addOrUpdateAdvice(adviceDTO, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<DoctorAdviceDTO>>() {},
                    onSuccess = { resultDTO ->
                        // 保存到本地数据库
                        val entity = resultDTO.toEntity()
                        doctorAdviceDao.insertDoctorAdvice(entity)
                        
                        // 回调
                        onSuccess?.invoke(resultDTO)
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
     * 更新医生建议状态
     */
    fun updateAdviceStatus(
        adviceId: Int,
        status: Int,
        context: Context,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAdviceApi.updateAdviceStatus(adviceId, status, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<String>>() {},
                    onSuccess = { _ ->
                        // 更新本地数据库
                        try {
                            val advice = doctorAdviceDao.getDoctorAdviceById(adviceId)
                            if (advice != null) {
                                advice.status = status
                                doctorAdviceDao.insertDoctorAdvice(advice)
                            }
                        } catch (ex: Exception) {
                            Log.e(TAG, "更新本地数据库失败: ${ex.message}", ex)
                        }
                        
                        // 回调
                        onSuccess?.invoke()
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
     * 分页查询医生建议
     */
    fun getAdvicesByPage(
        pageNum: Int,
        pageSize: Int,
        context: Context,
        onSuccess: ((List<DoctorAdviceDTO>, Int) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAdviceApi.getAdvicesByPage(pageNum, pageSize, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<Map<String, Any>>>() {},
                    onSuccess = { result ->
                        try {
                            // 使用GsonUtil创建Gson实例
                            val gson = GsonUtil.createGson()
                            
                            val listJson = gson.toJson(result["list"])
                            val adviceDTOs = gson.fromJson<List<DoctorAdviceDTO>>(
                                listJson,
                                object : TypeToken<List<DoctorAdviceDTO>>() {}.type
                            )
                            
                            val total = (result["total"] as? Double)?.toInt() ?: 0
                            
                            // 保存到本地数据库
                            val entities = adviceDTOs.map { it.toEntity() }
                            doctorAdviceDao.insertDoctorAdvices(entities)
                            
                            // 回调
                            onSuccess?.invoke(adviceDTOs, total)
                        } catch (ex: Exception) {
                            Log.e(TAG, "解析分页数据失败: ${ex.message}", ex)
                            onError?.invoke("解析分页数据失败: ${ex.message}")
                        }
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
     * 条件查询医生建议
     */
    fun searchAdvices(
        doctorId: Int? = null,
        residentId: Int? = null,
        adviceType: Int? = null,
        status: Int? = null,
        isImportant: Int? = null,
        pageNum: Int = 1,
        pageSize: Int = 10,
        context: Context,
        onSuccess: ((List<DoctorAdviceDTO>, Int) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorAdviceApi.searchAdvices(
            doctorId, residentId, adviceType, status, isImportant, pageNum, pageSize,
            context, object : OkhttpUtil.NetworkCallback {
                override fun onSuccess(response: String) {
                    ResponseHandler.processApiResponse(
                        response,
                        context,
                        object : TypeToken<RespResult<Map<String, Any>>>() {},
                        onSuccess = { result ->
                            try {
                                // 使用GsonUtil创建Gson实例
                                val gson = GsonUtil.createGson()
                                
                                val listJson = gson.toJson(result["list"])
                                val adviceDTOs = gson.fromJson<List<DoctorAdviceDTO>>(
                                    listJson,
                                    object : TypeToken<List<DoctorAdviceDTO>>() {}.type
                                )
                                
                                val total = (result["total"] as? Double)?.toInt() ?: 0
                                
                                // 保存到本地数据库
                                val entities = adviceDTOs.map { it.toEntity() }
                                doctorAdviceDao.insertDoctorAdvices(entities)
                                
                                // 回调
                                onSuccess?.invoke(adviceDTOs, total)
                            } catch (ex: Exception) {
                                Log.e(TAG, "解析搜索数据失败: ${ex.message}", ex)
                                onError?.invoke("解析搜索数据失败: ${ex.message}")
                            }
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
            }
        )
    }
    
    /**
     * 获取居民收到的未读建议
     */
    fun getUnreadAdvicesByResidentId(
        residentId: Int,
        context: Context,
        onSuccess: ((List<DoctorAdviceDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 先尝试从本地数据库获取
        try {
            val localAdvices = doctorAdviceDao.getUnreadDoctorAdvicesByResidentId(residentId)
            if (localAdvices.isNotEmpty()) {
                Log.i(TAG, "Using cached unread doctor advice data")
                onSuccess?.invoke(localAdvices.map { it.toDTO() })
                return
            }
        } catch (ex: Exception) {
            Log.e(TAG, "从本地获取未读建议失败: ${ex.message}", ex)
        }
        
        // 如果本地没有数据，则从网络获取所有建议，然后过滤未读的
        searchAdvices(
            residentId = residentId,
            status = 0, // 未读状态
            context = context,
            onSuccess = { advices, _ ->
                onSuccess?.invoke(advices)
            },
            onError = onError
        )
    }
    
    /**
     * 获取居民收到的重要建议
     */
    fun getImportantAdvicesByResidentId(
        residentId: Int,
        context: Context,
        onSuccess: ((List<DoctorAdviceDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 先尝试从本地数据库获取
        try {
            val localAdvices = doctorAdviceDao.getImportantDoctorAdvicesByResidentId(residentId)
            if (localAdvices.isNotEmpty()) {
                Log.i(TAG, "Using cached important doctor advice data")
                onSuccess?.invoke(localAdvices.map { it.toDTO() })
                return
            }
        } catch (ex: Exception) {
            Log.e(TAG, "从本地获取重要建议失败: ${ex.message}", ex)
        }
        
        // 如果本地没有数据，则从网络获取所有建议，然后过滤重要的
        searchAdvices(
            residentId = residentId,
            isImportant = 1, // 重要标记
            context = context,
            onSuccess = { advices, _ ->
                onSuccess?.invoke(advices)
            },
            onError = onError
        )
    }

    /**
     * 获取居民收到的最近几条建议
     */
    fun getRecentAdvicesByResidentId(
        residentId: Int,
        limit: Int = 3,
        context: Context,
        onSuccess: ((List<DoctorAdviceDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 使用searchAdvices方法，设置pageSize为limit
        searchAdvices(
            residentId = residentId,
            pageNum = 1,
            pageSize = limit,
            context = context,
            onSuccess = { advices, _ ->
                // 只返回建议列表，忽略总数
                onSuccess?.invoke(advices)
            },
            onError = onError
        )
    }
}
