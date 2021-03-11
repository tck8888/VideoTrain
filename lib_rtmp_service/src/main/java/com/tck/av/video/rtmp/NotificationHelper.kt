package com.tck.av.video.rtmp

import android.content.Context
import androidx.core.app.NotificationCompat

/**
 * https://github.com/SMask/MediaProjectionLibrary_Android/blob/a2899245d9/app/src/main/java/com/mask/mediaprojectionlibrary/NotificationHelper.java
 *<p>description:</p>
 *<p>created on: 2021/3/11 9:24</p>
 * @author tck
 * @version v1.0
 *
 */
class NotificationHelper private constructor() {

    companion object {
        const val CHANNEL_ID_SYSTEM = "system";
        val instances: NotificationHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { NotificationHelper() }
    }

    init {
        createChannel()
    }

    private fun createChannel() {

    }

    fun createSystem(context: Context): NotificationCompat.Builder {
        return create(context, CHANNEL_ID_SYSTEM)
    }

    private fun create(context: Context, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).setContentTitle("音视频学习")
            .setWhen(System.currentTimeMillis())
    }
}