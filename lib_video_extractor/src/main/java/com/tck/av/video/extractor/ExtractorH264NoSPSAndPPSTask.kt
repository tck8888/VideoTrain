package com.tck.av.video.extractor

import android.media.MediaExtractor
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
class ExtractorH264NoSPSAndPPSTask(
    private val cacheFile: File,
    var callback: TaskExecuteCallbackWrapper? = null
) :
    Runnable {

    private var mediaExtractor: MediaExtractor? = null
    private var mediaFormatMaxInputSize = 0
    private var videoTrackIndex = -1

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