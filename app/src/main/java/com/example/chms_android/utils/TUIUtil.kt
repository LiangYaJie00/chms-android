package com.example.chms_android.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chms_android.data.User
import com.tencent.cloud.tuikit.engine.call.TUICallDefine
import com.tencent.cloud.tuikit.engine.call.TUICallEngine
import com.tencent.cloud.tuikit.engine.call.TUICallObserver
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine.Callback
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.TUICallback
import com.tencent.qcloud.tuikit.tuicallkit.TUICallKit
import com.tencent.qcloud.tuikit.tuicallkit.debug.GenerateTestUserSig
import java.lang.ref.WeakReference

object TUIUtil {
    private const val TENCENT_TRTC_SDKAPPID = 1600078530
    private const val TENCENT_TRTC_SECRETKEY = "7187547f2eff3f4b06f3c93196ba2c678a9b5771d65749f57e0d7ff099bb9e78"
    private const val TAG = "TUIUtil"

    private lateinit var contextRef: WeakReference<Context>

    private val tuiCallKit: TUICallKit by lazy {
        contextRef.get()?.let {
            TUICallKit.createInstance(it)
        } ?: throw IllegalStateException("TUIUtil has not been initialized with a valid Context.")
    }

    // 初始化TUI
    fun initTUI(context: Context) {
        initialize(context)
        loginTUI()
        setupCallObserver()
    }

    fun initialize(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    // 完成 TUI 组件的登录。这一步骤至关重要，只有在成功登录之后，您才能正常使用 TUICallKit 提供的各项功能。
    fun loginTUI() {
        val context = contextRef.get() ?: throw IllegalStateException("Context is no longer valid.")
        val userId = AccountUtil(context).getUserId().toString()
        val sdkAppId = TENCENT_TRTC_SDKAPPID   // 在控制台得到的SDKAppID
        val secretKey = TENCENT_TRTC_SECRETKEY // 在控制台得到的SecretKey

        val userSig = GenerateTestUserSig.genTestUserSig(userId, sdkAppId, secretKey)

        TUILogin.login(context, sdkAppId, userId, userSig, object : TUICallback() {
            override fun onSuccess() {
                initSelfInfo()
                enableIncomingBanner(true)
            }
            override fun onError(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "TUI登录失败: $errorCode, $errorMessage")
            }
        })
    }

    // 设置本人的信息
    fun initSelfInfo() {
        val context = contextRef.get() ?: throw IllegalStateException("Context is no longer valid.")
        val me = AccountUtil(context).getUser()

        tuiCallKit.setSelfInfo(me?.name, me?.avatar, object: Callback {
            override fun onSuccess() {
                Log.i(TAG, "用户信息设置成功。")
            }

            override fun onError(errCode: Int, errMsg: String?) {
                Log.e(TAG, "用户信息设置失败。错误码：$errCode，错误信息：$errMsg")
            }
        })
    }

    // 发起音频通话
    fun startAudio(toUser: User) {
        val list = mutableListOf<String>()
        list.add(toUser.userId.toString())
        tuiCallKit.calls(list, TUICallDefine.MediaType.Audio, null, object: Callback {
            override fun onSuccess() {
                Log.i(TAG, "音频通话启动成功。")
            }

            override fun onError(errCode: Int, errMsg: String?) {
                Log.e(TAG, "音频通话启动失败。错误码：$errCode，错误信息：$errMsg")
            }
        })
    }

    // 发起视频通话
    fun startVideo(toUser: User) {
        val list = mutableListOf<String>()
        list.add(toUser.userId.toString())
        tuiCallKit.calls(list, TUICallDefine.MediaType.Video, null, object: Callback {
            override fun onSuccess() {
                Log.i(TAG, "视频通话启动成功。")
            }

            override fun onError(errCode: Int, errMsg: String?) {
                Log.e(TAG, "视频通话启动失败。错误码：$errCode，错误信息：$errMsg")
            }
        })
    }

    // 开启/关闭来电横幅显示。
    fun enableIncomingBanner(isOn: Boolean) {
        tuiCallKit.enableIncomingBanner(isOn)
    }

    // 设置通话结束监听器
    private fun setupCallObserver() {
        val context = contextRef.get() ?: return
        
        // 使用TUICallEngine的观察者机制监听通话状态
        val callEngine = TUICallEngine.createInstance(context)
        
        // 添加通话状态观察者
        callEngine.addObserver(object : TUICallObserver() {
            override fun onCallEnd(
                roomId: TUICommonDefine.RoomId?,
                callMediaType: TUICallDefine.MediaType?,
                callRole: TUICallDefine.Role?,
                totalTime: Long
            ) {
                Log.i(TAG, "通话结束，总时长: $totalTime 秒")
                
                // 发送通话结束广播
                val intent = Intent("com.example.chms_android.CALL_ENDED")
                intent.putExtra("TOTAL_TIME", totalTime)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                Log.i(TAG, "通话结束，已发送广播")
            }
        })
    }
}