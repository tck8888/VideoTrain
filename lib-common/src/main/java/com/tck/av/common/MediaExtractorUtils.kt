package com.tck.av.common

import android.media.MediaExtractor
import android.media.MediaFormat
import android.text.TextUtils

/**
 *<p>description:</p>
 *<p>created on: 2021/3/8 13:25</p>
 * @author tck
 * @version v1.0
 *
 */
object MediaExtractorUtils {


    fun findAudioFormat(mediaExtractor: MediaExtractor): Int {
        return findMediaFormatByMime(mediaExtractor, MediaFormat.MIMETYPE_AUDIO_AAC)
    }

    fun findVideoFormat(mediaExtractor: MediaExtractor): Int {
        return findMediaFormatByMime(mediaExtractor, MediaFormat.MIMETYPE_VIDEO_AVC)
    }


    private fun findMediaFormatByMime(
        mediaExtractor: MediaExtractor,
        mimeType: String
    ): Int {
        val trackCount = mediaExtractor.trackCount
        for (index in 0 until trackCount) {
            val mediaFormat = mediaExtractor.getTrackFormat(index)
            TLog.i("index:${index},mediaFormat:${mediaFormat}")
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime.isNullOrEmpty()) {
                continue
            }
            if (TextUtils.equals(mime, mimeType)) {
                return index
            }
        }
        return -1
    }


}