package com.gyf.cactus.service

import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.IBinder
import com.gyf.cactus.Cactus
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.Constant
import com.gyf.cactus.entity.ICactusInterface
import com.gyf.cactus.ext.*

/**
 * 本地服务
 *
 * @author geyifeng
 * @date 2019-08-28 17:05
 */
class LocalService : Service(), IBinder.DeathRecipient {

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
    private var mConnectionTimes = sTimes

    /**
     * 停止标识符
     */
    private var mIsStop = false

    /**
     * 是否已经绑定
     */
    private var mIsBind = false

    /**
     * 是否已经注册linkToDeath
     */
    private var mIsDeathBind = false

    private lateinit var mLocalBinder: LocalBinder

    private var mIInterface: ICactusInterface? = null

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            log("onServiceDisconnected")
            if (!mIsStop) {
                mIsBind = startRemoteService(this, mCactusConfig)
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            log("onServiceConnected")
            service?.let {
                mIInterface = ICactusInterface.Stub.asInterface(it)
                    ?.apply {
                        if (asBinder().isBinderAlive && asBinder().pingBinder()) {
                            try {
                                ++mConnectionTimes
                                wakeup(mCactusConfig)
                                connectionTimes(mConnectionTimes)
                                if (!mIsDeathBind) {
                                    mIsDeathBind = true
                                    asBinder().linkToDeath(this@LocalService, 0)
                                }
                            } catch (e: Exception) {
                                --mConnectionTimes
                            }
                        }
                    }
            }
        }
    }

    override fun binderDied() {
        log("binderDied")
        try {
            unlinkToDeath(mIInterface) {
                mIsDeathBind = false
                mIInterface = null
                if (!mIsStop) {
                    mIsBind = startRemoteService(mServiceConnection, mCactusConfig)
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onCreate() {
        super.onCreate()
        mCactusConfig = getConfig()
        registerStopReceiver {
            mIsStop = true
            sTimes = mConnectionTimes
            stopService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<CactusConfig>(Constant.CACTUS_CONFIG)?.let {
            mCactusConfig = it
        }
        setNotification(mCactusConfig.notificationConfig)
        mIsBind = startRemoteService(mServiceConnection, mCactusConfig)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopBind()
        stopService(Intent(this, RemoteService::class.java))
        onStop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        mLocalBinder = LocalBinder()
        return mLocalBinder
    }

    inner class LocalBinder : ICactusInterface.Stub() {

        override fun wakeup(config: CactusConfig) {
            mCactusConfig = config
        }

        override fun connectionTimes(time: Int) {
            mConnectionTimes = time
            if (mConnectionTimes > 3 && mConnectionTimes % 2 == 0) {
                ++mConnectionTimes
            }
            sTimes = mConnectionTimes
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
     * 解除相关绑定
     */
    private fun stopBind() {
        try {
            if (mIsDeathBind) {
                mIsDeathBind = false
                unlinkToDeath(mIInterface)
            }
            if (mIsBind) {
                unbindService(mServiceConnection)
                mIsBind = false
            }
        } catch (e: Exception) {
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
            setCrashRestart(times)
            if (Constant.CALLBACKS.isNotEmpty()) {
                Constant.CALLBACKS.forEach {
                    if (mCactusConfig.defaultConfig.workOnMainThread) {
                        sMainHandler.post { it.doWork(times) }
                    } else {
                        it.doWork(times)
                    }
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
            pauseMusic()
            mMediaPlayer = null
            if (Constant.CALLBACKS.isNotEmpty()) {
                Constant.CALLBACKS.forEach {
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
            sMainHandler.postDelayed({ startOnePixActivity() }, 1000)
        }
    }

    /**
     * 关闭一像素
     */
    private fun closeOnePix() {
        mCactusConfig.defaultConfig.apply {
            if (onePixEnabled) {
                backBackground()
                finishOnePix()
            }
        }
    }

    /**
     * 设置奔溃重启，google原生rom android 10 以下可以正常重启
     *
     * @param times Int
     */
    private fun setCrashRestart(times: Int) {
        if (times > 1 && sStartTimes == 1) {
            mCactusConfig.defaultConfig.restartIntent?.also {
                try {
                    PendingIntent.getActivity(this, 0, it, 0).send()
                } catch (e: Exception) {
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
        if (Constant.BACKGROUND_CALLBACKS.isNotEmpty()) {
            Constant.BACKGROUND_CALLBACKS.forEach {
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
                    sMainHandler.postDelayed(
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
}