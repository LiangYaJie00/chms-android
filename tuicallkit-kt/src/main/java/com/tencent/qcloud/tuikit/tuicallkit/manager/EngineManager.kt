package com.tencent.qcloud.tuikit.tuicallkit.manager

import android.content.Context
import android.text.TextUtils
import com.tencent.cloud.tuikit.engine.call.TUICallDefine
import com.tencent.cloud.tuikit.engine.call.TUICallDefine.CallParams
import com.tencent.cloud.tuikit.engine.call.TUICallEngine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine.ValueCallback
import com.tencent.cloud.tuikit.engine.common.TUIVideoView
import com.tencent.imsdk.BaseConstants
import com.tencent.qcloud.tuicore.TUIConfig
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.permission.PermissionCallback
import com.tencent.qcloud.tuicore.util.ErrorMessageConverter
import com.tencent.qcloud.tuicore.util.SPUtils
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.tencent.qcloud.tuikit.tuicallkit.R
import com.tencent.qcloud.tuikit.tuicallkit.data.Constants
import com.tencent.qcloud.tuikit.tuicallkit.data.OfflinePushInfoConfig
import com.tencent.qcloud.tuikit.tuicallkit.data.User
import com.tencent.qcloud.tuikit.tuicallkit.extensions.CallingBellFeature
import com.tencent.qcloud.tuikit.tuicallkit.state.TUICallState
import com.tencent.qcloud.tuikit.tuicallkit.utils.Logger
import com.tencent.qcloud.tuikit.tuicallkit.utils.PermissionRequest
import com.tencent.qcloud.tuikit.tuicallkit.utils.PermissionRequest.requestPermissions
import com.tencent.qcloud.tuikit.tuicallkit.utils.UserInfoUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.Collections

class EngineManager private constructor(context: Context) {

    public val context: Context

    init {
        this.context = context.applicationContext
    }

    companion object {
        const val TAG = "EngineManager"
        var instance: EngineManager = EngineManager(TUIConfig.getAppContext())
        private const val BLUR_LEVEL_HIGH = 3
        private const val BLUR_LEVEL_CLOSE = 0
    }

    fun call(
        userId: String?, callMediaType: TUICallDefine.MediaType?, params: TUICallDefine.CallParams?,
        callback: TUICommonDefine.Callback?
    ) {
        Logger.info(TAG, "call -> {userId: $userId, callMediaType: $callMediaType, params: $params}")
        if (TextUtils.isEmpty(userId)) {
            Logger.error(TAG, "call failed, userId is empty")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "call failed, userId is empty")
            return
        }
        if (TUICallDefine.MediaType.Unknown == callMediaType) {
            Logger.error(TAG, "call failed, callMediaType is Unknown")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "call failed, callMediaType is Unknown")
            return
        }
        TUICallState.instance.selfUser.get().avatar.set(TUILogin.getFaceUrl())
        TUICallState.instance.selfUser.get().nickname.set(TUILogin.getNickName())
        TUICallState.instance.selfUser.get().id = TUILogin.getLoginUser()
        requestPermissions(context, callMediaType!!, object : PermissionCallback() {
            override fun onGranted() {
                TUICallEngine.createInstance(context).call(userId, callMediaType, params,
                    object : TUICommonDefine.Callback {
                        override fun onSuccess() {
                            val user = User()
                            user.id = userId
                            user.callRole.set(TUICallDefine.Role.Called)
                            user.callStatus.set(TUICallDefine.Status.Waiting)
                            UserInfoUtils.updateUserInfo(user)
                            TUICallState.instance.remoteUserList.get()?.add(user)
                            TUICallState.instance.mediaType.set(callMediaType)
                            TUICallState.instance.scene.set(TUICallDefine.Scene.SINGLE_CALL)
                            TUICallState.instance.selfUser.get().callRole.set(TUICallDefine.Role.Caller)
                            TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.Waiting)
                            initAudioPlayDevice()
                            callback?.onSuccess()
                        }

                        override fun onError(errCode: Int, errMsg: String) {
                            val errMessage: String = convertErrorMsg(errCode, errMsg)
                            ToastUtil.toastLongMessage(errMessage)
                            callback?.onError(errCode, errMessage)
                        }
                    })
            }

            override fun onDenied() {
                callback?.onError(TUICallDefine.ERROR_PERMISSION_DENIED, "request Permissions failed")
            }
        })
    }

    fun calls(
        userIdList: List<String?>?, mediaType: TUICallDefine.MediaType?, params: CallParams?,
        callback: TUICommonDefine.Callback?
    ) {
        Logger.info(TAG, "calls, userIdList: $userIdList, callMediaType: $mediaType, params: $params")
        if (TUICallDefine.MediaType.Audio != mediaType && TUICallDefine.MediaType.Video != mediaType) {
            Logger.error(TAG, "calls failed, mediaType is Unknown")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "calls failed, mediaType is Unknown")
            return
        }

        val list = userIdList?.toHashSet()?.toMutableList()
        list?.remove(TUILogin.getLoginUser())
        list?.removeAll(Collections.singleton(null))

        if (list.isNullOrEmpty()) {
            Logger.error(TAG, "calls failed, userIdList is empty")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "calls failed, userIdList is empty")
            return
        }
        if (list.size >= Constants.MAX_USER) {
            ToastUtil.toastLongMessage(context.getString(R.string.tuicallkit_user_exceed_limit))
            Logger.error(TAG, "calls failed, exceeding max user number: 9")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "calls failed, exceeding max user number")
            return
        }
        TUICallState.instance.selfUser.get().avatar.set(TUILogin.getFaceUrl())
        TUICallState.instance.selfUser.get().nickname.set(TUILogin.getNickName())
        TUICallState.instance.selfUser.get().id = TUILogin.getLoginUser()
        requestPermissions(context, mediaType, object : PermissionCallback() {
            override fun onGranted() {
                TUICallEngine.createInstance(context).calls(list, mediaType, params, object : TUICommonDefine.Callback {
                    override fun onSuccess() {
                        for (userId in list) {
                            if (!TextUtils.isEmpty(userId)) {
                                val model = User()
                                model.id = userId
                                model.callRole.set(TUICallDefine.Role.Called)
                                model.callStatus.set(TUICallDefine.Status.Waiting)
                                UserInfoUtils.updateUserInfo(model)
                                TUICallState.instance.remoteUserList.get().add(model)
                            }
                        }
                        TUICallState.instance.mediaType.set(mediaType)
                        TUICallState.instance.groupId.set(params?.chatGroupId)
                        if (params != null && !params.chatGroupId.isNullOrEmpty() || list.size > 1) {
                            TUICallState.instance.scene.set(TUICallDefine.Scene.GROUP_CALL)
                        } else {
                            TUICallState.instance.scene.set(TUICallDefine.Scene.SINGLE_CALL)
                        }
                        
                        TUICallState.instance.selfUser.get().callRole.set(TUICallDefine.Role.Caller)
                        TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.Waiting)
                        initAudioPlayDevice()
                        callback?.onSuccess()
                    }

                    override fun onError(errCode: Int, errMsg: String) {
                        val errMessage: String = convertErrorMsg(errCode, errMsg)
                        ToastUtil.toastLongMessage(errMessage)
                        Logger.error(TAG, "calls errCode:$errCode, errMsg:$errMessage")
                        callback?.onError(errCode, errMessage)
                    }
                })
            }

            override fun onDenied() {
                callback?.onError(TUICallDefine.ERROR_PERMISSION_DENIED, "request Permissions failed")
            }
        })
    }

    fun groupCall(
        groupId: String?, userIdList: List<String?>?, callMediaType: TUICallDefine.MediaType,
        params: TUICallDefine.CallParams?, callback: TUICommonDefine.Callback?
    ) {
        Logger.info(
            TAG, "call -> {groupId: $groupId, userIdList: $userIdList, callMediaType: $callMediaType, params: $params}"
        )
        if (TextUtils.isEmpty(groupId)) {
            Logger.error(TAG, "groupCall failed, groupId is empty")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "groupCall failed, groupId is empty")
            return
        }
        if (TUICallDefine.MediaType.Unknown == callMediaType) {
            Logger.error(TAG, "groupCall failed, callMediaType is Unknown")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "groupCall failed, callMediaType is Unknown")
            return
        }

        val list = userIdList?.toHashSet()?.toMutableList()
        list?.remove(TUILogin.getLoginUser())
        list?.removeAll(Collections.singleton(null))

        if (list == null || list.isEmpty()) {
            Logger.error(TAG, "groupCall failed, userIdList is empty")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "groupCall failed, userIdList is empty")
            return
        }
        if (list.size >= Constants.MAX_USER) {
            ToastUtil.toastLongMessage(context.getString(R.string.tuicallkit_user_exceed_limit))
            Logger.error(TAG, "groupCall failed, exceeding max user number: 9")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "groupCall failed, exceeding max user number")
            return
        }
        TUICallState.instance.selfUser.get().avatar.set(TUILogin.getFaceUrl())
        TUICallState.instance.selfUser.get().nickname.set(TUILogin.getNickName())
        TUICallState.instance.selfUser.get().id = TUILogin.getLoginUser()
        requestPermissions(context, callMediaType, object : PermissionCallback() {
            override fun onGranted() {
                TUICallEngine.createInstance(context).groupCall(
                    groupId, list, callMediaType,
                    params, object : TUICommonDefine.Callback {
                        override fun onSuccess() {
                            for (userId in list) {
                                if (!TextUtils.isEmpty(userId)) {
                                    val model = User()
                                    model.id = userId
                                    model.callRole.set(TUICallDefine.Role.Called)
                                    model.callStatus.set(TUICallDefine.Status.Waiting)
                                    UserInfoUtils.updateUserInfo(model)
                                    TUICallState.instance.remoteUserList.get().add(model)
                                }
                            }
                            TUICallState.instance.mediaType.set(callMediaType)
                            TUICallState.instance.scene.set(TUICallDefine.Scene.GROUP_CALL)
                            TUICallState.instance.groupId.set(groupId)

                            TUICallState.instance.selfUser.get().callRole.set(TUICallDefine.Role.Caller)
                            TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.Waiting)
                            initAudioPlayDevice()
                            callback?.onSuccess()
                        }

                        override fun onError(errCode: Int, errMsg: String) {
                            val errMessage: String = convertErrorMsg(errCode, errMsg)
                            ToastUtil.toastLongMessage(errMessage)
                            Logger.error(TAG, "groupCall errCode:$errCode, errMsg:$errMessage")
                            callback?.onError(errCode, errMessage)
                        }
                    })
            }

            override fun onDenied() {
                callback?.onError(TUICallDefine.ERROR_PERMISSION_DENIED, "request Permissions failed")
            }
        })
    }

    fun joinInGroupCall(roomId: TUICommonDefine.RoomId?, groupId: String?, mediaType: TUICallDefine.MediaType?) {
        val intRoomId = roomId?.intRoomId ?: 0
        val strRoomId = roomId?.strRoomId ?: ""
        if (intRoomId <= 0 && TextUtils.isEmpty(strRoomId)) {
            Logger.error(TAG, "joinInGroupCall failed, roomId is invalid")
            return
        }
        if (TextUtils.isEmpty(groupId)) {
            Logger.error(TAG, "joinInGroupCall failed, groupId is empty")
            return
        }
        if (TUICallDefine.MediaType.Unknown == mediaType) {
            Logger.error(TAG, "joinInGroupCall failed, mediaType is unknown")
            return
        }
        TUICallState.instance.selfUser.get().avatar.set(TUILogin.getFaceUrl())
        TUICallState.instance.selfUser.get().nickname.set(TUILogin.getNickName())
        TUICallState.instance.selfUser.get().id = TUILogin.getLoginUser()
        requestPermissions(context, mediaType!!, object : PermissionCallback() {
            override fun onGranted() {
                TUICallEngine.createInstance(context).joinInGroupCall(roomId, groupId, mediaType,
                    object : TUICommonDefine.Callback {
                        override fun onSuccess() {
                            TUICallState.instance.groupId.set(groupId)
                            TUICallState.instance.roomId.set(roomId)
                            TUICallState.instance.mediaType.set(mediaType)
                            TUICallState.instance.scene.set(TUICallDefine.Scene.GROUP_CALL)
                            TUICallState.instance.selfUser.get().callRole.set(TUICallDefine.Role.Called)
                            TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.Accept)

                            TUICore.notifyEvent(
                                Constants.EVENT_TUICALLKIT_CHANGED,
                                Constants.EVENT_START_ACTIVITY,
                                HashMap()
                            )
                        }

                        override fun onError(errCode: Int, errMsg: String) {
                            val errMessage = convertErrorMsg(errCode, errMsg)
                            ToastUtil.toastLongMessage(errMessage)
                        }
                    })
            }

            override fun onDenied() {
                Logger.error(TAG, "requestPermissions failed")
            }
        })
    }

    fun join(callId: String?, callback: TUICommonDefine.Callback?) {
        if (callId.isNullOrEmpty()) {
            Logger.error(TAG, "join failed, callId is empty")
            callback?.onError(TUICallDefine.ERROR_PARAM_INVALID, "join failed, callId is empty")
            return
        }
        Logger.info(TAG, "join callId: $callId")
        TUICallState.instance.selfUser.get().avatar.set(TUILogin.getFaceUrl())
        TUICallState.instance.selfUser.get().nickname.set(TUILogin.getNickName())
        TUICallState.instance.selfUser.get().id = TUILogin.getLoginUser()
        requestPermissions(context, TUICallDefine.MediaType.Audio, object : PermissionCallback() {
            override fun onGranted() {
                TUICallEngine.createInstance(context).join(callId, object : TUICommonDefine.Callback {
                    override fun onSuccess() {
                        TUICallState.instance.scene.set(TUICallDefine.Scene.GROUP_CALL)
                        TUICallState.instance.selfUser.get().callRole.set(TUICallDefine.Role.Called)
                        TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.Accept)

                        TUICore.notifyEvent(
                            Constants.EVENT_TUICALLKIT_CHANGED, Constants.EVENT_START_ACTIVITY, HashMap()
                        )
                        callback?.onSuccess()
                    }

                    override fun onError(errCode: Int, errMsg: String) {
                        val errMessage = convertErrorMsg(errCode, errMsg)
                        ToastUtil.toastLongMessage(errMessage)
                        callback?.onError(errCode, errMsg)
                    }
                })
            }

            override fun onDenied() {
                Logger.error(TAG, "join failed, requestPermissions denied")
                callback?.onError(TUICallDefine.ERROR_PERMISSION_DENIED, "request Permissions failed")
            }
        })
    }

    fun accept(callback: TUICommonDefine.Callback?) {
        TUICallEngine.createInstance(context).accept(object : TUICommonDefine.Callback {
            override fun onSuccess() {
                if (TUICallState.instance.selfUser.get().callStatus.get() != TUICallDefine.Status.Accept) {
                    TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.Accept)
                }
                callback?.onSuccess()
            }

            override fun onError(errCode: Int, errMsg: String) {
                TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.None)
                callback?.onError(errCode, errMsg)
            }
        })
    }

    fun reject(callback: TUICommonDefine.Callback?) {
        TUICallEngine.createInstance(context).reject(object : TUICommonDefine.Callback {
            override fun onSuccess() {
                TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.None)
                callback?.onSuccess()
            }

            override fun onError(errCode: Int, errMsg: String) {
                TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.None)
                callback?.onError(errCode, errMsg)
            }
        })
    }

    fun hangup(callback: TUICommonDefine.Callback?) {
        TUICallEngine.createInstance(context).hangup(object : TUICommonDefine.Callback {
            override fun onSuccess() {
                TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.None)
                callback?.onSuccess()
            }

            override fun onError(errCode: Int, errMsg: String) {
                TUICallState.instance.selfUser.get().callStatus.set(TUICallDefine.Status.None)
                callback?.onError(errCode, errMsg)
            }
        })
    }

    fun openCamera(camera: TUICommonDefine.Camera?, videoView: TUIVideoView?, callback: TUICommonDefine.Callback?) {
        if (TUICore.getService(TUIConstants.USBCamera.SERVICE_NAME) != null) {
            Logger.info(TAG, "open usb camera")
            val map = HashMap<String, Any?>()
            map[TUIConstants.USBCamera.PARAM_TX_CLOUD_VIEW] = videoView
            TUICore.notifyEvent(TUIConstants.USBCamera.KEY_USB_CAMERA, TUIConstants.USBCamera.SUB_KEY_OPEN_CAMERA, map)
            return
        }

        PermissionRequest.requestCameraPermission(context, object : PermissionCallback() {
            override fun onGranted() {

                TUICallEngine.createInstance(context).openCamera(camera, videoView, object : TUICommonDefine.Callback {
                    override fun onSuccess() {
                        val status: TUICallDefine.Status = TUICallState.instance.selfUser.get().callStatus.get()
                        if (TUICallDefine.Status.None != status) {
                            val camera: TUICommonDefine.Camera = TUICallState.instance.isFrontCamera.get()
                            TUICallState.instance.isCameraOpen.set(true)
                            TUICallState.instance.isFrontCamera.set(camera)
                            TUICallState.instance.selfUser.get().videoAvailable.set(true)
                        }
                        callback?.onSuccess()
                    }

                    override fun onError(errCode: Int, errMsg: String) {
                        callback?.onError(errCode, errMsg)
                    }
                })
            }

            override fun onDenied() {
                Logger.warn(TAG, "refused to access to the camera")
            }
        })
    }

    fun closeCamera() {
        TUICallEngine.createInstance(context).closeCamera()
        TUICallState.instance.isCameraOpen.set(false)
        TUICallState.instance.selfUser.get().videoAvailable.set(false)

        if (TUICore.getService(TUIConstants.USBCamera.SERVICE_NAME) != null) {
            TUICore.notifyEvent(
                TUIConstants.USBCamera.KEY_USB_CAMERA, TUIConstants.USBCamera.SUB_KEY_CLOSE_CAMERA, null
            )
        }
    }

    fun switchCamera(camera: TUICommonDefine.Camera) {
        TUICallEngine.createInstance(context).switchCamera(camera)
        TUICallState.instance.isFrontCamera.set(camera)
    }

    fun openMicrophone(callback: TUICommonDefine.Callback?) {
        TUICallEngine.createInstance(context).openMicrophone(object : TUICommonDefine.Callback {
            override fun onSuccess() {
                val status: TUICallDefine.Status = TUICallState.instance.selfUser.get().callStatus.get()
                if (TUICallDefine.Status.None != status) {
                    TUICallState.instance.isMicrophoneMute.set(false)
                    TUICallState.instance.selfUser.get().audioAvailable.set(true)
                }
                callback?.onSuccess()
            }

            override fun onError(errCode: Int, errMsg: String) {
                callback?.onError(errCode, errMsg)
            }
        })
    }

    fun closeMicrophone() {
        TUICallEngine.createInstance(context).closeMicrophone()
        TUICallState.instance.isMicrophoneMute.set(true)
        TUICallState.instance.selfUser.get().audioAvailable.set(false)
    }

    fun selectAudioPlaybackDevice(device: TUICommonDefine.AudioPlaybackDevice?) {
        TUICallEngine.createInstance(context).selectAudioPlaybackDevice(device)
        TUICallState.instance.audioPlayoutDevice.set(device)
    }

    fun startRemoteView(userId: String?, videoView: TUIVideoView?, callback: TUICommonDefine.PlayCallback?) {
        TUICallEngine.createInstance(context).startRemoteView(userId, videoView, callback)
    }

    fun stopRemoteView(userId: String?) {
        TUICallEngine.createInstance(context).stopRemoteView(userId)
    }

    fun enableFloatWindow(enable: Boolean) {
        TUICallState.instance.enableFloatWindow = enable
    }

    fun enableMuteMode(enable: Boolean) {
        TUICallState.instance.enableMuteMode = enable
        SPUtils.getInstance(CallingBellFeature.PROFILE_TUICALLKIT).put(CallingBellFeature.PROFILE_MUTE_MODE, enable)
    }

    fun setBlurBackground(enable: Boolean) {
        val level = if (enable) BLUR_LEVEL_HIGH else BLUR_LEVEL_CLOSE
        TUICallState.instance.enableBlurBackground.set(enable)

        TUICallEngine.createInstance(context).setBlurBackground(level, object : TUICommonDefine.Callback {
            override fun onSuccess() {
            }

            override fun onError(errCode: Int, errMsg: String?) {
                Logger.error(TAG, "setBlurBackground failed, errCode: $errCode, errMsg: $errMsg")
                TUICallState.instance.enableBlurBackground.set(false)
            }
        })
    }

    fun inviteUser(userIdList: List<String?>?) {
        val params = CallParams()
        params.offlinePushInfo = OfflinePushInfoConfig.createOfflinePushInfo(context)
        params.timeout = Constants.SIGNALING_MAX_TIME
        TUICallEngine.createInstance(context)
            .inviteUser(userIdList, params, object : ValueCallback<List<String>> {
                override fun onSuccess(data: List<String>) {
                    val userList = data
                    Logger.info(TAG, "inviteUsersToGroupCall success, list:$userList")
                    UserInfoUtils.getUserListInfo(userList, object : ValueCallback<List<User>?> {
                        override fun onSuccess(data: List<User>?) {
                            if (data.isNullOrEmpty()) {
                                Logger.error(TAG, "getUsersInfo onSuccess list = null")
                                return
                            }
                            for (info in data) {
                                info.callStatus.set(TUICallDefine.Status.Waiting)
                                TUICallState.instance.remoteUserList.add(info)
                            }
                        }

                        override fun onError(errCode: Int, errMsg: String?) {
                            Logger.error(TAG, "getUsersInfo onError errorCode = $errCode , errorMsg = $errMsg")
                        }
                    })
                }

                override fun onError(errCode: Int, errMsg: String) {}
            })
    }

    private fun initAudioPlayDevice() {
        if (TUICallDefine.MediaType.Video == TUICallState.instance.mediaType.get()) {
            selectAudioPlaybackDevice(TUICommonDefine.AudioPlaybackDevice.Speakerphone)
            TUICallState.instance.isCameraOpen.set(true)
        } else {
            selectAudioPlaybackDevice(TUICommonDefine.AudioPlaybackDevice.Earpiece)
            TUICallState.instance.isCameraOpen.set(false)
        }
    }

    private fun getCommonErrorMap(): Map<Int, String> {
        val map = HashMap<Int, String>()
        map[TUICallDefine.ERROR_PACKAGE_NOT_PURCHASED] = context.getString(R.string.tuicallkit_package_not_purchased)
        map[TUICallDefine.ERROR_PACKAGE_NOT_SUPPORTED] = context.getString(R.string.tuicallkit_package_not_support)
        map[TUICallDefine.ERROR_INIT_FAIL] = context.getString(R.string.tuicallkit_error_invalid_login)
        map[TUICallDefine.ERROR_PARAM_INVALID] = context.getString(R.string.tuicallkit_error_parameter_invalid)
        map[TUICallDefine.ERROR_REQUEST_REFUSED] = context.getString(R.string.tuicallkit_error_request_refused)
        map[TUICallDefine.ERROR_REQUEST_REPEATED] = context.getString(R.string.tuicallkit_error_request_repeated)
        map[TUICallDefine.ERROR_SCENE_NOT_SUPPORTED] = context.getString(R.string.tuicallkit_error_scene_not_support)
        return map
    }

    private fun convertErrorMsg(errorCode: Int, msg: String): String {
        if (errorCode == BaseConstants.ERR_SVR_MSG_IN_PEER_BLACKLIST) {
            return context.getString(R.string.tuicallkit_error_in_peer_blacklist)
        }

        val commonErrorMap = getCommonErrorMap()
        if (commonErrorMap.containsKey(errorCode)) {
            return commonErrorMap[errorCode]!!
        }

        return ErrorMessageConverter.convertIMError(errorCode, msg)
    }

    fun reportOnlineLog(data: Map<String, Any>) {
        try {
            val map: JSONObject = JSONObject(data)
            map.put("version", TUICallDefine.VERSION)
            map.put("platform", "android")
            map.put("framework", "native")
            map.put("sdk_app_id", TUILogin.getSdkAppId())

            val params = JSONObject()
            params.put("level", 1)
            params.put("msg", map.toString())
            params.put("more_msg", "TUICallKit")

            val jsonObject = JSONObject()
            jsonObject.put("api", "reportOnlineLog")
            jsonObject.put("params", params)

            TUICallEngine.createInstance(context).trtcCloudInstance.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}