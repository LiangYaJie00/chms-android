package com.example.chms_android.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.chms_android.api.OssApi
import com.example.chms_android.vo.RespResult
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object ImageUploadUtil {
    private const val TAG = "ImageUploadUtil"
    
    interface ImageUploadCallback {
        fun onSuccess(imageUrl: String)
        fun onFailure(errorMessage: String)
    }
    
    fun uploadImage(context: Context, imageUri: Uri, callback: ImageUploadCallback) {
        try {
            // 将Uri转换为File
            val file = uriToFile(context, imageUri)
            if (file == null) {
                callback.onFailure("无法读取图片文件")
                return
            }
            
            // 获取文件的MIME类型
            val mimeType = getMimeType(file)
            
            // 创建RequestBody
            val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
            
            // 创建MultipartBody.Part
            val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
            
            // 调用OssApi上传图片
            OssApi.uploadImage(body, context, object : OkhttpUtil.NetworkCallback {
                override fun onSuccess(response: String) {
                    try {
                        // 解析响应获取图片URL
                        ResponseHandler.processApiResponse(
                            response,
                            context,
                            object : TypeToken<RespResult<String>>() {},
                            onSuccess = { imageUrl ->
                                callback.onSuccess(imageUrl)
                            },
                            onError = { message ->
                                callback.onFailure(message)
                            },
                            successToastMessage = "图片上传成功",
                            errorToastMessage = "图片上传失败"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onFailure("解析响应失败: ${e.message}")
                    }
                }
                
                override fun onFailure(e: IOException) {
                    e.printStackTrace()
                    callback.onFailure("网络请求失败: ${e.message}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure("图片上传过程出错: ${e.message}")
        }
    }
    
    // 将Uri转换为File
    private fun uriToFile(context: Context, uri: Uri): File? {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val fileName = getFileName(context, uri)
                val file = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                inputStream.close()
                outputStream.close()
                return file
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting uri to file: ${e.message}")
        }
        return null
    }
    
    // 获取文件名
    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = "image_${System.currentTimeMillis()}.jpg"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex("_display_name")
                if (displayNameIndex != -1) {
                    val displayName = it.getString(displayNameIndex)
                    if (!displayName.isNullOrEmpty()) {
                        fileName = displayName
                    }
                }
            }
        }
        return fileName
    }
    
    // 获取文件的MIME类型
    private fun getMimeType(file: File): String {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
    }
}