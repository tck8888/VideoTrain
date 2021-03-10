package com.tck.av.video.rtmp

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle

/**
 *<p>description:</p>
 *<p>created on: 2021/3/10 12:44</p>
 * @author tck
 * @version v1.0
 *
 */
class VideoCodecTask(var screenLiveController: ScreenLiveController) : Runnable {

    private var virtualDisplay: VirtualDisplay? = null
    private var mediaCodec: MediaCodec? = null

    private var timeStamp = 0L
    private var startTime = 0L
    private var isLiving = false

    fun startLive() {
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
        virtualDisplay = screenLiveController.mediaProjection?.createVirtualDisplay(
            "tck", 720, 1280, 1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mediaCodec?.createInputSurface(), null, null
        )
    }

    override fun run() {
        val virtualDisplayTemp = virtualDisplay
        val mediaCodecTemp = mediaCodec
        if (virtualDisplayTemp == null || mediaCodecTemp == null) {
            return
        }
        isLiving = true
        mediaCodecTemp.start()
        val bufferInfo = MediaCodec.BufferInfo()
        while (isLiving) {
            if (System.currentTimeMillis() - timeStamp >= 2000) {
                val params = Bundle()
                params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                //                dsp 芯片触发I帧
                mediaCodecTemp.setParameters(params)
                timeStamp = System.currentTimeMillis()
            }

            var index = mediaCodecTemp.dequeueOutputBuffer(bufferInfo, 100000)
            if (index >= 0) {
                if (startTime == 0L) {
                    startTime = bufferInfo.presentationTimeUs / 1000
                }
                val outputBuffer = mediaCodecTemp.getOutputBuffer(index)
                outputBuffer?.let {
                    val outData = ByteArray(bufferInfo.size)
                    it.get(outData)
                    screenLiveController.addPackage(
                        RTMPPackage(
                            outData,
                            (bufferInfo.presentationTimeUs / 1000) - startTime
                        )
                    )
                }
                mediaCodecTemp.releaseOutputBuffer(index, false)
            }
        }
        isLiving = false
        mediaCodecTemp.stop()
        mediaCodecTemp.release()
        mediaCodec = null
        virtualDisplayTemp.release()
        virtualDisplay = null
        screenLiveController.mediaProjection?.stop()
        screenLiveController.mediaProjection=null
        startTime = 0
    }


}