package com.example.chms_android.utils

import java.sql.Timestamp
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 日期工具类，处理日期格式转换和时区问题
 */
object DateUtil {
    private const val TAG = "DateUtil"

    fun getCurrentTimestamp(): Timestamp {
        val currentTimeMillis = System.currentTimeMillis()
        return Timestamp(currentTimeMillis)
    }

    /**
     * 将字符串解析为日期对象，不考虑时区
     *
     * @param dateString 日期字符串，如 "1985-04-09"
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @return 解析后的日期对象，如果解析失败则返回null
     */
    fun parseDate(dateString: String, format: String = "yyyy-MM-dd"): Date? {
        return try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            // 不设置时区，使用本地时区

            // 解析日期
            val parsedDate = dateFormat.parse(dateString)

            // 创建一个只包含年月日的日历对象
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate

            // 重置时分秒毫秒为0
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            calendar.time
        } catch (e: ParseException) {
            Log.e(TAG, "Failed to parse date: $dateString", e)
            null
        }
    }

    /**
     * 将日期对象格式化为字符串，不考虑时区
     *
     * @param date 日期对象
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @return 格式化后的日期字符串，如果日期为null则返回空字符串
     */
    fun formatDate(date: Date?, format: String = "yyyy-MM-dd"): String {
        if (date == null) return ""

        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        // 不设置时区，使用本地时区

        return dateFormat.format(date)
    }

    /**
     * 修复JSON中的日期字符串，确保不会因为时区问题导致日期偏差
     *
     * @param jsonString JSON字符串
     * @return 修复后的JSON字符串
     */
    fun fixDateInJson(jsonString: String): String {
        // 查找日期格式的字符串并替换
        // 这是一个简单的实现，可能需要根据实际情况调整
        val datePattern = "\"(\\d{4}-\\d{2}-\\d{2})T00:00:00\\.000\\+0000\""
        val regex = Regex(datePattern)

        return regex.replace(jsonString) { matchResult ->
            val dateWithTime = matchResult.groupValues[1]
            "\"$dateWithTime\""
        }
    }
}