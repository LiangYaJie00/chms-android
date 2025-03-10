package com.example.chms_android.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SPUtil(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * 保存一个键值对
     */
    fun <T> put(key: String, value: T) {
        val editor = sharedPreferences.edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
        editor.apply()
    }

    /**
     * 获取保存的值
     */
    fun <T> get(key: String, clazz: Class<T>, defaultValue: T): T {
        return when {
            clazz.isAssignableFrom(String::class.java) -> sharedPreferences.getString(key, defaultValue as? String ?: "") as T
            clazz.isAssignableFrom(Int::class.java) -> sharedPreferences.getInt(key, defaultValue as? Int ?: -1) as T
            clazz.isAssignableFrom(Boolean::class.java) -> sharedPreferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            clazz.isAssignableFrom(Float::class.java) -> sharedPreferences.getFloat(key, defaultValue as? Float ?: -1.0f) as T
            clazz.isAssignableFrom(Long::class.java) -> sharedPreferences.getLong(key, defaultValue as? Long ?: -1) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    /**
     * 保存对象列表
     */
    fun <T> putObjectList(key: String, list: List<T>) {
        val json = gson.toJson(list)
        put(key, json)
    }

    /**
     * 获取对象列表
     */
    fun <T> getObjectList(key: String, clazz: Class<T>): List<T> {
        val json = get<String>(key, String::class.java, "")
        val listType = TypeToken.getParameterized(List::class.java, clazz).type
        return if (json.isNotEmpty()) {
            gson.fromJson(json, listType) ?: emptyList()
        } else {
            emptyList()
        }
    }
}
