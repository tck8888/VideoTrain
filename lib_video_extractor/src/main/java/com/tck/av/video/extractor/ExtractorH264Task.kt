package com.tck.av.video.extractor

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import com.tck.av.common.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 *<p>description:</p>
 *<p>created on: 2021/3/8 19:30</p>
 * @author tck
 * @version v1.0
 *
 */
class ExtractorH264Task(
    private val cacheFile: File,
    var callback: TaskExecuteCallbackWrapper? = null
) :
    Runnable {

    private var mediaExtractor: MediaExtractor? = null
    private var mediaFormatMaxInputSize = 0
    private var videoTrackIndex = -1
    private var mediaCodec: MediaCodec? = null

    fun initMediaExtractor(srcFile: File): Boolean {
        try {
            val mediaExtractorTemp = MediaExtractor()
            mediaExtractorTemp.setDataSource(srcFile.absolutePath)
            videoTrackIndex =
                MediaExtractorUtils.findVideoFormatTrackIndex(mediaExtractorTemp)
            if (videoTrackIndex == -1) {
                callback?.onError("videoTrackIndex == -1")
                return false
            }
            val videoFormatTemp = mediaExtractorTemp.getTrackFormat(videoTrackIndex)
            mediaFormatMaxInputSize =
                MediaExtractorUtils.getMediaFormatMaxInputSize(videoFormatTemp)
            mediaExtractor = mediaExtractorTemp

            val createVideoFormat =
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280)
            createVideoFormat.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            createVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 400000)
            createVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            createVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec?.configure(createVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            return true
        } catch (e: Exception) {
            callback?.onError("ExtractorH264Task initMediaExtractor error:${e.message}")
        }
        return false
    }

    override fun run() {
        val mediaExtractorTemp = mediaExtractor
        if (mediaExtractorTemp == null|| videoTrackIndex == -1 || mediaFormatMaxInputSize == 0) {
            callback?.onError("mediaExtractorTemp==null || videoTrackIndex == -1 || mediaFormatMaxInputSize == 0")
            return
        }

        callback?.onStart()
        mediaExtractorTemp.selectTrack(videoTrackIndex)
        val buffer = ByteBuffer.allocate(mediaFormatMaxInputSize)

        try {
            FileOutputStream(cacheFile).channel.use { fileOutputStream ->
                while (true) {
                    val readSampleData = mediaExtractorTemp.readSampleData(buffer, 0)
                    TLog.i("readSampleData:${readSampleData}")
                    if (readSampleData < 0) {
                        break
                    }
                    fileOutputStream.write(buffer)
                    buffer.clear()
                    mediaExtractorTemp.advance()
                }
            }
        } catch (e: Exception) {
            callback?.onError("ExtractorH264Task run error:${e.message}")
        }
        TLog.i("ExtractorH264Task success:${cacheFile.length() / 1024}kb")
        mediaExtractorTemp.release()
        mediaExtractor = null
        callback?.onSuccess()
        callback = null
    }
}