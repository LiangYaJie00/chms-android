package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import com.example.chms_android.api.AppointmentApi
import com.example.chms_android.dao.AppointmentDao
import com.example.chms_android.data.Appointment
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.dto.AppointmentDTO
import com.example.chms_android.dto.AppointmentRequestDTO
import com.example.chms_android.dto.AppointmentResponseDTO
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
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

object AppointmentRepo {
    private const val TAG = "AppointmentRepo"
    
    private val appointmentDao: AppointmentDao by lazy {
        DatabaseProvider.getDatabase().appointmentDao()
    }
    
    /**
     * 创建预约
     */
    fun createAppointment(
        requestDTO: AppointmentRequestDTO,
        context: Context,
        onSuccess: ((AppointmentDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        AppointmentApi.createAppointment(requestDTO, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<AppointmentDTO>>() {},
                    onSuccess = { appointmentDTO ->
                        // 保存到本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val entity = appointmentDTO.toEntity()
                                appointmentDao.insertAppointment(entity)
                            } catch (e: Exception) {
                                Log.e(TAG, "保存预约到本地失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(appointmentDTO)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    successToastMessage = "创建预约成功",
                    errorToastMessage = "创建预约失败"
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }
    
    /**
     * 更新预约状态
     */
    fun updateAppointmentStatus(
        responseDTO: AppointmentResponseDTO,
        context: Context,
        onSuccess: ((AppointmentDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        AppointmentApi.updateAppointmentStatus(responseDTO, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<AppointmentDTO>>() {},
                    onSuccess = { appointmentDTO ->
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val entity = appointmentDTO.toEntity()
                                appointmentDao.updateAppointment(entity)
                            } catch (e: Exception) {
                                Log.e(TAG, "更新预约状态到本地失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(appointmentDTO)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    successToastMessage = "更新预约状态成功",
                    errorToastMessage = "更新预约状态失败"
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }
    
    /**
     * 根据ID查询预约
     */
    fun getAppointmentById(
        appointmentId: Int,
        context: Context,
        onSuccess: ((AppointmentDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 先尝试从本地数据库获取
        GlobalScope.launch(Dispatchers.Main) {
            val localAppointment = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentById(appointmentId)
            }
            
            // 无论是否有本地数据，都从网络获取最新数据
            AppointmentApi.getAppointmentById(appointmentId, context, object : OkhttpUtil.NetworkCallback {
                override fun onSuccess(response: String) {
                    ResponseHandler.processApiResponse(
                        response,
                        context,
                        object : TypeToken<RespResult<AppointmentDTO>>() {},
                        onSuccess = { appointmentDTO ->
                            // 更新本地数据库
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val entity = appointmentDTO.toEntity()
                                    appointmentDao.insertAppointment(entity)
                                } catch (e: Exception) {
                                    Log.e(TAG, "保存预约到本地失败: ${e.message}", e)
                                }
                            }
                            
                            onSuccess?.invoke(appointmentDTO)
                        },
                        onError = { errorMsg ->
                            onError?.invoke(errorMsg)
                        }
                    )
                }
                
                override fun onFailure(e: IOException) {
                    if (localAppointment == null) {
                        // 只有在没有本地数据的情况下才显示错误
                        NetworkUtil.handleNetworkError(TAG, context, e)
                        onError?.invoke("网络请求失败: ${e.message}")
                    } else {
                        // 如果有本地数据，转换为DTO并返回
                        onSuccess?.invoke(AppointmentDTO.fromEntity(localAppointment))
                    }
                }
            })
        }
    }
    
    /**
     * 查询患者的所有预约
     */
    fun getAppointmentsByPatientId(
        patientId: Int,
        context: Context,
        onSuccess: ((List<AppointmentDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        AppointmentApi.getAppointmentsByPatientId(patientId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<AppointmentDTO>>>() {},
                    onSuccess = { appointmentDTOs ->
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val entities = appointmentDTOs.map { it.toEntity() }
                                appointmentDao.insertAppointments(entities)
                            } catch (e: Exception) {
                                Log.e(TAG, "保存预约列表到本地失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(appointmentDTOs)
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
                        val localAppointments = withContext(Dispatchers.IO) {
                            appointmentDao.getAppointmentsByPatientId(patientId)
                        }
                        
                        if (localAppointments.isNotEmpty()) {
                            // 如果本地有数据，转换为DTO并返回
                            val dtos = localAppointments.map { AppointmentDTO.fromEntity(it) }
                            onSuccess?.invoke(dtos)
                            return@launch
                        }
                        
                        // 如果本地没有数据，显示错误
                        NetworkUtil.handleNetworkError(TAG, context, e)
                        onError?.invoke("网络请求失败: ${e.message}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "获取本地预约失败: ${ex.message}", ex)
                        onError?.invoke("获取预约失败: ${ex.message}")
                    }
                }
            }
        })
    }
    
    /**
     * 查询医生的所有预约
     */
    fun getAppointmentsByDoctorId(
        doctorId: Int,
        context: Context,
        onSuccess: ((List<AppointmentDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        AppointmentApi.getAppointmentsByDoctorId(doctorId, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<AppointmentDTO>>>() {},
                    onSuccess = { appointmentDTOs ->
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val entities = appointmentDTOs.map { it.toEntity() }
                                appointmentDao.insertAppointments(entities)
                            } catch (e: Exception) {
                                Log.e(TAG, "保存预约列表到本地失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(appointmentDTOs)
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
                        val localAppointments = withContext(Dispatchers.IO) {
                            appointmentDao.getAppointmentsByDoctorId(doctorId)
                        }
                        
                        if (localAppointments.isNotEmpty()) {
                            // 如果本地有数据，转换为DTO并返回
                            val dtos = localAppointments.map { AppointmentDTO.fromEntity(it) }
                            onSuccess?.invoke(dtos)
                            return@launch
                        }
                        
                        // 如果本地没有数据，显示错误
                        NetworkUtil.handleNetworkError(TAG, context, e)
                        onError?.invoke("网络请求失败: ${e.message}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "获取本地预约失败: ${ex.message}", ex)
                        onError?.invoke("获取预约失败: ${ex.message}")
                    }
                }
            }
        })
    }
    
    /**
     * 查询特定日期的预约
     */
    fun getAppointmentsByDate(
        doctorId: Int,
        date: String,
        context: Context,
        onSuccess: ((List<AppointmentDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        AppointmentApi.getAppointmentsByDate(doctorId, date, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<List<AppointmentDTO>>>() {},
                    onSuccess = { appointmentDTOs ->
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val entities = appointmentDTOs.map { it.toEntity() }
                                appointmentDao.insertAppointments(entities)
                            } catch (e: Exception) {
                                Log.e(TAG, "保存预约列表到本地失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(appointmentDTOs)
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
                            val localAppointments = withContext(Dispatchers.IO) {
                                appointmentDao.getAppointmentsByDateAndDoctorId(dateObj, doctorId)
                            }
                            
                            if (localAppointments.isNotEmpty()) {
                                // 如果本地有数据，转换为DTO并返回
                                val dtos = localAppointments.map { AppointmentDTO.fromEntity(it) }
                                onSuccess?.invoke(dtos)
                                return@launch
                            }
                        }
                        
                        // 如果本地没有数据，显示错误
                        NetworkUtil.handleNetworkError(TAG, context, e)
                        onError?.invoke("网络请求失败: ${e.message}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "获取本地预约失败: ${ex.message}", ex)
                        onError?.invoke("获取预约失败: ${ex.message}")
                    }
                }
            }
        })
    }
    
    /**
     * 取消预约
     */
    fun cancelAppointment(
        appointmentId: Int,
        notes: String?,
        context: Context,
        onSuccess: ((Boolean) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        AppointmentApi.cancelAppointment(appointmentId, notes, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<Boolean>>() {},
                    onSuccess = { result ->
                        // 更新本地数据库中的预约状态
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                appointmentDao.updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED)
                                
                                if (notes != null) {
                                    appointmentDao.updateAppointmentNotes(appointmentId, notes)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "更新本地预约状态失败: ${e.message}", e)
                            }
                        }
                        
                        onSuccess?.invoke(result)
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    successToastMessage = "取消预约成功",
                    errorToastMessage = "取消预约失败"
                )
            }
            
            override fun onFailure(e: IOException) {
                NetworkUtil.handleNetworkError(TAG, context, e)
                onError?.invoke("网络请求失败: ${e.message}")
            }
        })
    }

    /**
     * 将预约标记为已完成
     */
    fun completeAppointment(
        appointmentId: Int,
        notes: String?,
        context: Context,
        onSuccess: ((AppointmentDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val responseDTO = AppointmentResponseDTO(
            appointmentId = appointmentId,
            status = AppointmentStatus.COMPLETED,
            notes = notes
        )
        
        updateAppointmentStatus(
            responseDTO = responseDTO,
            context = context,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * 查询指定医生和患者之间的所有预约
     */
    fun getAppointmentsByDoctorAndPatient(
        doctorId: Int,
        patientId: Int,
        context: Context,
        onSuccess: ((List<AppointmentDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        AppointmentApi.getAppointmentsByDoctorAndPatient(
            doctorId, 
            patientId, 
            context, 
            object : OkhttpUtil.NetworkCallback {
                override fun onSuccess(response: String) {
                    ResponseHandler.processApiResponse(
                        response,
                        context,
                        object : TypeToken<RespResult<List<AppointmentDTO>>>() {},
                        onSuccess = { appointmentDTOs ->
                            // 更新本地数据库
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val entities = appointmentDTOs.map { it.toEntity() }
                                    appointmentDao.insertAppointments(entities)
                                } catch (e: Exception) {
                                    Log.e(TAG, "保存预约列表到本地失败: ${e.message}", e)
                                }
                            }
                            
                            onSuccess?.invoke(appointmentDTOs)
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
                            val localAppointments = withContext(Dispatchers.IO) {
                                appointmentDao.getAppointmentsByDoctorAndPatientId(doctorId, patientId)
                            }
                            
                            if (localAppointments.isNotEmpty()) {
                                // 如果本地有数据，转换为DTO并返回
                                val dtos = localAppointments.map { AppointmentDTO.fromEntity(it) }
                                onSuccess?.invoke(dtos)
                                return@launch
                            }
                            
                            // 如果本地没有数据，显示错误
                            NetworkUtil.handleNetworkError(TAG, context, e)
                            onError?.invoke("网络请求失败: ${e.message}")
                        } catch (ex: Exception) {
                            Log.e(TAG, "获取本地预约失败: ${ex.message}", ex)
                            onError?.invoke("获取预约失败: ${ex.message}")
                        }
                    }
                }
            }
        )
    }
}
