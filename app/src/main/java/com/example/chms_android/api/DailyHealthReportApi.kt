package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.common.Constants
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.sql.Timestamp
import java.text.SimpleDateFormat

object DailyHealthReportApi {

    fun addDailyHealthReport(dailyHealthReport: DailyHealthReport, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置全局日期格式
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        val jsonBody = gson.toJson(dailyHealthReport)

        OkhttpUtil.postRequest("/reports/addReport", jsonBody, context, callback, needsToken = !Constants.IS_TESTING, useJson = true)
    }
}