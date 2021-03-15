package com.tck.av.common

import android.content.res.Resources

/**
 *<p>description:</p>
 *<p>created on: 2021/3/8 13:29</p>
 * @author tck
 * @version v1.0
 *
 */
fun Float.dp2px(): Int = (Resources.getSystem().displayMetrics.density * this + 0.5f).toInt()