package com.example.chms_android.repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.api.DoctorApi
import com.example.chms_android.common.Constants
import com.example.chms_android.dao.DoctorDao
import com.example.chms_android.data.Doctor
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.dto.DoctorDTO
import com.example.chms_android.utils.NetworkUtil
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
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

    // 获取所有医生（从网络）
    fun getAllNetDoctors(context: Context) {
        DoctorApi.getAllDoctors(context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<List<DoctorDTO>>>() {},
                    onSuccess = { doctorDTOs ->
                        // 将DTO列表转换为实体类列表
                        val doctorList = doctorDTOs.map { it.toEntity() }
                        
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                doctorDao.insertDoctors(doctorList)
                                
                                withContext(Dispatchers.Main) {
                                    ToastUtil.show(context, "医生数据更新成功", Toast.LENGTH_SHORT)
                                    Log.i(TAG, "getAllNetDoctors successful: 更新成功!")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    ToastUtil.show(context, "Failed to update doctors: ${e.message}", Toast.LENGTH_SHORT)
                                    Log.e(TAG, "Failed to update doctors: ${e.message}")
                                }
                            }
                        }
                    },
                    onError = { message ->
                        ToastUtil.show(context, "获取医生数据失败: $message", Toast.LENGTH_SHORT)
                        Log.e(TAG, "getAllNetDoctors failed: $message")
                    }
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                NetworkUtil.handleNetworkError(TAG, context, e)
                
                if (Constants.IS_TESTING) {
                    Log.i(TAG, "Using cached data in testing mode")
                }
            }
        })
    }
    
    // 根据ID获取医生信息
    fun getDoctorById(
        doctorId: Int,
        context: Context,
        onSuccess: ((DoctorDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        fetchDoctorData(
            localDataFetch = { doctorDao.getDoctorById(doctorId) },
            networkFetch = { callback -> DoctorApi.getDoctorById(doctorId, context, callback) },
            context = context,
            onSuccess = onSuccess,
            onError = onError,
            errorLogTag = "getting doctor by ID"
        )
    }
    
    // 分页获取医生列表
    fun getDoctorsByPage(
        offset: Int,
        limit: Int,
        context: Context,
        onSuccess: ((List<DoctorDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorApi.getDoctorsByPage(offset, limit, context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<List<DoctorDTO>>>() {},
                    onSuccess = { doctorDTOs ->
                        val doctorList = doctorDTOs.map { it.toEntity() }
                        
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                doctorDao.insertDoctors(doctorList)
                                
                                withContext(Dispatchers.Main) {
                                    onSuccess?.invoke(doctorDTOs)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    onError?.invoke("Failed to save doctors: ${e.message}")
                                }
                            }
                        }
                    },
                    onError = { message ->
                        onError?.invoke(message)
                    }
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                onError?.invoke("Network error: ${e.message}")
            }
        })
    }
    
    // 根据用户ID获取医生信息
    fun getDoctorByUserId(
        userId: Int,
        context: Context,
        onSuccess: ((DoctorDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        fetchDoctorData(
            localDataFetch = { doctorDao.getDoctorByUserId(userId) },
            networkFetch = { callback -> DoctorApi.getDoctorByUserId(userId, context, callback) },
            context = context,
            onSuccess = onSuccess,
            onError = onError,
            errorLogTag = "getting doctor by user ID"
        )
    }
    
    // 新增或更新医生信息
    fun saveOrUpdateDoctor(
        doctorDTO: DoctorDTO,
        context: Context,
        onSuccess: ((DoctorDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorApi.saveOrUpdateDoctor(doctorDTO, context, object: OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<DoctorDTO>>() {},
                    onSuccess = { updatedDoctorDTO ->
                        val doctor = updatedDoctorDTO.toEntity()
                        
                        // 更新本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                doctorDao.insertDoctor(doctor)
                                
                                withContext(Dispatchers.Main) {
                                    onSuccess?.invoke(updatedDoctorDTO)
                                    ToastUtil.show(context, "医生履历信息保存成功", Toast.LENGTH_SHORT)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    onError?.invoke("Failed to save doctor: ${e.message}")
                                }
                            }
                        }
                    },
                    onError = { message ->
                        onError?.invoke(message)
                        ToastUtil.show(context, "保存医生履历信息失败: $message", Toast.LENGTH_SHORT)
                    }
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                onError?.invoke("Network error: ${e.message}")
                ToastUtil.show(context, "网络错误: ${e.message}", Toast.LENGTH_SHORT)
            }
        })
    }

    // 从数据库获取所有医生
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

    // 获取前7个医生
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

    // 通用的获取医生数据方法
    private fun fetchDoctorData(
        localDataFetch: () -> Doctor?,
        networkFetch: (OkhttpUtil.NetworkCallback) -> Unit,
        context: Context,
        onSuccess: ((DoctorDTO) -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
        errorLogTag: String
    ) {
        // 先从本地数据库获取数据
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val localDoctor = localDataFetch()
                
                // 如果本地有数据，先返回本地数据
                if (localDoctor != null) {
                    val localDoctorDTO = localDoctor.toDTO()
                    withContext(Dispatchers.Main) {
                        onSuccess?.invoke(localDoctorDTO)
                    }
                }
                
                // 然后请求网络获取最新数据
                withContext(Dispatchers.Main) {
                    networkFetch(object: OkhttpUtil.NetworkCallback {
                        override fun onSuccess(response: String) {
                            ResponseHandler.processApiResponse(
                                response = response,
                                context = context,
                                typeToken = object : TypeToken<RespResult<DoctorDTO>>() {},
                                onSuccess = { doctorDTO ->
                                    val doctor = doctorDTO.toEntity()
                                    
                                    // 更新本地数据库
                                    GlobalScope.launch(Dispatchers.IO) {
                                        try {
                                            doctorDao.insertDoctor(doctor)
                                            
                                            withContext(Dispatchers.Main) {
                                                onSuccess?.invoke(doctorDTO)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            // 如果更新数据库失败但已经返回了本地数据，不需要再次调用onError
                                            if (localDoctor == null) {
                                                withContext(Dispatchers.Main) {
                                                    onError?.invoke("Failed to save doctor: ${e.message}")
                                                }
                                            }
                                        }
                                    }
                                },
                                onError = { message ->
                                    // 如果网络请求失败但已经返回了本地数据，不需要再次调用onError
                                    if (localDoctor == null) {
                                        onError?.invoke(message)
                                    }
                                },
                                // 不显示Toast消息，避免重复提示
                                successToastMessage = null,
                                errorToastMessage = null
                            )
                        }

                        override fun onFailure(e: IOException) {
                            e.printStackTrace()
                            // 如果网络请求失败但已经返回了本地数据，不需要再次调用onError
                            if (localDoctor == null) {
                                onError?.invoke("Network error: ${e.message}")
                            }
                            
                            // 记录网络错误日志
                            Log.e(TAG, "Network error when $errorLogTag: ${e.message}")
                        }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Database error when $errorLogTag: ${e.message}")
                
                // 如果本地数据库访问失败，直接请求网络
                withContext(Dispatchers.Main) {
                    networkFetch(object: OkhttpUtil.NetworkCallback {
                        override fun onSuccess(response: String) {
                            ResponseHandler.processApiResponse(
                                response = response,
                                context = context,
                                typeToken = object : TypeToken<RespResult<DoctorDTO>>() {},
                                onSuccess = { doctorDTO ->
                                    val doctor = doctorDTO.toEntity()
                                    
                                    // 更新本地数据库
                                    GlobalScope.launch(Dispatchers.IO) {
                                        try {
                                            doctorDao.insertDoctor(doctor)
                                            
                                            withContext(Dispatchers.Main) {
                                                onSuccess?.invoke(doctorDTO)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            withContext(Dispatchers.Main) {
                                                onError?.invoke("Failed to save doctor: ${e.message}")
                                            }
                                        }
                                    }
                                },
                                onError = { message ->
                                    onError?.invoke(message)
                                }
                            )
                        }

                        override fun onFailure(e: IOException) {
                            e.printStackTrace()
                            onError?.invoke("Network error: ${e.message}")
                        }
                    })
                }
            }
        }
    }

    /**
     * 根据社区名称查询医生
     *
     * @param community 社区名称
     * @param context 上下文
     * @param onSuccess 成功回调，返回医生DTO列表
     * @param onError 错误回调，返回错误信息
     */
    fun getDoctorsByCommunity(
        community: String,
        context: Context,
        onSuccess: ((List<DoctorDTO>) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        DoctorApi.getDoctorsByCommunity(community, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response = response,
                    context = context,
                    typeToken = object : TypeToken<RespResult<List<DoctorDTO>>>() {},
                    onSuccess = { doctorDTOs ->
                        // 保存医生数据到本地数据库
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                // 将DTO转换为实体对象并保存
                                val doctorEntities = doctorDTOs.map { it.toEntity() }
                                doctorDao.insertDoctors(doctorEntities)

                                // 在主线程调用成功回调
                                withContext(Dispatchers.Main) {
                                    onSuccess?.invoke(doctorDTOs)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    val errorMsg = "保存医生数据失败: ${e.message}"
                                    Log.e(TAG, errorMsg, e)
                                    onError?.invoke(errorMsg)
                                }
                            }
                        }
                    },
                    onError = { errorMsg ->
                        onError?.invoke(errorMsg)
                    },
                    errorToastMessage = "获取社区医生失败"
                )
            }

            override fun onFailure(e: IOException) {
                // 网络请求失败，尝试从本地获取数据
                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        val localDoctors = withContext(Dispatchers.IO) {
                            doctorDao.getDoctorsByCommunity(community)
                        }

                        if (localDoctors.isNotEmpty()) {
                            // 如果本地有数据，转换为DTO并返回
                            val dtos = localDoctors.map { it.toDTO() }
                            onSuccess?.invoke(dtos)
                            return@launch
                        }

                        // 如果本地没有数据，显示错误
                        val errorMsg = "网络请求失败: ${e.message}"
                        Log.e(TAG, errorMsg, e)
                        onError?.invoke(errorMsg)
                    } catch (ex: Exception) {
                        Log.e(TAG, "获取本地医生数据失败: ${ex.message}", ex)
                        onError?.invoke("获取医生数据失败: ${ex.message}")
                    }
                }
            }
        })
    }
}
