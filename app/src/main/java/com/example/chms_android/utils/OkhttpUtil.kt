package com.example.chms_android.utils

import android.content.Context
import android.util.Log
import com.example.chms_android.common.Constants
import okhttp3.OkHttpClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class CustomCookieJar : CookieJar {
    // 用于存储特定路径的 Cookies
    private var specificPathCookies: List<Cookie> = listOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (url.encodedPath == "/chms/user/auth/sendCode") {
            // 存储从 /sendCode 接口接收到的 cookies
            specificPathCookies = cookies
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return when (url.encodedPath) {
            "/chms/user/auth/updatePassword", "/chms/user/auth/register" -> specificPathCookies
            else -> listOf()
        }
    }
}

object OkhttpUtil {
    private const val TAG = "OkhttpUtil"
    private const val BASE_URL = Constants.BASE_URL

    // 初始化OkHttpClient，添加重试机制
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(CustomCookieJar())
            .connectTimeout(10, TimeUnit.SECONDS) // 连接超时时间
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // 启用连接失败重试
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES)) // 设置连接池
            .dispatcher(Dispatcher().apply {
                maxRequestsPerHost = 10 // 每个主机的最大请求数
                maxRequests = 30 // 整体最大请求数
            })
            .addInterceptor { chain ->
                // 最多重试3次
                var retryCount = 0
                var response: Response? = null
                var exception: IOException? = null
                
                while (retryCount < 3 && (response == null || !response.isSuccessful)) {
                    try {
                        if (retryCount > 0) {
                            // 重试前等待一段时间
                            Thread.sleep((1000 * retryCount).toLong())
                        }
                        
                        // 关闭之前的响应
                        response?.close()
                        
                        response = chain.proceed(chain.request())
                        exception = null
                    } catch (e: IOException) {
                        exception = e
                        Log.w(TAG, "Retry attempt $retryCount failed", e)
                    } finally {
                        retryCount++
                    }
                    
                    // 如果成功获取响应，跳出循环
                    if (response != null && response.isSuccessful) {
                        break
                    }
                }
                
                // 如果所有重试都失败，抛出最后一个异常
                exception?.let { 
                    Log.e(TAG, "All retry attempts failed", it)
                    throw it 
                }
                
                // 返回响应
                response!!
            }
            .build()
    }

    // Callback interface to handle responses
    interface NetworkCallback {
        fun onSuccess(response: String)
        fun onFailure(e: IOException)
    }

    // 执行GET请求
    fun getRequest(
        uri: String,
        context: Context,
        callback: NetworkCallback,
        needsToken: Boolean = true // 增加参数以判断是否需要 token
    ) {
        val requestBuilder = Request.Builder()
            .url(getUrl(uri))

        // 根据需要添加token头
        if (needsToken) {
            requestBuilder.header("Authorization", "${TokenUtil.getToken(context)}")
        }

        val request = requestBuilder.build()
        Log.d(TAG, "GET Request: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "GET Request failed for ${call.request().url}", e)
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseBody ->
                            Log.d(TAG, "GET Request successful: ${call.request().url}")
                            callback.onSuccess(responseBody)
                        } ?: run {
                            val error = IOException("Empty Response Body")
                            Log.e(TAG, "GET Request empty body: ${call.request().url}", error)
                            callback.onFailure(error)
                        }
                    } else {
                        val error = IOException("Unexpected code $response")
                        Log.e(TAG, "GET Request unsuccessful: ${call.request().url}, code: ${response.code}", error)
                        callback.onFailure(error)
                    }
                } finally {
                    // 确保响应体被关闭
                    response.body?.close()
                }
            }
        })
    }

    // 执行POST请求
    fun postRequest(
        uri: String,
        jsonBody: String,
        context: Context,
        callback: NetworkCallback,
        needsToken: Boolean = true, // 判断是否需要 token
        useJson: Boolean = true // 参数决定使用 JSON 格式还是表单格式
    ) {
        val requestBody = if (useJson) {
            // 直接使用传入的 JSON 字符串创建请求体
            jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        } else {
            // 将 JSON 字符串解析成中键值对，并构建 FormBody
            try {
                val jsonObject = JSONObject(jsonBody)
                val formBodyBuilder = FormBody.Builder()
                jsonObject.keys().forEach { key ->
                    formBodyBuilder.add(key, jsonObject.getString(key))
                }
                formBodyBuilder.build()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse JSON body for form data", e)
                callback.onFailure(IOException("Failed to parse JSON body: ${e.message}"))
                return
            }
        }

        val requestBuilder = Request.Builder()
            .url(getUrl(uri))
            .post(requestBody)

        // 根据需要添加 token 头
        if (needsToken) {
            requestBuilder.header("Authorization", "${TokenUtil.getToken(context)}")
        }

        val request = requestBuilder.build()
        Log.d(TAG, "POST Request: ${request.url}, useJson: $useJson")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "POST Request failed for ${call.request().url}", e)
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        response.body?.string()?.let {
                            Log.d(TAG, "POST Request successful: ${call.request().url}")
                            callback.onSuccess(it)
                        } ?: run {
                            val error = IOException("Empty Response Body")
                            Log.e(TAG, "POST Request empty body: ${call.request().url}", error)
                            callback.onFailure(error)
                        }
                    } else {
                        val error = IOException("Unexpected code $response")
                        Log.e(TAG, "POST Request unsuccessful: ${call.request().url}, code: ${response.code}", error)
                        callback.onFailure(error)
                    }
                } finally {
                    // 确保响应体被关闭
                    response.body?.close()
                }
            }
        })
    }

    // 获取完整的url
    fun getUrl(uri: String): String {
        return BASE_URL + uri
    }

    // 执行文件上传请求
    fun uploadFile(
        uri: String,
        filePart: MultipartBody.Part,
        context: Context,
        callback: NetworkCallback,
        needsToken: Boolean = true
    ) {
        val requestBuilder = Request.Builder()
            .url(getUrl(uri))
        
        // 根据需要添加token头
        if (needsToken) {
            requestBuilder.header("Authorization", "${TokenUtil.getToken(context)}")
        }
        
        // 创建MultipartBody
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(filePart)
            .build()
        
        val request = requestBuilder
            .post(requestBody)
            .build()
        
        Log.d(TAG, "Upload Request: ${request.url}, file: ${filePart.headers}")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Upload Request failed for ${call.request().url}", e)
                callback.onFailure(e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        response.body?.string()?.let {
                            Log.d(TAG, "Upload Request successful: ${call.request().url}")
                            callback.onSuccess(it)
                        } ?: run {
                            val error = IOException("Empty Response Body")
                            Log.e(TAG, "Upload Request empty body: ${call.request().url}", error)
                            callback.onFailure(error)
                        }
                    } else {
                        val error = IOException("Unexpected code $response")
                        Log.e(TAG, "Upload Request unsuccessful: ${call.request().url}, code: ${response.code}", error)
                        callback.onFailure(error)
                    }
                } finally {
                    // 确保响应体被关闭
                    response.body?.close()
                }
            }
        })
    }

    // 关闭OkHttpClient
    fun shutdown() {
        try {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()
            Log.d(TAG, "OkHttpClient shutdown successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down OkHttpClient", e)
        }
    }

}