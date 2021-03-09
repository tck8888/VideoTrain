package com.tck.av.common

/**
 *
 * description:

 * @date 2021/3/8 22:33

 * @author tck88
 *
 * @version v1.0.0
 *
 */
interface TaskExecuteCallback {

    fun onStart()
    fun onSuccess()
    fun onError(msg:String,code:Int=-1)
}