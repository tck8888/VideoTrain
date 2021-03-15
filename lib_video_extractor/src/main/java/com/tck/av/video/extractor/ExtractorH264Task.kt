package com.tck.av.video.extractor

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import com.tck.av.common.*
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
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

            mediaCodec = MediaCodec.createDecoderByType(
                videoFormatTemp.getString(MediaFormat.KEY_MIME) ?: MediaFormat.MIMETYPE_VIDEO_AVC
            )
            mediaCodec?.configure(videoFormatTemp, null, null, 0)
            return true
        } catch (e: Exception) {
            callback?.onError("ExtractorH264Task initMediaExtractor error:${e.message}")
        }
        return false
    }

    override fun run() {
        val mediaCodecTemp = mediaCodec
        val mediaExtractorTemp = mediaExtractor
        if (mediaCodecTemp == null || mediaExtractorTemp == null || videoTrackIndex == -1 || mediaFormatMaxInputSize == 0) {
            callback?.onError("mediaCodecTemp == null || mediaExtractorTemp==null || videoTrackIndex == -1 || mediaFormatMaxInputSize == 0")
            return
        }

        callback?.onStart()
        mediaExtractorTemp.selectTrack(videoTrackIndex)
        val buffer = ByteBuffer.allocate(mediaFormatMaxInputSize)
        val bufferInfo = MediaCodec.BufferInfo()

        mediaCodecTemp.start()

        val videoFormatTemp = mediaExtractorTemp.getTrackFormat(videoTrackIndex)

        //pps
        //00 00 00 01 67 64 00 1F AC D9 40 DC 11 68 40 00 00 03 00 40 00 00 0F 03 C6 0C 65 80
        val byteBuffer_pps = videoFormatTemp.getByteBuffer("csd-0")
        val pps = byteBufferToByte(videoFormatTemp.getByteBuffer("csd-0"))
        //sps
        //00 00 00 01 68 EF BC B0
        val byteBuffer_sps = videoFormatTemp.getByteBuffer("csd-1")
        val sps = byteBufferToByte(videoFormatTemp.getByteBuffer("csd-1"))

        val header = ByteArray(pps.size + sps.size)
        System.arraycopy(sps, 0, header, 0, sps.size)
        System.arraycopy(pps, 0, header, sps.size, pps.size)

        //  TLog.i(String(header))
        mediaExtractorTemp.selectTrack(videoTrackIndex)

        val timeoutUs = 10L
        try {
            FileOutputStream(cacheFile).use { fileOutputStream ->
                while (true) {
                    var inputIndex = mediaCodecTemp.dequeueInputBuffer(timeoutUs)
                    if (inputIndex >= 0) {
                        val readSampleData = mediaExtractorTemp.readSampleData(buffer, 0)
                        TLog.i("readSampleData:${readSampleData}")

                        val content = ByteArray(readSampleData)
                        buffer.get(content)

                        val inputBuffer = mediaCodecTemp.getInputBuffer(inputIndex)
                        inputBuffer?.put(content)
                        mediaCodecTemp.queueInputBuffer(
                            inputIndex,
                            0,
                            readSampleData,
                            mediaExtractorTemp.sampleTime,
                            0
                        )

                        mediaExtractorTemp.advance()
                    }

                    var index = mediaCodecTemp.dequeueOutputBuffer(bufferInfo, timeoutUs)
                    while (index >= 0) {
                        val outputBuffer = mediaCodecTemp.getOutputBuffer(index)
                        mediaCodecTemp.releaseOutputBuffer(index, false)
                        outputBuffer?.let {

                            val out = ByteArray(sps.size + pps.size + it.remaining())
                            byteBuffer_sps.get(out)
                            byteBuffer_pps.get(out,sps.size,pps.size)
                            it.get(out,sps.size+pps.size,it.remaining())
                            fileOutputStream.write(out)
                        }
                        index = mediaCodecTemp.dequeueOutputBuffer(bufferInfo, timeoutUs)
                    }
                }
            }
        } catch (e: Exception) {
            callback?.onError("ExtractorH264Task run error:${e.message}")
        }


        TLog.i("ExtractorH264Task success:${cacheFile.length() / 1024}kb")
        mediaExtractorTemp.release()
        mediaExtractor = null
        mediaCodecTemp.stop()
        mediaCodecTemp.release()
        mediaCodec = null
        callback?.onSuccess()
        callback = null
    }

    fun byteBufferToByte(buffer: ByteBuffer?): ByteArray {
        if (buffer == null) {
            return ByteArray(0)
        }
        val byteArray = ByteArray(buffer.remaining())
        buffer.get(byteArray)
        return byteArray
    }
}
