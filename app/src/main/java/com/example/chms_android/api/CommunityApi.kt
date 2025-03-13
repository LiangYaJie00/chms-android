package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.utils.OkhttpUtil

object CommunityApi {

    fun getAllCommunity(context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/community/getAll", context, callback, needsToken = false)
    }
}