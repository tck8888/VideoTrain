package com.tck.av.common

import android.app.Activity
import java.io.File
import java.io.FileOutputStream

/**
 *<p>description:</p>
 *<p>created on: 2021/3/8 19:24</p>
 * @author tck
 * @version v1.0
 *
 */
object FileUtils {

    fun deleteCacheDirFile(cacheDir: File) {
        if (cacheDir.isFile) {
            cacheDir.delete()
            return
        } else {
            cacheDir.listFiles()?.forEach {
                if (it.isFile) {
                    it.delete()
                } else {
                    deleteCacheDirFile(it)
                }
            }
        }
    }

    fun createCacheDir(activity: Activity, dirName: String): File {
        val file = File(activity.cacheDir, dirName)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    fun createCacheFile(activity: Activity, dirName: String, fileName: String): File {
        return File(createCacheDir(activity, dirName), fileName)
    }

    fun copyAssetsFileToCache(activity: Activity, assetsFileName: String, cacheFile: File) {
        try {
            val openFd = activity.assets.openFd(assetsFileName)
            openFd.createInputStream().channel.use { from ->
                FileOutputStream(cacheFile).channel.use { to ->
                    from.transferTo(openFd.startOffset, openFd.length, to)
                }
            }
        } catch (e: Exception) {
            TLog.e("copy assetsFileName:${assetsFileName} to cacheFile:${cacheFile.absolutePath} error:${e.message}")
        }
    }
}