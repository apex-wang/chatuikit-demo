package com.hyphenate.chatdemo.ui.contact

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.hyphenate.chatdemo.R
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.callkit.CallKitManager
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.room.entity.parse
import com.hyphenate.chatdemo.common.room.extensions.parseToDbBean
import com.hyphenate.chatdemo.viewmodel.ProfileInfoViewModel
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatUserInfoType
import com.hyphenate.easeui.common.bus.EaseFlowBus
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.extensions.showToast
import com.hyphenate.easeui.feature.contact.EaseContactDetailsActivity
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.model.EaseMenuItem
import com.hyphenate.easeui.provider.getSyncUser
import com.hyphenate.easeui.widget.EaseArrowItemView
import kotlinx.coroutines.launch


class ChatContactDetailActivity:EaseContactDetailsActivity() {
    private lateinit var model: ProfileInfoViewModel
    private val remarkItem: EaseArrowItemView by lazy { findViewById(R.id.item_remark) }

    companion object {
        private const val REQUEST_UPDATE_REMARK = 100
        private const val RESULT_UPDATE_REMARK = "result_update_remark"
    }

    private val launcherToUpdateRemark: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, REQUEST_UPDATE_REMARK) }

    override fun initView() {
        super.initView()
        model = ViewModelProvider(this)[ProfileInfoViewModel::class.java]
        user?.let {
            val remark = model.fetchLocalUserRemark(it.userId)
            remarkItem.setContent(remark)
        }
    }

    override fun initListener() {
        super.initListener()
        remarkItem.setOnClickListener{
            user?.let {
                launcherToUpdateRemark.launch(ChatContactRemarkActivity.createIntent(mContext,it.userId))
            }
        }
        binding.tvNumber
    }

    override fun initData() {
        super.initData()
        lifecycleScope.launch {
            user?.let { user->
                model.fetchUserInfoAttribute(listOf(user.userId), listOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                    .catchChatException {
                        ChatLog.e("ContactDetail", "fetchUserInfoAttribute error: ${it.description}")
                    }
                    .collect {
                        it[user.userId]?.parseToDbBean()?.let {u->
                            u.parse().apply {
                                remark = ChatClient.getInstance().contactManager().fetchContactFromLocal(id).remark
                                EaseIM.updateUsersInfo(mutableListOf(this))
                                DemoHelper.getInstance().getDataModel().insertUser(this)
                            }
                            notifyUpdateRemarkEvent()
                        }
                    }
            }
        }
    }

    override fun getDetailItem(): MutableList<EaseMenuItem>? {
        val list = super.getDetailItem()
        val audioItem = EaseMenuItem(
            title = getString(R.string.detail_item_audio),
            resourceId = R.drawable.ease_phone_pick,
            menuId = R.id.contact_item_audio_call,
            titleColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary),
            order = 2
        )

        val videoItem = EaseMenuItem(
            title = getString(R.string.detail_item_video),
            resourceId = R.drawable.ease_video_camera,
            menuId = R.id.contact_item_video_call,
            titleColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary),
            order = 3
        )
        list?.add(audioItem)
        list?.add(videoItem)
        return list
    }

    override fun onMenuItemClick(item: EaseMenuItem?, position: Int): Boolean {
        item?.let {
            when(item.menuId){
                R.id.contact_item_audio_call -> {
                    CallKitManager.startSingleAudioCall(user?.userId)
                    return true
                }
                R.id.contact_item_video_call -> {
                    CallKitManager.startSingleVideoCall(user?.userId)
                    return true
                }
                else -> {
                    return super.onMenuItemClick(item, position)
                }
            }
        }
        return false
    }

    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            when (requestCode) {
                REQUEST_UPDATE_REMARK ->{
                    data?.let {
                        if (it.hasExtra(RESULT_UPDATE_REMARK)){
                            remarkItem.setContent(it.getStringExtra(RESULT_UPDATE_REMARK))
                            notifyUpdateRemarkEvent()
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun notifyUpdateRemarkEvent() {
        DemoHelper.getInstance().getDataModel().updateUserCache(user?.userId)
        updateInfo()
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX)
            .post(lifecycleScope, EaseEvent(DemoConstant.EVENT_UPDATE_USER_SUFFIX, EaseEvent.TYPE.CONTACT, user?.userId))
    }

    private fun updateInfo(){
        EaseIM.getUserProvider()?.getSyncUser(user?.userId)?.let {
            binding.epPresence.setPresenceData(it)
            binding.tvName.text = it.getRemarkOrName()
        }
    }

    override fun onPrimaryClipChanged() {
        super.onPrimaryClipChanged()
        mContext.showToast(R.string.system_copy_success)
    }
}