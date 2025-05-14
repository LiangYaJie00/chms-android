package com.example.chms_android.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/**
 * 应用工具类，提供应用相关的实用方法
 */
object AppUtil {
    private const val TAG = "AppUtil"

    /**
     * 获取应用版本名称
     * @param context 上下文
     * @return 版本名称，例如 "1.0.0"
     */
    fun getVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            Log.e(TAG, "Error getting version name", e)
            "Unknown"
        }
    }

    /**
     * 获取应用版本号
     * @param context 上下文
     * @return 版本号，例如 1
     */
    fun getVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting version code", e)
            0
        }
    }

    /**
     * 获取格式化的版本信息
     * @param context 上下文
     * @param format 格式化模板，默认为 "v%s (%d)"
     * @return 格式化后的版本信息，例如 "v1.0.0 (1)"
     */
    fun getFormattedVersion(context: Context, format: String = "v%s (%d)"): String {
        val versionName = getVersionName(context)
        val versionCode = getVersionCode(context)
        return String.format(format, versionName, versionCode)
    }

    /**
     * 获取简洁版本信息
     * @param context 上下文
     * @return 简洁版本信息，例如 "v1.0.0"
     */
    fun getSimpleVersion(context: Context): String {
        return "v${getVersionName(context)}"
    }

    /**
     * 获取完整版本信息
     * @param context 上下文
     * @return 完整版本信息，包含版本名称和版本号
     */
    fun getFullVersion(context: Context): String {
        return "v${getVersionName(context)} (${getVersionCode(context)})"
    }
}