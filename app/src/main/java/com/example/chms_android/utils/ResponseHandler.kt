package com.example.chms_android.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chms_android.vo.RespResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ResponseHandler {
    fun <T> processApiResponse(
        response: String, // API 返回的响应内容
        context: Context, // 上下文对象
        typeToken: TypeToken<RespResult<T>>, // 指定要解析的响应数据的具体类型
        onSuccess: (T) -> Unit, // 成功处理数据后的回调函数
        onError: (String) -> Unit, // 当响应代码不是 "200" 或其他信号错误情况时触发的回调函数
        successToastMessage: String, // 成功执行 onSuccess 操作时显示的 Toast 消息内容
        errorToastMessage: String // 当解析过程中出现错误或当服务器返回非 "200" 状态码时，显示的 Toast 消息内容
    ) {
        val TAG = "ResponseHandler"
        try {
            val gson = Gson()
            val resultType = typeToken.type
            val result: RespResult<T> = gson.fromJson(response, resultType)

            if (result.code == "200") {
                val data = result.data
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        onSuccess(data)
                        withContext(Dispatchers.Main) {
                            ToastUtil.show(context, successToastMessage, Toast.LENGTH_SHORT)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            val errorMsg = "$errorToastMessage: ${e.message}"
                            ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
                            Log.e(TAG, "Error in onSuccess callback: $errorMsg", e)
                        }
                    }
                }
            } else {
                val errorMsg = "$errorToastMessage: ${result.message}"
                onError(result.message)
                ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
                Log.e(TAG, "API returned non-200 code: ${result.code}, message: ${result.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "Failed to parse response: ${e.message}"
            ToastUtil.show(context, errorMsg, Toast.LENGTH_SHORT)
            Log.e(TAG, errorMsg, e)
        }
    }
}