package com.gyf.cactus.service

import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.work.WorkManager
import com.gyf.cactus.Cactus
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.ICactusInterface
import com.gyf.cactus.pix.OnePixModel
import com.gyf.cactus.ext.*

/**
 * 本地服务
 * @author geyifeng
 * @date 2019-08-28 17:05
 */
class LocalService : Service() {

    /**
     * 配置信息
     */
    private var mCactusConfig = CactusConfig()
    /**
     * 音乐播放器
     */
    private var mMediaPlayer: MediaPlayer? = null

    /**
     * 一像素广播
     */
    private val mScreenOnOffReceiver = ScreenOnOffReceiver()

    /**
     * Service是否在运行
     */
    private var mIsServiceRunning = false

    /**
     * 音乐是否在播放
     */
    private var mIsMusicRunning = false

    /**
     * 服务连接次数
     */
    private var mConnectionTimes = 0

    private val mHandler = Handler(Looper.getMainLooper())

    private lateinit var mLocalBinder: LocalBinder

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            startRemoteService(this, mCactusConfig)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service?.let {
                ICactusInterface.Stub.asInterface(it)
                    ?.apply {
                        wakeup(mCactusConfig)
                        connectionTimes(++mConnectionTimes)
                    }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.getParcelableExtra<CactusConfig>(Cactus.CACTUS_CONFIG)?.let {
            mCactusConfig = it
        }
        setNotification(mCactusConfig.notificationConfig)
        startRemoteService(mServiceConnection, mCactusConfig)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        onStop()
    }

    /**
     * 处理外部事情
     * @param times Int
     */
    private fun doWork(times: Int) {
        if (!mIsServiceRunning) {
            mIsServiceRunning = true
            log("LocalService is run!")
            registerMedia()
            registerBroadcastReceiver()
            sendBroadcast(Intent(Cactus.CACTUS_WORK))
            if (Cactus.CALLBACKS.isNotEmpty()) {
                Cactus.CALLBACKS.forEach {
                    it.doWork(times)
                }
            }
        }
    }

    /**
     * 停止回调
     */
    private fun onStop() {
        log("LocalService is stop!")
        WorkManager.getInstance(this).cancelAllWorkByTag(Cactus.CACTUS_TAG)
        sendBroadcast(Intent(Cactus.CACTUS_STOP))
        if (Cactus.CALLBACKS.isNotEmpty()) {
            Cactus.CALLBACKS.forEach {
                it.onStop()
            }
        }
        unregisterReceiver(mScreenOnOffReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        mLocalBinder = LocalBinder()
        return mLocalBinder
    }

    inner class LocalBinder : ICactusInterface.Stub() {

        override fun wakeup(config: CactusConfig) {
            setNotification(config.notificationConfig)
            mCactusConfig = config
        }

        override fun connectionTimes(time: Int) {
            mConnectionTimes = time
            doWork((mConnectionTimes + 1) / 2)
        }
    }

    /**
     * 屏幕息屏亮屏广播
     */
    inner class ScreenOnOffReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.apply {
                when (this) {
                    Intent.ACTION_SCREEN_OFF -> {
                        // 熄屏，打开1像素Activity
                        log("screen off")
                        openOnePix()
                        playMusic()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        //亮屏，关闭1像素Activity
                        log("screen on")
                        closeOnePix()
                        pauseMusic()
                    }
                }
            }
        }
    }

    /**
     * 打开一像素
     */
    private fun openOnePix() {
        if (mCactusConfig.defaultConfig.onePixEnabled) {
            mHandler.postDelayed({ startOnePixActivity() }, 1000)
        }
    }

    /**
     * 关闭一像素
     */
    private fun closeOnePix() {
        mCactusConfig.defaultConfig.apply {
            if (onePixEnabled) {
                backBackground()
                if (onePixModel == OnePixModel.DEFAULT) {
                    finishOnePix()
                }
            }
        }
    }

    /**
     * 注册息屏亮屏广播监听
     */
    private fun registerBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(mScreenOnOffReceiver, intentFilter)
    }

    /**
     * 注册音乐播放器
     */
    private fun registerMedia() {
        if (mCactusConfig.defaultConfig.musicEnabled) {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer.create(this, mCactusConfig.defaultConfig.musicId)
            }
            mMediaPlayer?.apply {
                if (!mCactusConfig.defaultConfig.debug) {
                    setVolume(0f, 0f)
                }
                setOnCompletionListener {
                    mHandler.postDelayed(
                        {
                            mIsMusicRunning = false
                            playMusic()
                        },
                        mCactusConfig.defaultConfig.repeatInterval
                    )
                }
                setOnErrorListener { _, _, _ -> false }
                playMusic()
            }
        }
    }

    /**
     * 播放音乐
     */
    private fun playMusic() {
        if (!isScreenOn) {
            mMediaPlayer?.apply {
                if (mCactusConfig.defaultConfig.musicEnabled) {
                    if (!mIsMusicRunning) {
                        start()
                        mIsMusicRunning = true
                        log("music is playing")
                    }
                }
            }
        } else {
            pauseMusic()
        }
    }

    /**
     * 暂停音乐
     */
    private fun pauseMusic() {
        mMediaPlayer?.apply {
            if (mIsMusicRunning) {
                pause()
                mIsMusicRunning = false
                log("music is pause")
            }
        }
    }

    /**
     * log输出
     * @param msg String
     */
    private fun log(msg: String) {
        if (mCactusConfig.defaultConfig.debug) {
            Log.d(Cactus.CACTUS_TAG, msg)
        }
    }
}