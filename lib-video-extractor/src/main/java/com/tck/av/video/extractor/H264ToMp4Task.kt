package com.tck.av.video.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.widget.Toast
import com.tck.av.common.MediaExtractorUtils
import com.tck.av.common.TLog
import com.tck.av.common.TaskExecutor
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 *<p>description:</p>
 *<p>created on: 2021/3/8 19:30</p>
 * @author tck
 * @version v1.0
 *
 */
class H264ToMp4Task(private val cacheFile: File, var callback: TaskExecuteCallback? = null) :
    Runnable {

    private var muxer: MediaMuxer? = null

    fun start(srcFile:File){
        val muxerTemp =
            MediaMuxer(cacheFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        //MediaExtractorUtils.findVideoFormatTrackIndex()
    }
    override fun run() {
        TODO("Not yet implemented")
    }
}