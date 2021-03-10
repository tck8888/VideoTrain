package com.tck.av.video.rtmp

import android.app.*
import android.content.Context
import android.content.Intent
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val resultData = intent.getParcelableExtra<Intent>("data")
            val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_OK)
            if (resultData !== null && resultCode == Activity.RESULT_OK) {
                createNotificationChannel()
                val mediaProjection =
                    getMediaProjectionManager().getMediaProjection(resultCode, resultData)
                ScreenLiveController("rtmp://172.20.7.219:1935/myapp",mediaProjection).start()
            }else{
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)

    }

    private fun createNotificationChannel() {

        val intent = Intent(this, ScreenLiveActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel("123", "录屏直播", NotificationManager.IMPORTANCE_HIGH)
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
                NotificationCompat
                    .Builder(this, channelId)
                    .setContentTitle("录屏直播")
                    .setContentText("正在录屏...")
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build()
            } else {
                Notification.Builder(this)
                    .setContentTitle("录屏直播")
                    .setContentText("正在录屏...")
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build()
            }

        startForeground(10000,notification)
    }

    private fun getMediaProjectionManager(): MediaProjectionManager {
        return getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

}