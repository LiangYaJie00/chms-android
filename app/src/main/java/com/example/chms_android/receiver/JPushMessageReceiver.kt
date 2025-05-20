package com.example.chms_android.receiver

import android.content.Context
import android.util.Log
import cn.jpush.android.api.JPushMessage
import cn.jpush.android.service.JPushMessageReceiver
import org.json.JSONException
import org.json.JSONObject

/**
 * 自定义JPush消息接收器
 * 用于接收JPush的消息回调
 */
class JPushMessageReceiver : JPushMessageReceiver() {
    private val TAG = "JPushMessageReceiver"

    /**
     * 注册成功回调
     */
    override fun onRegister(context: Context, registrationId: String) {
        Log.d(TAG, "onRegister: $registrationId")
    }

    /**
     * 注册失败回调
     */
    fun onConnectFailed(context: Context, s: String) {
        Log.e(TAG, "onConnectFailed: $s")
    }

    /**
     * 别名操作回调
     */
    override fun onAliasOperatorResult(context: Context, jPushMessage: JPushMessage) {
        Log.d(TAG, "onAliasOperatorResult: ${jPushMessage.alias}, errorCode: ${jPushMessage.errorCode}")
    }

    /**
     * 标签操作回调
     */
    override fun onTagOperatorResult(context: Context, jPushMessage: JPushMessage) {
        Log.d(TAG, "onTagOperatorResult: ${jPushMessage.tags}, errorCode: ${jPushMessage.errorCode}")
    }

    /**
     * 手机号码操作回调
     */
    override fun onMobileNumberOperatorResult(context: Context, jPushMessage: JPushMessage) {
        Log.d(TAG, "onMobileNumberOperatorResult: ${jPushMessage.mobileNumber}, errorCode: ${jPushMessage.errorCode}")
    }

    /**
     * 通知消息到达回调
     */
    override fun onNotifyMessageArrived(context: Context, message: cn.jpush.android.api.NotificationMessage) {
        Log.d(TAG, "onNotifyMessageArrived: ${message.notificationTitle}, content: ${message.notificationContent}")
        try {
            val extras = JSONObject(message.notificationExtras)
            Log.d(TAG, "extras: $extras")
        } catch (e: JSONException) {
            Log.e(TAG, "Parse notification extras error", e)
        }
    }

    /**
     * 通知消息点击回调
     */
    override fun onNotifyMessageOpened(context: Context, message: cn.jpush.android.api.NotificationMessage) {
        Log.d(TAG, "onNotifyMessageOpened: ${message.notificationTitle}, content: ${message.notificationContent}")
        try {
            val extras = JSONObject(message.notificationExtras)
            Log.d(TAG, "extras: $extras")
            
            // 可以在这里处理通知点击事件，例如打开特定页面
        } catch (e: JSONException) {
            Log.e(TAG, "Parse notification extras error", e)
        }
    }
}