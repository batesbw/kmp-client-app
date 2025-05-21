package com.mass.client.core.model

// Enums based on frontend/src/plugins/api/interfaces.ts

enum class MediaType {
    ARTIST,
    ALBUM,
    TRACK,
    PLAYLIST,
    RADIO,
    AUDIOBOOK,
    PODCAST,
    PODCAST_EPISODE,
    FOLDER,
    UNKNOWN
}

enum class AlbumType {
    ALBUM,
    SINGLE,
    COMPILATION,
    EP,
    UNKNOWN
}

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
    IDLE,
    PAUSED,
    PLAYING
}

enum class PlayerType {
    PLAYER,
    GROUP,
    STEREO_PAIR
}

// Simplified PlayerFeature for now, can be expanded
enum class PlayerFeature {
    POWER,
    VOLUME_SET,
    VOLUME_MUTE,
    PAUSE,
    SEEK,
    NEXT_PREVIOUS
    // Add other features as needed, e.g., PLAY_ANNOUNCEMENT, ENQUEUE, SELECT_SOURCE
}

// Simplified HidePlayerOption
enum class HidePlayerOption {
    NEVER,
    WHEN_OFF,
    WHEN_GROUP_ACTIVE,
    WHEN_SYNCED,
    WHEN_UNAVAILABLE,
    ALWAYS
} 