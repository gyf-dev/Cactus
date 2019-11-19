package com.gyf.cactus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.gyf.cactus.Cactus

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

    init {
        context.registerReceiver(this, IntentFilter(Cactus.CACTUS_FLAG_STOP))
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
                Cactus.CACTUS_FLAG_STOP -> {
                    this.context.unregisterReceiver(this)
                    mBlock?.let {
                        it()
                    }
                }
            }
        }
    }
}