package com.tck.av.common

import android.media.MediaExtractor
import android.media.MediaFormat
import android.text.TextUtils
import java.io.ByteArrayOutputStream
import java.io.File

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

    fun getVideoFormatInfo(srcFile: File): MediaFormat? {
        if (!srcFile.exists()) {
            return null
        }

        if (srcFile.length() == 0L) {
            return null
        }
        try {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(srcFile.absolutePath)
            val findVideoFormatTrackIndex = findVideoFormatTrackIndex(mediaExtractor)
            return mediaExtractor.getTrackFormat(findVideoFormatTrackIndex)
        } catch (e: Exception) {
        }
        return null
    }

    fun findVideoFormatTrackIndex(mediaExtractor: MediaExtractor): Int {
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

    fun getMediaFormatMaxInputSize(mediaFormat: MediaFormat): Int {
        val max_input_size = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        return if (max_input_size == 0) {
            1024 * 1024
        } else {
            max_input_size
        }
    }


}