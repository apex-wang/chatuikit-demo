package com.hyphenate.chatdemo.common.room.extensions

import com.hyphenate.chatdemo.common.room.entity.DemoGroup
import com.hyphenate.chatdemo.common.room.entity.DemoUser
import com.hyphenate.easeui.common.ChatUserInfo
import com.hyphenate.easeui.model.EaseGroupProfile
import com.hyphenate.easeui.model.EaseProfile

internal fun EaseProfile.parseToDbBean() = DemoUser(id, name, avatar, remark)

internal fun EaseGroupProfile.parseToDbBean() = DemoGroup(id, name, avatar)


internal fun ChatUserInfo.parseToDbBean(): DemoUser {
    return DemoUser(
        userId = userId,
        name = nickname,
        avatar = avatarUrl
    )
}