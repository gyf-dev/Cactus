package com.gyf.cactus.sample

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.gyf.cactus.Cactus
import com.gyf.cactus.callback.CactusCallback
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
        /**
         * 结束时间
         */
        val mEndDate = MutableLiveData<String>()
        /**
         * 上次存活时间
         */
        val mLastTimer = MutableLiveData<String>()
        /**
         * 存活时间
         */
        val mTimer = MutableLiveData<String>()
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        //可选，设置通知栏点击事件
        val pendingIntent =
            PendingIntent.getActivity(this, 0, Intent().apply {
                setClass(this@App, MainActivity::class.java)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }, PendingIntent.FLAG_UPDATE_CURRENT)
        //可选，注册广播监听器
        registerReceiver(MainReceiver(), IntentFilter().apply {
            addAction(Cactus.CACTUS_WORK)
            addAction(Cactus.CACTUS_STOP)
            addAction(Cactus.CACTUS_BACKGROUND)
            addAction(Cactus.CACTUS_FOREGROUND)
        })

        cactus {
            //可选，设置通知栏点击事件
            setPendingIntent(pendingIntent)
            //可选，设置音乐
            setMusicId(R.raw.main)
            //可选，是否是debug模式
            isDebug(true)
            //可选，退到后台是否可以播放音乐
            setBackgroundMusicEnabled(true)
            //可选，运行时回调
            addCallback(this@App)
            //可选，切后台切换回调
            addBackgroundCallback {
                Toast.makeText(this@App, if (it) "退到后台啦" else "跑到前台啦", Toast.LENGTH_SHORT).show()
            }
        }
        //或者这样设置前后台监听
//        registerActivityLifecycleCallbacks(AppBackgroundCallback {
//
//        })
    }

    @SuppressLint("CheckResult")
    override fun doWork(times: Int) {
        Log.d(TAG, "doWork:$times")
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+00:00")
        var oldTimer = Save.timer
        if (times == 1) {
            Save.lastTimer = oldTimer
            Save.endDate = Save.date
            oldTimer = 0L
        }
        mLastTimer.postValue(dateFormat.format(Date(Save.lastTimer * 1000)))
        mEndDate.postValue(Save.endDate)
        Observable.interval(1, TimeUnit.SECONDS)
            .map {
                oldTimer + it
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { aLong ->
                Save.timer = aLong
                Save.date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).run {
                    format(Date())
                }
                mTimer.value = dateFormat.format(Date(aLong * 1000))
            }
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }
}