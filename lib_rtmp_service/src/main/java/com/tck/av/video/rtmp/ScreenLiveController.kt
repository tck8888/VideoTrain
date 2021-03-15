package com.tck.av.video.rtmp

import android.media.projection.MediaProjection
import com.tck.av.common.TLog
import com.tck.av.common.TaskExecutor
import java.util.concurrent.LinkedBlockingQueue

/**
 * https://developer.qiniu.com/pili/kb/6701/push-the-flow-quality-configuration
 *<p>description:</p>
 *<p>created on: 2021/3/9 12:26</p>
 * @author tck
 * @version v1.0
 *
 */
class ScreenLiveController(private val url: String,  var mediaProjection: MediaProjection?) :
    Thread() {

    init {
        System.loadLibrary("myrtmp")
    }

    private val queue: LinkedBlockingQueue<RTMPPackage> = LinkedBlockingQueue()

    private var isLiving = false

    fun addPackage(rtmpPackage: RTMPPackage?) {
        if (!isLiving) {
            return
        }
        queue.add(rtmpPackage)
    }

    override fun run() {
        super.run()

        if (!connect(url)) {
            TLog.i("connect error")
            return
        }
        val videoCodecTask = VideoCodecTask(this)
        videoCodecTask.startLive()
        TaskExecutor.instances.executeOnDiskIO(videoCodecTask)
        
        isLiving = true
        while (isLiving) {
            val rtmpPackage = try {
                queue.take()
            } catch (e: Exception) {
                TLog.e("get RTMPPackage error:${e.message}")
                null
            }
            if (rtmpPackage != null && rtmpPackage.buffer.isNotEmpty()) {
                TLog.i("run:推送：${rtmpPackage.buffer.size}")
                sendData(rtmpPackage.buffer, rtmpPackage.buffer.size, rtmpPackage.tms)
            }
        }
    }

    private external fun connect(url: String): Boolean

    private external fun sendData(data: ByteArray, len: Int, tms: Long): Boolean
}