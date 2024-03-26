package com.hyphenate.chatdemo.ui.group

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.chatdemo.viewmodel.ProfileInfoViewModel
import com.hyphenate.easeui.common.ChatGroup
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.extensions.showToast
import com.hyphenate.easeui.feature.group.EaseCreateGroupActivity
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CreateGroupActivity: EaseCreateGroupActivity() {
    private val profileViewModel by lazy { ViewModelProvider(this)[ProfileInfoViewModel::class.java] }

    override fun createGroupSuccess(group: ChatGroup) {
        lifecycleScope.launch {
            profileViewModel.getGroupAvatar(group.groupId)
                .catchChatException { e ->
                    showToast(e.description)
                }
                .onStart {
                    showLoading(true)
                }
                .onCompletion { dismissLoading() }
                .collect {
                    super.createGroupSuccess(group)
                }
        }
    }
}