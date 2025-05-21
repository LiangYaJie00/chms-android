package com.example.chms_android.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.vo.RespResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ResponseHandler {
    // 创建自定义的日期反序列化器
    private class DateDeserializer : JsonDeserializer<Date> {
        private val dateFormats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ss", // 添加这种格式
            "yyyy-MM-dd",
            "yyyy/MM/dd"
        )

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Date {
            val dateString = json.asString
            
            // 尝试使用不同的日期格式解析
            for (format in dateFormats) {
                try {
                    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    return dateFormat.parse(dateString) ?: throw JsonParseException("Failed to parse date: $dateString")
                } catch (e: ParseException) {
                    // 尝试下一个格式
                }
            }
            
            throw JsonParseException("Failed to parse date: $dateString")
        }
    }
    
    // 创建自定义的Timestamp反序列化器
    private class TimestampDeserializer : JsonDeserializer<Timestamp> {
        private val dateFormats = arrayOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd",
            "yyyy/MM/dd"
        )

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Timestamp {
            val dateString = json.asString
            
            // 尝试使用不同的日期格式解析
            for (format in dateFormats) {
                try {
                    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date = dateFormat.parse(dateString)
                    return Timestamp(date.time)
                } catch (e: ParseException) {
                    // 尝试下一个格式
                }
            }
            
            throw JsonParseException("Failed to parse timestamp: $dateString")
        }
    }

    fun <T> processApiResponse(
        response: String, // API 返回的响应内容
        context: Context, // 上下文对象
        typeToken: TypeToken<RespResult<T>>, // 指定要解析的响应数据的具体类型
        onSuccess: (T) -> Unit, // 成功处理数据后的回调函数
        onError: (String) -> Unit, // 当响应代码不是 "200" 或其他信号错误情况时触发的回调函数
        successToastMessage: String? = null, // 成功执行 onSuccess 操作时显示的 Toast 消息内容，默认为null
        errorToastMessage: String? = null // 当解析过程中出现错误或当服务器返回非 "200" 状态码时，显示的 Toast 消息内容，默认为null
    ) {
        val TAG = "ResponseHandler"
        
        // 确保在后台线程中执行JSON解析
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 使用自定义的Gson实例，添加日期和时间戳反序列化器
                val gson = GsonBuilder()
                    .registerTypeAdapter(Date::class.java, DateDeserializer())
                    .registerTypeAdapter(Timestamp::class.java, TimestampDeserializer())
                    .create()
                
                val resultType = typeToken.type
                val result: RespResult<T> = gson.fromJson(response, resultType)

                if (result.code == "200") {
                    val data = result.data
                    try {
                        // 将数据处理移到主线程中执行
                        withContext(Dispatchers.Main) {
                            onSuccess(data)
                            successToastMessage?.let {
                                ToastUtil.show(context, it, Toast.LENGTH_SHORT)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            val errorMsg = if (errorToastMessage != null) "$errorToastMessage: ${e.message}" else "Error: ${e.message}"
                            ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
                            Log.e(TAG, "Error in onSuccess callback: $errorMsg", e)
                        }
                    }
                } else {
                    // 确保错误回调在主线程中执行
                    withContext(Dispatchers.Main) {
                        val errorMsg = if (errorToastMessage != null) "$errorToastMessage: ${result.message}" else result.message
                        onError(result.message)
                        ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
                        Log.e(TAG, "API returned non-200 code: ${result.code}, message: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 确保异常处理在主线程中执行
                withContext(Dispatchers.Main) {
                    val errorMsg = if (errorToastMessage != null) "$errorToastMessage: ${e.message}" else "Failed to parse response: ${e.message}"
                    ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
                    Log.e(TAG, errorMsg, e)
                    onError(errorMsg)
                }
            }
        }
    }
}