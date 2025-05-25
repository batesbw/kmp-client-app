package com.mass.client.core.model

import kotlinx.serialization.SerialName

// Enums based on frontend/src/plugins/api/interfaces.ts

// MediaType and AlbumType removed, defined in MediaItems.kt

// Note: ContentType in frontend is very extensive. We might only need a subset initially
// For now, a simplified version or just a String might be used in models needing it.
// We can expand this later if specific content type checking is needed in shared code.

enum class ImageType {
    THUMB,
    LANDSCAPE,
    FANART,
    LOGO,
    CLEARART,
    BANNER,
    CUTOUT,
    BACK,
    DISCART,
    OTHER
}

enum class QueueOption {
    PLAY,
    REPLACE,
    NEXT,
    REPLACE_NEXT,
    ADD
}

enum class RepeatMode {
    OFF, // no repeat at all
    ONE, // repeat current/single track
    ALL // repeat entire queue
}

enum class PlayerState {
    @SerialName("idle")
    IDLE,
    @SerialName("paused")
    PAUSED,
    @SerialName("playing")
    PLAYING
}

enum class PlayerType {
    @SerialName("player")
    PLAYER,
    @SerialName("group")
    GROUP,
    @SerialName("stereo_pair")
    STEREO_PAIR
}

// PlayerFeature enum based on frontend/src/plugins/api/interfaces.ts
enum class PlayerFeature {
    @SerialName("power")
    POWER,
    @SerialName("volume_set")
    VOLUME_SET,
    @SerialName("volume_mute")
    VOLUME_MUTE,
    @SerialName("pause")
    PAUSE,
    @SerialName("set_members")
    SET_MEMBERS,
    @SerialName("multi_device_dsp")
    MULTI_DEVICE_DSP,
    @SerialName("seek")
    SEEK,
    @SerialName("next_previous")
    NEXT_PREVIOUS,
    @SerialName("play_announcement")
    PLAY_ANNOUNCEMENT,
    @SerialName("enqueue")
    ENQUEUE,
    @SerialName("select_source")
    SELECT_SOURCE,
    @SerialName("gapless_playback")
    GAPLESS_PLAYBACK
}

// Simplified HidePlayerOption
enum class HidePlayerOption {
    @SerialName("never")
    NEVER,
    @SerialName("when_off")
    WHEN_OFF,
    @SerialName("when_group_active")
    WHEN_GROUP_ACTIVE,
    @SerialName("when_synced")
    WHEN_SYNCED,
    @SerialName("when_unavailable")
    WHEN_UNAVAILABLE,
    @SerialName("always")
    ALWAYS
} 