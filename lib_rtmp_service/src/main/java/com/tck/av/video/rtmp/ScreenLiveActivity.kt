package com.tck.av.video.rtmp

import android.app.Activity
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            realStartScreenLive(resultCode, data)
        }
    }

    private fun realStartScreenLive(resultCode: Int, data: Intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val intent = Intent(this, ScreenLiveService::class.java)
            intent.putExtra("resultCode", resultCode)
            intent.putExtra("data", data)
            startForegroundService(intent)
        } else {
            val mediaProjection = getMediaProjectionManager().getMediaProjection(resultCode, data)
            ScreenLiveController("rtmp://172.20.7.219:1935/myapp",mediaProjection).start()
        }
    }

    private fun getMediaProjectionManager(): MediaProjectionManager {
        return getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            TODO("Not yet implemented")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
        }

    }

}