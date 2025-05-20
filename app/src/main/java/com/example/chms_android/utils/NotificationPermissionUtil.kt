package com.example.chms_android.utils

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * 通知权限工具类
 * 用于检查和请求通知权限
 */
class NotificationPermissionUtil {

    companion object {
        private const val TAG = "NotificationPermissionUtil"

        /**
         * 检查是否有通知权限
         * @param context 上下文
         * @return 是否有通知权限
         */
        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13及以上版本检查POST_NOTIFICATIONS权限
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // Android 13以下版本检查通知是否开启
                isNotificationEnabled(context)
            }
        }

        /**
         * 检查通知是否开启（适用于Android 13以下版本）
         * @param context 上下文
         * @return 通知是否开启
         */
        fun isNotificationEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0及以上
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                return manager.areNotificationsEnabled()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Android 4.4 - 7.1
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val appInfo = context.applicationInfo
                val packageName = context.packageName
                val uid = appInfo.uid
                try {
                    val appOpsClass = Class.forName(AppOpsManager::class.java.name)
                    val checkOpNoThrowMethod = appOpsClass.getMethod(
                        "checkOpNoThrow", Integer.TYPE, Integer.TYPE, String::class.java
                    )
                    val opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION")
                    val value = opPostNotificationValue.get(Int::class.java) as Int
                    return checkOpNoThrowMethod.invoke(appOps, value, uid, packageName) as Int == AppOpsManager.MODE_ALLOWED
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return true // 默认返回true
        }

        /**
         * 请求通知权限（Activity中使用）
         * @param activity Activity实例
         * @param onGranted 权限授予后的回调
         * @param onDenied 权限拒绝后的回调
         */
        fun requestPermissionInActivity(
            activity: AppCompatActivity,
            onGranted: () -> Unit = {},
            onDenied: () -> Unit = {}
        ) {
            if (hasNotificationPermission(activity)) {
                onGranted()
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 注册权限请求回调
                val requestPermissionLauncher = activity.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        Log.d(TAG, "通知权限已授予")
                        onGranted()
                    } else {
                        Log.d(TAG, "通知权限被拒绝")
                        if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            // 用户选择了"不再询问"
                            showSettingsDialog(activity, onDenied)
                        } else {
                            onDenied()
                        }
                    }
                }

                // 检查是否需要显示权限解释
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showRationaleDialog(activity, Manifest.permission.POST_NOTIFICATIONS, requestPermissionLauncher, onDenied)
                } else {
                    // 直接请求权限
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // Android 13以下版本，引导用户到设置页面
                showSettingsDialog(activity, onDenied)
            }
        }

        /**
         * 请求通知权限（Fragment中使用）
         * @param fragment Fragment实例
         * @param onGranted 权限授予后的回调
         * @param onDenied 权限拒绝后的回调
         */
        fun requestPermissionInFragment(
            fragment: Fragment,
            onGranted: () -> Unit = {},
            onDenied: () -> Unit = {}
        ) {
            val context = fragment.requireContext()
            if (hasNotificationPermission(context)) {
                onGranted()
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 注册权限请求回调
                val requestPermissionLauncher = fragment.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        Log.d(TAG, "通知权限已授予")
                        onGranted()
                    } else {
                        Log.d(TAG, "通知权限被拒绝")
                        if (!fragment.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            // 用户选择了"不再询问"
                            showSettingsDialog(context, onDenied)
                        } else {
                            onDenied()
                        }
                    }
                }

                // 检查是否需要显示权限解释
                if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showRationaleDialog(context, Manifest.permission.POST_NOTIFICATIONS, requestPermissionLauncher, onDenied)
                } else {
                    // 直接请求权限
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // Android 13以下版本，引导用户到设置页面
                showSettingsDialog(context, onDenied)
            }
        }

        /**
         * 显示权限解释对话框
         */
        private fun showRationaleDialog(
            context: Context,
            permission: String,
            launcher: ActivityResultLauncher<String>,
            onDenied: () -> Unit
        ) {
            AlertDialog.Builder(context)
                .setTitle("通知权限")
                .setMessage("为了确保您能及时收到重要消息和提醒，应用需要通知权限。")
                .setPositiveButton("授权") { _, _ ->
                    launcher.launch(permission)
                }
                .setNegativeButton("取消") { _, _ ->
                    onDenied()
                }
                .create()
                .show()
        }

        /**
         * 显示设置引导对话框
         */
        private fun showSettingsDialog(context: Context, onDenied: () -> Unit) {
            AlertDialog.Builder(context)
                .setTitle("开启通知")
                .setMessage("为了确保您能及时收到重要消息和提醒，请在设置中开启通知权限。")
                .setPositiveButton("去设置") { _, _ ->
                    openNotificationSettings(context)
                }
                .setNegativeButton("取消") { _, _ ->
                    onDenied()
                }
                .create()
                .show()
        }

        /**
         * 打开通知设置页面
         */
        private fun openNotificationSettings(context: Context) {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0及以上，打开应用通知渠道设置
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                // Android 8.0以下，打开应用详情页
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.parse("package:${context.packageName}")
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}