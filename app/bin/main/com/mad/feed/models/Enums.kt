package com.mad.feed.models

import kotlinx.serialization.Serializable

@Serializable
enum class ReactionType {
    UNSPECIFIED,
    LIKE,
    LOVE,
    HAHA,
    WOW,
    SAD,
    ANGRY
}

@Serializable
enum class AttachmentType {
    UNSPECIFIED,
    IMAGE,
    VIDEO
}
