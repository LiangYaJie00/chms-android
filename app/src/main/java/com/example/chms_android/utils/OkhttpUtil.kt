package com.example.chms_android.utils

import android.content.Context
import com.example.chms_android.common.Constants
import okhttp3.OkHttpClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object OkhttpUtil {
    private const val BASE_URL = Constants.BASE_URL

    // 初始化OkHttpClient
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        callback.onSuccess(responseBody)
                    } ?: run {
                        callback.onFailure(IOException("Empty Response Body"))
                    }
                } else {
                    callback.onFailure(IOException("Unexpected code $response"))
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
            val jsonObject = JSONObject(jsonBody)
            val formBodyBuilder = FormBody.Builder()
            jsonObject.keys().forEach { key ->
                formBodyBuilder.add(key, jsonObject.getString(key))
            }
            formBodyBuilder.build()
        }

        val requestBuilder = Request.Builder()
            .url(getUrl(uri))
            .post(requestBody)

        // 根据需要添加 token 头
        if (needsToken) {
            requestBuilder.header("Authorization", "Bearer ${TokenUtil.getToken(context)}")
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        callback.onSuccess(it)
                    } ?: run {
                        callback.onFailure(IOException("Empty Response Body"))
                    }
                } else {
                    callback.onFailure(IOException("Unexpected code $response"))
                }
            }
        })
    }

    // 获取完整的url
    fun getUrl(uri: String): String {
        return BASE_URL + uri
    }

}