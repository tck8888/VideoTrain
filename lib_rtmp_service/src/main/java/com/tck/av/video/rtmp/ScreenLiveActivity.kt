package com.tck.av.video.rtmp

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.tck.av.video.rtmp.databinding.ActivityScreenLiveBinding

class ScreenLiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScreenLiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreenLiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //172.20.7.219:9082
        binding.btnConnectLive.setOnClickListener {
            //ScreenLiveController("rtmp://58.200.131.2:1935/livetv/hunantv").start()
            startLive()
        }


    }

    private fun startLive() {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val createScreenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(createScreenCaptureIntent, 100)

        val intent = Intent(this, ScreenLiveService::class.java)
        bindService(intent,serviceConnection, Service.BIND_AUTO_CREATE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            screenLiveService?.createVirtualDisplay(resultCode, data)
        }
    }



    private var screenLiveService: ScreenLiveService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            screenLiveService = (service as? ScreenLiveService.ScreenLiveBinder)?.getService()
           // screenLiveService.setNotificationEngine()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            screenLiveService = null
        }

    }

}