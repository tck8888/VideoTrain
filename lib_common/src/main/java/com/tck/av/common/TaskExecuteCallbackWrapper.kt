package com.tck.av.common

/**
 *<p>description:</p>
 *<p>created on: 2021/3/9 9:23</p>
 * @author tck
 * @version v1.0
 *
 */
open class TaskExecuteCallbackWrapper(private val callback: TaskExecuteCallback? = null) {

    fun onStart() {
        callback?.let {
            TaskExecutor.instances.postToMainThread {
                it.onStart()
            }
        }
    }

    fun onSuccess() {
        callback?.let {
            TaskExecutor.instances.postToMainThread {
                it.onSuccess()
            }
        }
    }

    fun onError(msg: String, code: Int = -1) {
        callback?.let {
            TaskExecutor.instances.postToMainThread {
                it.onError(msg, code)
            }
        }
    }
}

