package com.hyphenate.chatdemo.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hyphenate.easeui.model.EaseProfile

@Entity
data class DemoUser(
    @PrimaryKey val userId: String,
    val name: String?,
    val avatar: String?,
    val remark: String?
)

/**
 * Convert the user data to the profile data.
 */
internal fun DemoUser.parse() = EaseProfile(userId, name, avatar, remark)
