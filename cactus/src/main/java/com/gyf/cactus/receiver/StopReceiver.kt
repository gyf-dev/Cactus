package com.gyf.cactus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.gyf.cactus.entity.Constant
import com.gyf.cactus.ext.mainPid

/**
 * 注销服务监听
 *
 * @author geyifeng
 * @date 2019-11-19 10:05
 */
internal class StopReceiver private constructor(val context: Context) : BroadcastReceiver() {

    companion object {
        internal fun newInstance(context: Context) = StopReceiver(context)
    }

    /**
     * 待操作事件
     */
    private var mBlock: (() -> Unit)? = null

    private var mActionStop = "${Constant.CACTUS_FLAG_STOP}.${context.packageName}"

    init {
        context.registerReceiver(this, IntentFilter(mActionStop))
    }

    /**
     * 注册
     *
     * @param block Function0<Unit>
     */
    internal fun register(block: () -> Unit) {
        mBlock = block
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.also {
            when (it) {
                mActionStop -> {
                    this.context.unregisterReceiver(this)
                    mBlock?.let {
                        it()
                    }
                }
            }
        }
    }
}