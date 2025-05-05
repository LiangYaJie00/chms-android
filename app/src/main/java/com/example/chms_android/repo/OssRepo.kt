package com.example.chms_android.repo

import android.content.Context
import android.net.Uri
import com.example.chms_android.utils.ImageUploadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OssRepo {
    
    /**
     * 上传图片并返回URL (同步方法)
     */
    fun uploadImage(context: Context, imageUri: Uri, callback: (String?, String?) -> Unit) {
        ImageUploadUtil.uploadImage(context, imageUri, object : ImageUploadUtil.ImageUploadCallback {
            override fun onSuccess(imageUrl: String) {
                callback(imageUrl, null)
            }
            
            override fun onFailure(errorMessage: String) {
                callback(null, errorMessage)
            }
        })
    }
    
    /**
     * 上传图片并返回URL (协程方法)
     * 可以在ViewModel中使用
     */
    suspend fun uploadImageCoroutine(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            ImageUploadUtil.uploadImage(context, imageUri, object : ImageUploadUtil.ImageUploadCallback {
                override fun onSuccess(imageUrl: String) {
                    if (continuation.isActive) {
                        continuation.resume(imageUrl)
                    }
                }
                
                override fun onFailure(errorMessage: String) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(Exception(errorMessage))
                    }
                }
            })
        }
    }
}