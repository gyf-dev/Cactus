package com.gyf.cactus.service

import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.gyf.cactus.Cactus
import com.gyf.cactus.CactusConfig
import com.gyf.cactus.ICactusInterface
import com.gyf.cactus.ext.isScreenOn
import com.gyf.cactus.ext.setNotification
import com.gyf.cactus.ext.startOnePixActivity
import com.gyf.cactus.ext.startRemoteService

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
    private val mOnePixReceiver = OnePixReceiver()

    /**
     * 是否在运行
     */
    private var mIsServiceRunning = false

    /**
     * 音乐是否在播放
     */
    private var mIsMusicRunning = false

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
                    }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.getParcelableExtra<CactusConfig>(Cactus.CACTUS_CONFIG)?.let {
            mCactusConfig = it
        }
        setNotification(mCactusConfig.notificationConfig)
        registerMedia()
        registerBroadcastReceiver()
        startRemoteService(mServiceConnection, mCactusConfig)
        doWork()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        onStop()
    }

    /**
     * 处理外部事情
     */
    private fun doWork() {
        if (!mIsServiceRunning) {
            log("LocalService is run!")
            sendBroadcast(Intent(Cactus.CACTUS_WORK))
            if (Cactus.CALLBACKS.isNotEmpty()) {
                Cactus.CALLBACKS.forEach {
                    it.doWork()
                }
            }
            mIsServiceRunning = true
        }
    }

    /**
     * 停止回调
     */
    private fun onStop() {
        log("LocalService is stop!")
        sendBroadcast(Intent(Cactus.CACTUS_STOP))
        if (Cactus.CALLBACKS.isNotEmpty()) {
            Cactus.CALLBACKS.forEach {
                it.onStop()
            }
        }
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
    }

    /**
     * 屏幕息屏亮屏广播
     */
    inner class OnePixReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.apply {
                when (this) {
                    Intent.ACTION_SCREEN_OFF -> {
                        // 熄屏，打开1像素Activity
                        log("screen off")
                        startOnePixActivity()
                        playMusic()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        //亮屏，关闭1像素Activity
                        log("screen on")
                        pauseMusic()
                    }
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
        registerReceiver(mOnePixReceiver, intentFilter)
    }

    /**
     * 注册音乐播放器
     */
    private fun registerMedia() {
        if (mCactusConfig.musicEnabled) {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer.create(this, mCactusConfig.musicId)
            }
            mMediaPlayer?.apply {
                if (!mCactusConfig.debug) {
                    setVolume(0f, 0f)
                }
                setOnCompletionListener {
                    Handler().postDelayed({ playMusic() }, mCactusConfig.repeatInterval)
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
        mMediaPlayer?.apply {
            if (mCactusConfig.musicEnabled) {
                if (!isScreenOn && !mIsMusicRunning) {
                    start()
                    mIsMusicRunning = true
                    log("music is playing")
                }
            } else {
                pauseMusic()
            }
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
        if (mCactusConfig.debug) {
            Log.d(Cactus.CACTUS_TAG, msg)
        }
    }
}