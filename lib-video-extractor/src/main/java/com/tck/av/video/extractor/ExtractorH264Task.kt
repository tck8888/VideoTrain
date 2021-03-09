package com.tck.av.video.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import com.tck.av.common.MediaExtractorUtils
import com.tck.av.common.TLog
import com.tck.av.common.TaskExecuteCallback
import com.tck.av.common.TaskExecutor
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
class ExtractorH264Task(private val cacheFile: File, var callback: TaskExecuteCallback? = null) :
    Runnable {

    private var mediaExtractor: MediaExtractor? = null
    private var mediaFormatMaxInputSize = 0
    private var videoTrackIndex = -1
    private var videoFormat: MediaFormat? = null

    fun initMediaExtractor(srcFile: File): Boolean {
        try {
            val mediaExtractorTemp = MediaExtractor()
            mediaExtractorTemp.setDataSource(srcFile.absolutePath)
            videoTrackIndex =
                MediaExtractorUtils.findVideoFormatTrackIndex(mediaExtractorTemp)
            if (videoTrackIndex == -1) {
                callback?.onError("")
                return false
            }
            val videoFormatTemp = mediaExtractorTemp.getTrackFormat(videoTrackIndex)
            mediaFormatMaxInputSize =
                MediaExtractorUtils.getMediaFormatMaxInputSize(videoFormatTemp)
            videoFormat = videoFormatTemp
            mediaExtractor = mediaExtractorTemp
            return true
        } catch (e: Exception) {
            TLog.i("ExtractorH264Task initMediaExtractor error:${e.message}")
            callback?.onError("")
        }
        return false
    }

    override fun run() {
        val mediaExtractorTemp = mediaExtractor ?: return
        if (videoTrackIndex == -1) {
            return
        }
        if (mediaFormatMaxInputSize == 0) {
            return
        }
        callback?.let {
            TaskExecutor.instances.postToMainThread {
                it.onStart()
            }
        }
        mediaExtractorTemp.selectTrack(videoTrackIndex)
        val inputBuffer = ByteBuffer.allocate(mediaFormatMaxInputSize)
        try {
            FileOutputStream(cacheFile).use { fileOutputStream ->
                while (true) {
                    val readSampleCount = mediaExtractorTemp.readSampleData(inputBuffer, 0)
                    TLog.i("readSampleCount:${readSampleCount}")
                    if (readSampleCount < 0) {
                        break
                    }
                    val buffer = ByteArray(readSampleCount)
                    inputBuffer.get(buffer)
                    fileOutputStream.write(buffer)
                    inputBuffer.clear()
                    mediaExtractorTemp.advance()
                }
            }
        } catch (e: Exception) {
            TLog.i("ExtractorH264Task run error:${e.message}")
            callback?.let {
                TaskExecutor.instances.postToMainThread {
                    it.onError("")
                }
            }
        }
        TLog.i("ExtractorH264Task success:${cacheFile.length() / 1024}kb")
        mediaExtractorTemp.release()
        mediaExtractor = null
        callback?.let {
            TaskExecutor.instances.postToMainThread {
                it.onSuccess()
            }
        }
        callback = null
    }
}