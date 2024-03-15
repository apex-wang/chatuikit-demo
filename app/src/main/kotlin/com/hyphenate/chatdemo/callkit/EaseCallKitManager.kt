package com.hyphenate.chatdemo.callkit

import android.content.Context
import android.text.SpannableStringBuilder
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.utils.ToastUtils
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.callkit.extensions.getStringOrNull
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.base.EaseCallEndReason
import com.hyphenate.easecallkit.base.EaseCallKitConfig
import com.hyphenate.easecallkit.base.EaseCallKitListener
import com.hyphenate.easecallkit.base.EaseCallKitTokenCallback
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easecallkit.base.EaseGetUserAccountCallback
import com.hyphenate.easecallkit.base.EaseUserAccount
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatHttpClientManagerBuilder
import com.hyphenate.easeui.common.ChatHttpResponse
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.bus.EaseFlowBus
import com.hyphenate.easeui.common.extensions.mainScope
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.provider.getSyncUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.TimeZone

object EaseCallKitManager {

    private const val TAG = "EaseCallKitManager"
    private const val FETCH_TOKEN_URL = com.hyphenate.chatdemo.BuildConfig.APP_SERVER_PROTOCOL + "://" + com.hyphenate.chatdemo.BuildConfig.APP_SERVER_DOMAIN +
            com.hyphenate.chatdemo.BuildConfig.APP_RTC_TOKEN_URL
    private const val FETCH_USER_MAPPER = com.hyphenate.chatdemo.BuildConfig.APP_SERVER_PROTOCOL + "://" + com.hyphenate.chatdemo.BuildConfig.APP_SERVER_DOMAIN +
            com.hyphenate.chatdemo.BuildConfig.APP_RTC_CHANNEL_MAPPER_URL
    private const val PARAM_USER = "user"
    private const val PARAM_CHANNEL_NAME = "channelName"
    private const val PARAM_USER_APPKEY = "appkey"
    private const val RESULT_PARAM_TOKEN = "accessToken"
    private const val RESULT_PARAM_UID = "agoraUid"
    private const val RESULT_PARAM_RESULT = "result"
    private const val KEY_GROUPID = "groupId"

    /**
     * If multiple call, should set groupId.
     */
    var currentCallGroupId: String? = null

    private val callKitListener by lazy { object :EaseCallKitListener {
        override fun onInviteUsers(context: Context?, users: Array<out String>?, ext: JSONObject?) {
            currentCallGroupId = ext?.getStringOrNull(KEY_GROUPID)
        }

        override fun onEndCallWithReason(
            callType: EaseCallType?,
            channelName: String?,
            reason: EaseCallEndReason?,
            callTime: Long
        ) {
            ChatLog.d(
                TAG,
                "onEndCallWithReason" + (callType?.name
                    ?: " callType is null ") + " reason:" + reason + " time:" + callTime
            )
            val formatter = SimpleDateFormat("mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            var callString: String = DemoHelper.getInstance().context.getString(R.string.call_duration, formatter.format(callTime))
            ToastUtils.showToast(callString)
        }

        override fun onReceivedCall(callType: EaseCallType?, userId: String?, ext: JSONObject?) {
            ChatLog.e(TAG, "onReceivedCall: $callType, userId: $userId")
            // Can get groupId from ext
            ext?.getStringOrNull(KEY_GROUPID)?.let { groupId ->
                currentCallGroupId = groupId
                CallUserInfo(userId).getUserInfo(groupId).parse().apply {
                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
                }
            } ?: kotlin.run {
                currentCallGroupId = null
                CallUserInfo(userId).apply {
                    EaseIM.getUserProvider()?.getSyncUser(userId)?.let { user ->
                        this.nickName = user.getRemarkOrName()
                        this.headImage = user.avatar
                    }
                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this.parse())
                } // Single call
            }
        }

        override fun onCallError(
            type: EaseCallKit.EaseCallError?,
            errorCode: Int,
            description: String?
        ) {
            ChatLog.e(TAG, "onCallError: $type, errorCode: $errorCode, description: $description")
        }

        override fun onInViteCallMessageSent() {
            if (ChatClient.getInstance().options.isIncludeSendMessageInMessageListener.not()) {
                EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.ADD + EaseEvent.TYPE.MESSAGE)
                    .post(DemoHelper.getInstance().context.mainScope(), EaseEvent(DemoConstant.CALL_INVITE_MESSAGE, EaseEvent.TYPE.MESSAGE))
            }
        }

        override fun onGenerateToken(
            userId: String?,
            channelName: String?,
            agoraAppId: String?,
            callback: EaseCallKitTokenCallback?
        ) {
            SpannableStringBuilder(FETCH_TOKEN_URL).apply {
                append("$channelName/$PARAM_USER/$userId")
                getRtcToken(this.toString(), callback)
            }
        }

        override fun onRemoteUserJoinChannel(
            channelName: String?,
            userName: String?,
            uid: Int,
            callback: EaseGetUserAccountCallback?
        ) {
            // Only multi call callback this method
            if (userName.isNullOrEmpty()) {
                SpannableStringBuilder(FETCH_USER_MAPPER).apply {
                    append("?$PARAM_CHANNEL_NAME=$channelName")
                    getAllUsersByUid(this.toString(), callback)
                }
            } else {
                // Set user info to call kit.
                CallUserInfo(userName).getUserInfo(currentCallGroupId).parse().apply {
                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
                }
                callback?.onUserAccount(listOf(EaseUserAccount(uid, userName)))
            }
        }

    } }

    fun init(context: Context) {
        EaseCallKitConfig().apply {
            // Set call timeout.
            callTimeOut = 30 * 1000
            // Set RTC appId.
            agoraAppId = com.hyphenate.chatdemo.BuildConfig.RTC_APPID
            // Set whether token verification is required.
            isEnableRTCToken = true
            EaseCallKit.getInstance().init(context, this)
        }
        EaseCallKit.getInstance().setCallKitListener(callKitListener)
    }

    /**
     * Get rtc token from server.
     */
    private fun getRtcToken(tokenUrl: String, callback: EaseCallKitTokenCallback?) {
        executeGetRequest(tokenUrl) {
            it?.let { response ->
                if (response.code == 200) {
                    response.content?.let { body ->
                        try {
                            val result = JSONObject(body)
                            val token = result.getString(RESULT_PARAM_TOKEN)
                            val uid = result.getInt(RESULT_PARAM_UID)
                            callback?.onSetToken(token, uid)
                        } catch (e: Exception) {
                            e.stackTrace
                            callback?.onGetTokenError(ChatError.GENERAL_ERROR, e.message)
                        }
                    }
                } else {
                    callback?.onGetTokenError(response.code, response.content)
                }
            } ?: kotlin.run {
                callback?.onSetToken(null, 0)
            }
        }
    }

    private fun getAllUsersByUid(url: String, callback: EaseGetUserAccountCallback?) {
        executeGetRequest(url) {
            it?.let { response ->
                if (response.code == 200) {
                    response.content?.let { body ->
                        try {
                            val result = JSONObject(body)
                            val userList = result.getJSONObject(RESULT_PARAM_RESULT)
                            val userAccountList = mutableListOf<EaseUserAccount>()
                            userList.keys().forEach { uIdStr ->
                                val userId = userList.optString(uIdStr)
                                // Set user info to call kit.
                                CallUserInfo(userId).getUserInfo(currentCallGroupId).parse().apply {
                                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
                                }
                                userAccountList.add(EaseUserAccount(uIdStr.toInt(), userId))
                            }
                            callback?.onUserAccount(userAccountList)
                        } catch (e: Exception) {
                            e.stackTrace
                            callback?.onSetUserAccountError(ChatError.GENERAL_ERROR, e.message)
                        }
                    }
                } else {
                    callback?.onSetUserAccountError(response.code, response.content)
                }
            } ?: kotlin.run {
                callback?.onSetUserAccountError(ChatError.GENERAL_ERROR, "response is null")
            }
        }
    }

    /**
     * Base get request.
     */
    private fun executeGetRequest(url: String, callback: (ChatHttpResponse?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            ChatHttpClientManagerBuilder()
                .get()
                .setUrl(url)
                .withToken(true)
                .execute()?.let { response ->
                    callback(response)
                } ?: kotlin.run {
                callback(null)
            }
        }
    }

}