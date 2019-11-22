package com.gyf.cactus.service

import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.gyf.cactus.Cactus
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.ICactusInterface
import com.gyf.cactus.ext.*
import com.gyf.cactus.pix.OnePixModel

/**
 * 本地服务
 *
 * @author geyifeng
 * @date 2019-08-28 17:05
 */
class LocalService : Service() {

    /**
     * 配置信息
     */
    private lateinit var mCactusConfig: CactusConfig
    /**
     * 音乐播放器
     */
    private var mMediaPlayer: MediaPlayer? = null

    /**
     * 广播
     */
    private var mServiceReceiver: ServiceReceiver? = null

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
    private var mConnectionTimes = mTimes

    /**
     * 停止标识符
     */
    private var mIsStop = false

    /**
     * 是否已经绑定
     */
    private var mIsBind = false

    private lateinit var mLocalBinder: LocalBinder

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            if (!mIsStop) {
                mIsBind = startRemoteService(this, mCactusConfig)
                setNotification(mCactusConfig.notificationConfig)
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service?.let {
                ICactusInterface.Stub.asInterface(it)
                    ?.apply {
                        if (asBinder().isBinderAlive) {
                            ++mConnectionTimes
                            try {
                                wakeup(mCactusConfig)
                                connectionTimes(mConnectionTimes)
                            } catch (e: Exception) {
                                --mConnectionTimes
                            }
                        }
                    }
            }
            setNotification(mCactusConfig.notificationConfig)
        }
    }

    override fun onCreate() {
        super.onCreate()
        mCactusConfig = getConfig()
        setNotification(mCactusConfig.notificationConfig)
        registerStopReceiver {
            mIsStop = true
            mTimes = mConnectionTimes
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<CactusConfig>(Cactus.CACTUS_CONFIG)?.let {
            mCactusConfig = it
        }
        mIsBind = startRemoteService(mServiceConnection, mCactusConfig)
        setNotification(mCactusConfig.notificationConfig)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        onStop()
        if (mIsBind) {
            unbindService(mServiceConnection)
        }
        stopService(Intent(this, RemoteService::class.java))
    }

    override fun onBind(intent: Intent?): IBinder? {
        mLocalBinder = LocalBinder()
        return mLocalBinder
    }

    inner class LocalBinder : ICactusInterface.Stub() {

        override fun wakeup(config: CactusConfig) {
            mCactusConfig = config
            setNotification(mCactusConfig.notificationConfig)
        }

        override fun connectionTimes(time: Int) {
            mConnectionTimes = time
            doWork((mConnectionTimes + 1) / 2)
        }
    }

    /**
     * 屏幕息屏亮屏与前后台切换广播
     */
    inner class ServiceReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.apply {
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
                        if (!mCactusConfig.defaultConfig.backgroundMusicEnabled) {
                            pauseMusic()
                        }
                    }
                    Cactus.CACTUS_BACKGROUND -> {
                        log("background")
                        if (mCactusConfig.defaultConfig.backgroundMusicEnabled) {
                            playMusic()
                        }
                        onBackground(true)
                    }
                    Cactus.CACTUS_FOREGROUND -> {
                        log("foreground")
                        pauseMusic()
                        onBackground(false)
                    }
                }
            }
        }
    }

    /**
     * 处理外部事情
     *
     * @param times Int，启动次数
     */
    private fun doWork(times: Int) {
        if (!mIsServiceRunning) {
            mIsServiceRunning = true
            log("LocalService is run >>>> do work times = $times")
            registerMedia()
            registerBroadcastReceiver()
            sendBroadcast(
                Intent(Cactus.CACTUS_WORK).putExtra(
                    Cactus.CACTUS_TIMES,
                    times
                )
            )
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
        if (mIsServiceRunning) {
            mIsServiceRunning = false
            log("LocalService is stop!")
            unregisterReceiver()
            sendBroadcast(Intent(Cactus.CACTUS_STOP))
            if (Cactus.CALLBACKS.isNotEmpty()) {
                Cactus.CALLBACKS.forEach {
                    it.onStop()
                }
            }
        }
    }

    /**
     * 打开一像素
     */
    private fun openOnePix() {
        if (mCactusConfig.defaultConfig.onePixEnabled) {
            mMainHandler.postDelayed({ startOnePixActivity() }, 1000)
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
     * 是否是在后台
     *
     * @param background Boolean
     */
    private fun onBackground(background: Boolean) {
        if (Cactus.BACKGROUND_CALLBACKS.isNotEmpty()) {
            Cactus.BACKGROUND_CALLBACKS.forEach {
                it.onBackground(background)
            }
        }
    }

    /**
     * 注册息屏亮屏、前后台切换广播监听
     */
    private fun registerBroadcastReceiver() {
        if (mServiceReceiver == null) {
            mServiceReceiver = ServiceReceiver()
        }
        mServiceReceiver?.let {
            registerReceiver(it, IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Cactus.CACTUS_BACKGROUND)
                addAction(Cactus.CACTUS_FOREGROUND)
            })
        }
    }

    /**
     * 注销息屏亮屏、前后台切换广播监听
     */
    private fun unregisterReceiver() {
        mServiceReceiver?.let {
            unregisterReceiver(it)
            mServiceReceiver = null
        }
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
                    mMainHandler.postDelayed(
                        {
                            mIsMusicRunning = false
                            playMusic()
                        },
                        mCactusConfig.defaultConfig.repeatInterval
                    )
                }
                setOnErrorListener { _, _, _ -> false }
                if (!isScreenOn) {
                    playMusic()
                }
            }
        }
    }

    /**
     * 播放音乐
     */
    private fun playMusic() {
        mMediaPlayer?.apply {
            if (mCactusConfig.defaultConfig.musicEnabled) {
                if (!mIsMusicRunning) {
                    start()
                    mIsMusicRunning = true
                    log("music is playing")
                }
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
     *
     * @param msg String
     */
    private fun log(msg: String) {
        if (mCactusConfig.defaultConfig.debug) {
            Log.d(Cactus.CACTUS_TAG, msg)
        }
    }
}