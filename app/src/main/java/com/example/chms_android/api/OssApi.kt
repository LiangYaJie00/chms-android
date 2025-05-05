package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.utils.OkhttpUtil
import okhttp3.MultipartBody

object OssApi {
    
    /**
     * 上传图片到OSS
     * 
     * @param filePart 图片文件的MultipartBody.Part
     * @param context 上下文
     * @param callback 网络回调
     */
    fun uploadImage(filePart: MultipartBody.Part, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.uploadFile("/oss/uploadImage", filePart, context, callback, needsToken = false)
    }
}