package com.example.chms_android.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Gson工具类，提供自定义的序列化和反序列化功能
 */
object GsonUtil {
    
    /**
     * 创建一个配置了日期处理的Gson实例
     */
    fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .registerTypeAdapter(Timestamp::class.java, TimestampDeserializer())
            .registerTypeAdapter(Date::class.java, DateSerializer())
            .registerTypeAdapter(Timestamp::class.java, TimestampSerializer())
            .create()
    }
    
    /**
     * 日期反序列化器，支持多种日期格式
     */
    class DateDeserializer : JsonDeserializer<Date> {
        private val dateFormats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ss",
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
    
    /**
     * 时间戳反序列化器，支持多种日期格式
     */
    class TimestampDeserializer : JsonDeserializer<Timestamp> {
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
    
    /**
     * 日期序列化器，将Date转换为标准格式的字符串
     */
    class DateSerializer : JsonSerializer<Date> {
        override fun serialize(
            src: Date?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return if (src == null) {
                JsonPrimitive("")
            } else {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                JsonPrimitive(format.format(src))
            }
        }
    }
    
    /**
     * 时间戳序列化器，将Timestamp转换为标准格式的字符串
     */
    class TimestampSerializer : JsonSerializer<Timestamp> {
        override fun serialize(
            src: Timestamp?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return if (src == null) {
                JsonPrimitive("")
            } else {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                JsonPrimitive(format.format(src))
            }
        }
    }
}