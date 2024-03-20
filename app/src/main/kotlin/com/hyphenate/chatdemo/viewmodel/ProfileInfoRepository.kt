package com.hyphenate.chatdemo.viewmodel

import android.util.Log
import com.hyphenate.EMCallBack
import com.hyphenate.chatdemo.BuildConfig
import com.hyphenate.cloud.HttpCallback
import com.hyphenate.cloud.HttpClientManager
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatHttpClientManagerBuilder
import com.hyphenate.easeui.common.ChatValueCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProfileInfoRepository: BaseRepository()  {

    companion object {
        private const val UPLOAD_AVATAR_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_USER
        private const val GROUP_AVATAR_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_GROUP
    }

    suspend fun setUserRemark(username:String,remark:String): Int =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                ChatClient.getInstance().contactManager().asyncSetContactRemark(username,remark,object :
                    EMCallBack{
                    override fun onSuccess() {
                        continuation.resume(ChatError.EM_NO_ERROR)
                    }

                    override fun onError(code: Int, error: String?) {
                        continuation.resumeWithException(ChatException(code, error))
                    }
                })
            }
        }

    suspend fun getGroupAvatar(groupId:String?): String =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                getGroupAvatarFromServer(groupId,object : ChatValueCallback<String>{
                    override fun onSuccess(value: String?) {
                        value?.let {
                            continuation.resume(it)
                        }
                    }

                    override fun onError(code: Int, errorMsg: String?) {
                        continuation.resumeWithException(ChatException(code, errorMsg))
                    }
                })
            }
        }

    /**
     * 上传头像
     * @return
     */
    suspend fun uploadAvatar(filePath: String?): String =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                uploadAvatarToAppServer(filePath,object : ChatValueCallback<String>{
                    override fun onSuccess(value: String?) {
                        value?.let {
                            continuation.resume(it)
                        }
                    }

                    override fun onError(error: Int, errorMsg: String?) {
                        continuation.resumeWithException(ChatException(error, errorMsg))
                    }
                })
            }
        }

    private fun uploadAvatarToAppServer(
        filePath: String?,
        callBack: ChatValueCallback<String>
    ){
        try {
            Log.e("apex","uploadAvatarToAppServer $filePath")
            if (filePath.isNullOrEmpty()){
                callBack.onError(ChatError.INVALID_URL," invalid url.")
                return
            }
                ChatHttpClientManagerBuilder()
                    .uploadFile(filePath)
                    .setParam("file",filePath)
                    .setUrl(UPLOAD_AVATAR_URL+"/${ChatClient.getInstance().currentUser}"+BuildConfig.APP_UPLOAD_AVATAR)
                    .execute(object : HttpCallback{
                        override fun onSuccess(result: String?) {
                            result?.let {
                                val jsonObject = JSONObject(it)
                                val url = jsonObject.getString("avatarUrl")
                                callBack.onSuccess(url)
                            }
                        }

                        override fun onError(code: Int, msg: String?) {
                            callBack.onError(code,msg)
                        }

                        override fun onProgress(total: Long, pos: Long) {

                        }
                    })
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

    private fun getGroupAvatarFromServer(
        groupId: String?,
        callBack: ChatValueCallback<String>
    ){
        try {
            if (groupId.isNullOrEmpty()){
                callBack.onError(ChatError.GROUP_INVALID_ID, "The group ID is incorrect")
                return
            }
            val headers: MutableMap<String, String> = HashMap()
            headers["Content-Type"] = "application/json"
            val url: String = GROUP_AVATAR_URL + "/$groupId" +BuildConfig.APP_GROUP_AVATAR
            val response = HttpClientManager.httpExecute(
                url,
                headers, "",
                HttpClientManager.Method_GET
            )
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                val `object` = JSONObject(responseInfo)
                val avatarUrl = `object`.getString("avatarUrl")
                callBack.onSuccess(avatarUrl)
            } else {
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    var errorInfo: String? = null
                    try {
                        val responseObject = JSONObject(responseInfo)
                        errorInfo = responseObject.getString("errorInfo")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    callBack.onError(code, errorInfo)
                } else {
                    callBack.onError(code, responseInfo)
                }
            }
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }
}