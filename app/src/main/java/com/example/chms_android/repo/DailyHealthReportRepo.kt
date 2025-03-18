package com.example.chms_android.repo

import android.content.Context
import android.widget.Toast
import com.example.chms_android.api.DailyHealthReportApi
import com.example.chms_android.dao.DailyHealthReportDao
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.database.DatabaseProvider
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.utils.ResponseHandler
import com.example.chms_android.utils.ToastUtil
import com.example.chms_android.vo.RespResult
import com.google.gson.reflect.TypeToken
import java.io.IOException

object DailyHealthReportRepo {
    private val dailyHealthReportDao: DailyHealthReportDao get() = DatabaseProvider.getDatabase().dailyHealthReportDao()

    fun addDailyHealthReport(dailyHealthReport: DailyHealthReport, context: Context) {
        DailyHealthReportApi.addDailyHealthReport(dailyHealthReport, context, object : OkhttpUtil.NetworkCallback {
            override fun onSuccess(response: String) {
                ResponseHandler.processApiResponse(
                    response,
                    context,
                    object : TypeToken<RespResult<DailyHealthReport>>() {},
                    onSuccess = { dailyHealthReport ->
                        dailyHealthReportDao.insertDailyHealthReport(dailyHealthReport)
                    },
                    onError = { message ->

                    },
                    successToastMessage = "本日日报上报成功",
                    errorToastMessage = "日报上报失败"
                )
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
                // 弹出接口请求失败的提示框
                ToastUtil.show(context, "Network error: ${e.message}", Toast.LENGTH_SHORT)
            }

        })
    }
}