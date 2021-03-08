package com.tck.av.common

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * description:

 * @date 2021/3/8 21:40

 * @author tck88
 *
 * @version v1.0.0
 *
 */
class TaskExecutor private constructor() {

    companion object {
        val instances: TaskExecutor by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { TaskExecutor() }
    }

    private val mDiskIO = Executors.newFixedThreadPool(4, object : ThreadFactory {
        private val THREAD_NAME_STEM = "arch_disk_io_%d"
        private val mThreadId = AtomicInteger(0)
        override fun newThread(r: Runnable): Thread {
            val t = Thread(r)
            t.name = String.format(THREAD_NAME_STEM, mThreadId.getAndIncrement())
            return t
        }
    })

    private val mainHandler = Handler(Looper.getMainLooper())

    fun executeOnDiskIO(runnable: Runnable) {
        mDiskIO.execute(runnable)
    }
    fun postToMainThread(runnable: Runnable){
        mainHandler.post(runnable)
    }
}