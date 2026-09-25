package com.vu.lecturehub.data.model

import java.io.Serializable

enum class ResourceType {
    HANDOUT,
    LINK,
    TOOL
}

data class ResourceItem(
    val id: String,
    val title: String,
    val description: String,
    val type: ResourceType,
    val targetUrl: String? = null,
    val badgeText: String? = null,
    val iconRes: Int = 0
) : Serializable
