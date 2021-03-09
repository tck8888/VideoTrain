package com.tck.av.video

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.collection.ArrayMap
import com.tck.av.video.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val map = ArrayMap<String, String>().apply {
        put("com.tck.av.video.extractor", "com.tck.av.video.extractor.VideoExtractorHomeActivity")
        put("com.tck.av.video.rtmp", "com.tck.av.video.rtmp.ScreenLiveActivity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.btnVideoExtractor.setOnClickListener {
            jump(map.getOrDefault("com.tck.av.video.extractor", ""))
        }

        binding.btnVideoLive.setOnClickListener {
            jump(map.getOrDefault("com.tck.av.video.rtmp", ""))
        }
    }

    private fun jump(clazzName: String) {
        if (clazzName.isEmpty()) {
            Toast.makeText(this, "找不到跳转目标", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent()
        intent.component = ComponentName(this, clazzName)
        startActivity(intent)
    }
}