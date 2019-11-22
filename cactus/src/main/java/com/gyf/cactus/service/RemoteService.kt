package com.gyf.cactus.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.gyf.cactus.Cactus
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.ICactusInterface
import com.gyf.cactus.ext.*

/**
 * 远程服务
 *
 * @author geyifeng
 * @date 2019-08-28 17:05
 */
class RemoteService : Service() {

    /**
     * 配置信息
     */
    private lateinit var mCactusConfig: CactusConfig

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

    private lateinit var remoteBinder: RemoteBinder

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            if (!mIsStop) {
                mIsBind = startLocalService(this, mCactusConfig)
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
        }
    }

    override fun onCreate() {
        super.onCreate()
        mCactusConfig = getConfig()
        sCactusConfig?.apply {
            setNotification(notificationConfig)
        }
        registerStopReceiver {
            mIsStop = true
            sTimes = mConnectionTimes
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<CactusConfig>(Cactus.CACTUS_CONFIG)?.let {
            sCactusConfig = it
            mCactusConfig = it
        }
        mIsBind = startLocalService(mServiceConnection, mCactusConfig, false)
        setNotification(mCactusConfig.notificationConfig)
        log("RemoteService is run")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        if (mIsBind) {
            unbindService(mServiceConnection)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        remoteBinder = RemoteBinder()
        return remoteBinder
    }

    inner class RemoteBinder : ICactusInterface.Stub() {

        override fun wakeup(config: CactusConfig) {
            mCactusConfig = config
        }

        override fun connectionTimes(time: Int) {
            mConnectionTimes = time
        }
    }

    private fun log(msg: String) {
        if (mCactusConfig.defaultConfig.debug) {
            Log.d(Cactus.CACTUS_TAG, msg)
        }
    }
}