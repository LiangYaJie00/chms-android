package com.example.chms_android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ImagePickerUtil(
    private val context: Context, // 可以是 Activity 或 Fragment 的 Context
    private val imagePickerCallback: ImagePickerCallback,
    private val pickImageLauncher: ActivityResultLauncher<Intent>
) {
    companion object {
        private const val TAG = "ImagePickerUtil"
        private const val REQUEST_CODE_READ_MEDIA = 100
    }

    private val spUtil by lazy { SPUtil(context) }

    interface ImagePickerCallback {
        fun onImagePicked(imageUri: Uri)
        fun onPermissionDenied()
    }

    // 检查权限并打开相册
    fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 文件读取权限
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // Android 6 - Android 12 的文件读取权限
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // 使用SPUtil获取请求记录
        val permissionKey = "has_requested_$permission"
        val hasRequested = spUtil.get(permissionKey, Boolean::class.java, false)

        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            context is Activity && hasRequested -> {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    // 用户之前拒绝过但未禁用询问
                    showRationaleDialog(permission)
                } else {
                    // 用户勾选了不再询问
                    showPermissionDeniedDialog()
                }
            }
            context is Activity -> {
                // 首次请求，记录请求状态
                spUtil.put(permissionKey, true)
                ActivityCompat.requestPermissions(context, arrayOf(permission), REQUEST_CODE_READ_MEDIA)
            }
            else -> {
                imagePickerCallback.onPermissionDenied()
            }
        }
    }

    // 首次权限请求弹窗
    private fun showRationaleDialog(permission: String) {
        AlertDialog.Builder(context)
            .setTitle("权限请求")
            .setMessage("应用程序需要访问存储权限以选择图片。请在接下来的提示中授予权限。")
            .setPositiveButton("确定") { _, _ ->
                if (context is Activity) {
                    ActivityCompat.requestPermissions(context, arrayOf(permission), REQUEST_CODE_READ_MEDIA)
                }
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                imagePickerCallback.onPermissionDenied()
            }
            .create()
            .show()
    }

    // 打开设置界面
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    // 打开相册
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    // 权限拒绝后再次请求的弹窗
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(context)
            .setTitle("需要权限")
            .setMessage("我们需要访问存储以选择图片。请在设置中启用权限。")
            .setPositiveButton("前往设置") { _, _ ->
                openSettings()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                imagePickerCallback.onPermissionDenied()
            }
            .create()
            .show()
    }

    // 处理权限请求结果
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_READ_MEDIA) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openGallery()
            } else {
                showPermissionDeniedDialog()
            }
//            else if (context is Activity && !ActivityCompat.shouldShowRequestPermissionRationale(context, permissions[0])) {
//                // 如果用户拒绝授权，弹出设置请求弹窗
//                showPermissionDeniedDialog()
//            } else {
//                imagePickerCallback.onPermissionDenied()
//            }
        }
    }


}