package com.gyf.cactus.simple

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.gyf.cactus.Cactus
import com.gyf.cactus.CactusCallback
import com.gyf.cactus.ext.cactus

/**
 * @author geyifeng
 * @date 2019-08-30 09:49
 */
class App : Application(), CactusCallback {

    companion object {
        const val TAG = "cactus-simple"
    }

    override fun onCreate() {
        super.onCreate()

        // 设置通知栏点击事件，可选
        val pendingIntent =
            PendingIntent.getActivity(this, 0, Intent().apply {
                setClass(this@App, MainActivity::class.java)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }, PendingIntent.FLAG_UPDATE_CURRENT)
        //注册广播监听器，可选
        registerReceiver(MainReceiver(), IntentFilter().apply {
            addAction(Cactus.CACTUS_WORK)
            addAction(Cactus.CACTUS_STOP)
        })

        cactus {
            setPendingIntent(pendingIntent)
            setMusicId(R.raw.main)
            isDebug(true)
            addCallback(this@App)
        }
    }

    override fun doWork() {
        Log.d(TAG, "doWork")
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }
}