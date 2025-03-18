package com.example.chms_android.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.view.WindowCompat

class WindowUtil(private val context: Context) {
    private val spUtil: SPUtil = SPUtil(context)

    companion object {
        private const val STATUS_BAR_HEIGHT = "STATUS_BAR_HEIGHT"
        private const val DEFAULT_STATUS_BAR_HEIGHT = 37 // Assuming userId is of type Long

        /**
         * 设置状态栏文字颜色。
         * @param activity 需要设置状态栏的 Activity
         * @param isLight 是否使用亮色文本（黑色图标），如果为 false，则使用暗色文本（白色图标）
         */
        fun setStatusBarTextColor(activity: Activity, isLight: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                insetsController?.isAppearanceLightStatusBars = isLight
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decorView = activity.window.decorView
                decorView.systemUiVisibility = if (isLight) {
                    decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
    }

    fun handleStatusBarHeight(): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        var height = if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            DEFAULT_STATUS_BAR_HEIGHT
        }

        saveStatusBarHeight(height)

        return height
    }

    fun saveStatusBarHeight(height: Int) {
        spUtil.put(STATUS_BAR_HEIGHT, height)
    }

    fun getStatusBarHeight(): Int {
        return spUtil.get(STATUS_BAR_HEIGHT, Int::class.java, DEFAULT_STATUS_BAR_HEIGHT)
    }

}