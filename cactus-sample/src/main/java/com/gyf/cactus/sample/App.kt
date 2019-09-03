package com.gyf.cactus.sample

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.gyf.cactus.Cactus
import com.gyf.cactus.CactusCallback
import com.gyf.cactus.ext.cactus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author geyifeng
 * @date 2019-08-30 09:49
 */
class App : Application(), CactusCallback {

    companion object {
        const val TAG = "cactus-sample"
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        val mTimer = MutableLiveData<String>()
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
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

    @SuppressLint("CheckResult")
    override fun doWork(times: Int) {
        var oldTimer = Save.timer
        if (times == 1) {
            oldTimer = 0L
        }
        Log.d(TAG, "doWork:$times")
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+00:00")
        Observable.interval(1, TimeUnit.SECONDS)
            .map {
                oldTimer + it
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { aLong ->
                Save.timer = aLong
                mTimer.value = dateFormat.format(Date(aLong * 1000))
            }
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }
}