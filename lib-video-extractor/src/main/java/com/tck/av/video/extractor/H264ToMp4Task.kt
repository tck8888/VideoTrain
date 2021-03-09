package com.tck.av.video.extractor

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import com.tck.av.common.MediaExtractorUtils
import com.tck.av.common.TLog
import com.tck.av.common.TaskExecuteCallback
import com.tck.av.common.TaskExecuteCallbackWrapper
import java.io.File
import java.nio.ByteBuffer

/**
 *<p>description:</p>
 *<p>created on: 2021/3/8 19:30</p>
 * @author tck
 * @version v1.0
 *
 */
class H264ToMp4Task(private val cacheFile: File, var callback: TaskExecuteCallbackWrapper? = null) :
    Runnable {

    val TAG = this.javaClass.simpleName


    private var mediaExtractor: MediaExtractor? = null
    private var mediaFormatMaxInputSize = -1


    fun createMediaExtractor(srcFile: File): Boolean {
        try {
            val mediaExtractorTemp = MediaExtractor()
            mediaExtractorTemp.setDataSource(srcFile.absolutePath)
            mediaExtractor = mediaExtractorTemp
            return true
        } catch (e: Exception) {
            callback?.onError("createMediaExtractor error:${e.message}")
        }

        return false
    }

    override fun run() {
        val mediaExtractorTemp = mediaExtractor
        if (mediaExtractorTemp == null) {
            callback?.onError(
                "$TAG run error:mediaExtractorTemp == null"
            )
            return
        }
        val findVideoFormatTrackIndex =
            MediaExtractorUtils.findVideoFormatTrackIndex(mediaExtractorTemp)

        if (findVideoFormatTrackIndex == -1) {
            callback?.onError("$TAG run  findVideoFormatTrackIndex is -1")
            return
        }

        val videoMediaFormat = mediaExtractorTemp.getTrackFormat(findVideoFormatTrackIndex)

        val frame_rate = videoMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
        if (frame_rate <= 0) {
            callback?.onError("$TAG run get frame_rate is ${frame_rate}")
            return
        }

        mediaFormatMaxInputSize =
            MediaExtractorUtils.getMediaFormatMaxInputSize(videoMediaFormat)
        if (mediaFormatMaxInputSize == -1) {
            callback?.onError("$TAG run get mediaFormatMaxInputSize is -1")
            return
        }

        val muxer =
            MediaMuxer(cacheFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val videoTrackIndex = muxer.addTrack(videoMediaFormat)
        if (videoTrackIndex == -1) {
            callback?.onError("$TAG run get videoTrackIndex is -1")
            return
        }

        callback?.onStart()

        val inputBuffer = ByteBuffer.allocate(mediaFormatMaxInputSize)
        val bufferInfo = MediaCodec.BufferInfo().apply {
            presentationTimeUs = 0
        }
        mediaExtractorTemp.selectTrack(findVideoFormatTrackIndex)
        muxer.start()
        try {
            while (true) {
                val readSampleData = mediaExtractorTemp.readSampleData(inputBuffer, 0)
                TLog.i("readSampleData:${readSampleData}")
                if (readSampleData < 0) {
                    break
                }
                bufferInfo.offset = 0
                bufferInfo.size = readSampleData
                bufferInfo.flags = mediaExtractorTemp.sampleFlags
                bufferInfo.presentationTimeUs += 1000 * 1000 / frame_rate
                muxer.writeSampleData(videoTrackIndex, inputBuffer, bufferInfo)
                mediaExtractorTemp.advance()
            }
            muxer.stop()
            muxer.release()
        } catch (e: Exception) {
            TLog.e("$TAG run error:${e.message}")
        }

        mediaExtractorTemp.release()
        mediaExtractor = null

        TLog.i("$TAG run get mp4 file success:${cacheFile.absolutePath}")
        callback?.onSuccess()
    }
}