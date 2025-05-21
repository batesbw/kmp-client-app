package com.mass.client.core.model

import kotlinx.serialization.Serializable

// Based on frontend/src/plugins/api/interfaces.ts

@Serializable
data class AudioFormat(
    val content_type: String, // Was ContentType enum, using String for simplicity now
    val codec_type: String? = null, // Similar to content_type
    val sample_rate: Int,
    val bit_depth: Int,
    val channels: Int,
    val output_format_str: String? = null, // Optional, as it might not always be present
    val bit_rate: Int? = null // Optional, as it might not always be present
)

@Serializable
data class StreamDetails(
    val provider: String,
    val item_id: String,
    val audio_format: AudioFormat,
    val media_type: MediaType, // Enum
    val stream_title: String? = null,
    val duration: Int? = null, // seconds
    val queue_id: String? = null,
    // Skipping loudness and DSP details for initial model simplicity, can be added later
    // val loudness: Float? = null,
    // val dsp: Map<String, DspDetails>? = null 
) 