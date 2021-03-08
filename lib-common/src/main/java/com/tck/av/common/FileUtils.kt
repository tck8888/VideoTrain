package com.tck.av.common

import android.app.Activity
import java.io.File

/**
 *<p>description:</p>
 *<p>created on: 2021/3/8 19:24</p>
 * @author tck
 * @version v1.0
 *
 */
object FileUtils {


    fun getCacheFile(activity: Activity, dirName: String, fileName: String): File {
        val file = File(activity.cacheDir, dirName)
        if (!file.exists()) {
            file.mkdirs()
        }
        return File(file, fileName)
    }
}