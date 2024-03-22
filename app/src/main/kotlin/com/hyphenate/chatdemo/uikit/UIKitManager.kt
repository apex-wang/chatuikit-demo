package com.hyphenate.chatdemo.uikit

import android.content.Context
import android.content.Intent
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.ui.chat.ChatActivity
import com.hyphenate.chatdemo.ui.contact.ChatContactDetailActivity
import com.hyphenate.chatdemo.ui.group.ChatGroupDetailActivity
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.extensions.toProfile
import com.hyphenate.easeui.common.impl.OnValueSuccess
import com.hyphenate.easeui.feature.chat.activities.EaseChatActivity
import com.hyphenate.easeui.feature.contact.EaseContactDetailsActivity
import com.hyphenate.easeui.feature.group.EaseGroupDetailActivity
import com.hyphenate.easeui.model.EaseGroupProfile
import com.hyphenate.easeui.model.EaseProfile
import com.hyphenate.easeui.provider.EaseCustomActivityRoute
import com.hyphenate.easeui.provider.EaseGroupProfileProvider
import com.hyphenate.easeui.provider.EaseSettingsProvider
import com.hyphenate.easeui.provider.EaseUserProfileProvider

object UIKitManager {

    fun addUIKitSettings(context: Context) {
        addProviders(context)
        setUIKitConfigs(context)
    }

    fun addProviders(context: Context) {
        EaseIM.setUserProfileProvider(object : EaseUserProfileProvider {
            override fun getUser(userId: String?): EaseProfile? {
                return DemoHelper.getInstance().getDataModel().getUser(userId)?.toProfile()
            }

            override fun fetchUsers(
                userIds: List<String>,
                onValueSuccess: OnValueSuccess<List<EaseProfile>>
            ) {
                // fetch users from server and call call onValueSuccess.onSuccess(users) after successfully getting users
            }
        })
            .setGroupProfileProvider(object : EaseGroupProfileProvider {

                override fun getGroup(id: String?): EaseGroupProfile? {
                    return null
                }

                override fun fetchGroups(
                    groupIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<EaseGroupProfile>>
                ) {

                }
            })
            .setSettingsProvider(object : EaseSettingsProvider {
                override fun isMsgNotifyAllowed(message: ChatMessage?): Boolean {
                    return true
                }

                override fun isMsgSoundAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override fun isMsgVibrateAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override val isSpeakerOpened: Boolean
                    get() = true

            })
            .setCustomActivityRoute(object : EaseCustomActivityRoute {
                override fun getActivityRoute(intent: Intent): Intent? {
                    intent.component?.className?.let {
                        when(it) {
                            EaseChatActivity::class.java.name -> {
                                intent.setClass(context, ChatActivity::class.java)
                            }
                            EaseGroupDetailActivity::class.java.name -> {
                                intent.setClass(context, ChatGroupDetailActivity::class.java)
                            }
                            EaseContactDetailsActivity::class.java.name -> {
                                intent.setClass(context, ChatContactDetailActivity::class.java)
                            }

                            else -> {
                                return intent
                            }
                        }
                    }
                    return intent
                }

            })
    }

    fun setUIKitConfigs(context: Context) {
        EaseIM.getConfig()?.avatarConfig?.let {
            it.avatarShape = com.hyphenate.easeui.widget.EaseImageView.ShapeType.RECTANGLE
            it.avatarRadius = context.resources.getDimensionPixelSize(com.hyphenate.easeui.R.dimen.ease_corner_extra_small)
        }
    }
}