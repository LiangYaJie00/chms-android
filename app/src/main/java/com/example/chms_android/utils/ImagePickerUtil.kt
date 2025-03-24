package com.example.chms_android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ImagePickerUtil(
    private val context: Context, // 可以是 Activity 或 Fragment 的 Context
    private val imagePickerCallback: ImagePickerCallback,
    private val pickImageLauncher: ActivityResultLauncher<Intent>
) {
    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 100
    }

    interface ImagePickerCallback {
        fun onImagePicked(imageUri: Uri)
        fun onPermissionDenied()
    }

    // 检查权限并打开相册
    fun checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_READ_EXTERNAL_STORAGE
                )
            } else {
                imagePickerCallback.onPermissionDenied()
            }
        } else {
            openGallery()
        }
    }

    // 打开相册
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    // 处理权限请求结果
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openGallery()
            } else {
                imagePickerCallback.onPermissionDenied()
            }
        }
    }
}