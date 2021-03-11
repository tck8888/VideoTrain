package com.tck.av.video.rtmp

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 *<p>description:</p>
 *<p>created on: 2021/3/10 19:01</p>
 * @author tck
 * @version v1.0
 *
 */
class ScreenLiveService : Service() {

    val channelId = "123"


    inner class ScreenLiveBinder : Binder() {
        fun getService(): ScreenLiveService = this@ScreenLiveService
    }

    override fun onBind(intent: Intent?): IBinder {
        return ScreenLiveBinder()
    }

    private fun createNotificationChannel() {

        NotificationHelper.instances
            .createSystem(this)
            .setOngoing(true)
            .setTicker("录频直播")
            .setContentText("录频直播")
            .setDefaults(Notification.DEFAULT_ALL)
            .build()

        val intent = Intent(this, ScreenLiveActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val notification =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(channelId, "录屏直播", NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableVibration(true)
                notificationChannel.setShowBadge(true)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.parseColor("#e8334a")
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
                NotificationCompat
                    .Builder(this, channelId)
                    .setContentTitle("录屏直播")
                    .setContentText("正在录屏...")
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build()
            } else {
                Notification.Builder(this)
                    .setContentTitle("录屏直播")
                    .setContentText("正在录屏...")
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .build()
            }

        startForeground(10000, notification)
    }

    private fun getMediaProjectionManager(): MediaProjectionManager {
        return getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    fun createVirtualDisplay(resultCode: Int, resultData: Intent) {
        createNotificationChannel()
        val mediaProjection =
            getMediaProjectionManager().getMediaProjection(resultCode, resultData)
        ScreenLiveController("rtmp://172.20.7.219:1935/myapp", mediaProjection).start()
    }


}