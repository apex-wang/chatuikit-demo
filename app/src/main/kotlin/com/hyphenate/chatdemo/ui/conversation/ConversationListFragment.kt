package com.hyphenate.chatdemo.ui.conversation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.EaseConstant
import com.hyphenate.easeui.common.bus.EaseFlowBus
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.common.utils.EasePresenceUtil
import com.hyphenate.easeui.configs.setAvatarStyle
import com.hyphenate.easeui.configs.setStatusStyle
import com.hyphenate.easeui.feature.conversation.EaseConversationListFragment
import com.hyphenate.easeui.model.EaseEvent

class ConversationListFragment: EaseConversationListFragment() {

    override fun initData() {
        super.initData()
        initEventBus()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding?.titleConversations?.let {
            EaseIM.getConfig()?.avatarConfig?.setAvatarStyle(it.getLogoView())
            EaseIM.getConfig()?.avatarConfig?.setStatusStyle(it.getStatusView(),4,
                ContextCompat.getColor(mContext, com.hyphenate.easeui.R.color.ease_color_background))
            updateProfile()
            it.setTitleEndDrawable(R.drawable.conversation_title)
        }
    }

    private fun initEventBus() {
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT).register(this) {
            if (it.isContactChange && it.event == DemoConstant.EVENT_UPDATE_SELF) {
                updateProfile()
            }
        }
    }

    private fun updateProfile(){
        binding?.titleConversations?.let { titlebar->
            EaseIM.getCurrentUser()?.let { profile->
                val presence = EaseIM.getUserPresence(profile.id)
                presence?.let {
                    val logoStatus = EasePresenceUtil.getPresenceIcon(mContext,it)
                    val subtitle = EasePresenceUtil.getPresenceString(mContext,it)
                    titlebar.setLogoStatusMargin(end = -1, bottom = -1)
                    titlebar.setLogoStatus(logoStatus)
                    titlebar.setSubtitle(subtitle)
                    titlebar.getStatusView().visibility = View.VISIBLE
                    titlebar.setLogoStatusSize(resources.getDimensionPixelSize(com.hyphenate.easeui.R.dimen.ease_title_bar_status_icon_size))
                }
                titlebar.setLogo(profile.avatar, com.hyphenate.easeui.R.drawable.ease_default_avatar, 32.dpToPx(mContext))
                val layoutParams = titlebar.getLogoView()?.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.marginStart = 12.dpToPx(mContext)
                titlebar.getTitleView().let { text ->
                    text.text = ""
                }
            }
        }
    }

}