package com.tck.av.video.extractor

import androidx.core.graphics.toColorInt
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.tck.av.common.FileUtils
import com.tck.av.common.MediaExtractorUtils
import com.tck.av.common.TLog
import com.tck.av.common.dp2px
import com.tck.av.video.extractor.databinding.ActivityVideoExtractorHomeBinding
import java.io.FileOutputStream
import java.nio.ByteBuffer

class VideoExtractorHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoExtractorHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoExtractorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVideoExtractor.setOnClickListener {
            extractor()
        }
    }

    private fun extractor() {
        val openNonAssetFd = try {
            assets.openFd("WeChat_20210304214737.mp4")
        } catch (e: Exception) {
            null
        }
        if (openNonAssetFd == null) {
            Toast.makeText(this, "打开不了视频文件", Toast.LENGTH_SHORT).show()
            return
        }
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(openNonAssetFd)

        val videoTrackIndex = MediaExtractorUtils.findVideoFormat(mediaExtractor)
        if (videoTrackIndex == -1) {
            Toast.makeText(this, "找不到视频", Toast.LENGTH_SHORT).show()
            return
        }
        val videoFormat = mediaExtractor.getTrackFormat(videoTrackIndex)

        setVideoInfoView(videoFormat.toString())

        val mediaFormatMaxInputSize = MediaExtractorUtils.getMediaFormatMaxInputSize(videoFormat)
        mediaExtractor.selectTrack(videoTrackIndex)
        val inputBuffer = ByteBuffer.allocate(mediaFormatMaxInputSize)

        //https://www.jianshu.com/p/a22b5305a3e2?utm_campaign=haruki&utm_content=note&utm_medium=seo_notes&utm_source=recommendation
        FileOutputStream(
            FileUtils.getCacheFile(
                this,
                "extractor",
                "video_${System.currentTimeMillis()}.h264"
            )
        ).use { fileOutputStream ->
            while (true) {
                val readSampleCount = mediaExtractor.readSampleData(inputBuffer, 0)
                TLog.i("readSampleCount:${readSampleCount}")
                if (readSampleCount < 0) {
                    break
                }
                val buffer = ByteArray(readSampleCount)
                inputBuffer.get(buffer)
                fileOutputStream.write(buffer)


                inputBuffer.clear()
                mediaExtractor.advance()


            }
        }

        mediaExtractor.release()
    }


    private fun setVideoInfoView(info: String) {
        binding.llAudioInfo.removeAllViews()
        val replace = info.replace("{", "")
        val replace1 = replace.replace("}", "")
        replace1.split(",").forEach {

            val key = it.substring(0, it.indexOf("=")).trim()
            val value = it.substring(it.indexOf("=") + 1).trim()

            if (key.isNotEmpty()) {
                binding.llAudioInfo.addView(createKeyValueInfoWidget(key, value))
            }
        }

        binding.llAudioInfo.setBackgroundResource(R.drawable.shape_corners_4dp_stroke_fff0f0f0_solid_1ad8d8d8)

    }

    private fun createKeyValueInfoWidget(key: String, value: String): LinearLayout {
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

        val tvKey = TextView(this).apply {
            textSize = 13f
            setTextColor("#FF666666".toColorInt())
            text = key
            layoutParams = LinearLayout.LayoutParams(100f.dp2px(), -2)
        }
        linearLayout.addView(tvKey)

        val tvValue = TextView(this).apply {
            textSize = 13f
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            setTextColor("#FF222222".toColorInt())
            text = value
            layoutParams = LinearLayout.LayoutParams(-2, -2)
        }
        linearLayout.addView(tvValue)


        return linearLayout
    }
}