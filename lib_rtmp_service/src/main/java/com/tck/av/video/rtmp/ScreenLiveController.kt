package com.tck.av.video.rtmp

import com.tck.av.common.TLog

/**
 *<p>description:</p>
 *<p>created on: 2021/3/9 12:26</p>
 * @author tck
 * @version v1.0
 *
 */
class ScreenLiveController(private val url: String) : Thread() {


    init {
        System.loadLibrary("myrtmp")
    }


    override fun run() {
        super.run()

        if (!connect(url)) {
            TLog.i("connect error")
            return
        }
    }

    private external fun connect(url: String): Boolean

    private external fun sendData(data: ByteArray, len: Int, tms: Long): Boolean
}