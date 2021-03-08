package com.tck.av.video.extractor

import androidx.core.graphics.toColorInt
import android.graphics.Typeface
import android.media.MediaExtractor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.tck.av.common.*
import com.tck.av.video.extractor.databinding.ActivityVideoExtractorHomeBinding
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class VideoExtractorHomeActivity : AppCompatActivity() {

    companion object {
        const val EXTRACTOR_DIR = "extractor"
    }

    private lateinit var binding: ActivityVideoExtractorHomeBinding

    private lateinit var cacheFile: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoExtractorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FileUtils.deleteCacheDirFile(FileUtils.createCacheDir(this, EXTRACTOR_DIR))
        cacheFile = FileUtils.createCacheFile(this, EXTRACTOR_DIR, "src.mp4")
        FileUtils.copyAssetsFileToCache(this, "WeChat_20210304214737.mp4", cacheFile)

        binding.btnVideoExtractor.setOnClickListener {
            showVideoInfo()
        }

        binding.btnExtractorH264.setOnClickListener {
            extractorH264()
        }
    }

    private fun extractorH264() {
        val createCacheFile = FileUtils.createCacheFile(
            this,
            EXTRACTOR_DIR,
            "video_${System.currentTimeMillis()}.h264"
        )
        val extractorH264Task = ExtractorH264Task(
            createCacheFile,
            object : TaskExecuteCallback {
                override fun onStart() {
                    binding.btnExtractorH264.isEnabled = false
                    binding.llExtractorH264Container.visibility = View.GONE
                }

                override fun onSuccess() {
                    binding.btnExtractorH264.isEnabled = true
                    binding.llExtractorH264Container.visibility = View.VISIBLE
                    binding.tvExtractorH264Result.text = createCacheFile.absolutePath
                }

                override fun onError() {
                    binding.btnExtractorH264.isEnabled = true
                    binding.llExtractorH264Container.visibility = View.GONE
                }
            }
        )
        val initMediaExtractor = extractorH264Task.initMediaExtractor(cacheFile)
        if (!initMediaExtractor) {
            return
        }

        TaskExecutor.instances.executeOnDiskIO(extractorH264Task)
    }

    private fun showVideoInfo() {
        val videoFormatInfo = MediaExtractorUtils.getVideoFormatInfo(cacheFile)
        if (videoFormatInfo != null) {
            setVideoInfoView(videoFormatInfo.toString())
        } else {
            binding.llAudioInfo.removeAllViews()
            binding.llAudioInfo.addView(createKeyValueInfoWidget("error", "视频解析错误"))
            binding.llAudioInfo.setBackgroundResource(R.drawable.shape_corners_4dp_stroke_fff0f0f0_solid_1ad8d8d8)
        }
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