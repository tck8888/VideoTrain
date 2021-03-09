package com.tck.av.video.rtmp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tck.av.video.rtmp.databinding.ActivityScreenLiveBinding

class ScreenLiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScreenLiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreenLiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConnectLive.setOnClickListener {
            ScreenLiveController("rtmp://58.200.131.2:1935/livetv/hunantv").start()
        }
    }
}