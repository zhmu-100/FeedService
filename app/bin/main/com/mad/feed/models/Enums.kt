package com.mad.feed.models

import kotlinx.serialization.Serializable

@Serializable
enum class ReactionType {
  REACTION_UNSPECIFIED,
  REACTION_LIKE,
  REACTION_LOVE,
  REACTION_HAHA,
  REACTION_WOW,
  REACTION_SAD,
  REACTION_ANGRY
}

@Serializable
enum class AttachmentType {
  ATTACHMENT_TYPE_UNSPECIFIED,
  ATTACHMENT_TYPE_IMAGE,
  ATTACHMENT_TYPE_VIDEO
}
