package com.gyf.cactus

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gyf.cactus.ext.isMain
import com.gyf.cactus.ext.registerCactus
import com.gyf.cactus.service.JobHandlerService

/**
 * Cactus保活方案，Cactus有两种形式处理回调事件，
 * 第一种使用CactusCallback，
 * 第二种注册CACTUS_WORK和CACTUS_STOP广播监听器
 *
 * @author geyifeng
 * @date 2019-08-28 17:22
 */
class Cactus private constructor() {

    /**
     * 通知栏信息
     */
    private var mNotificationConfig = NotificationConfig()
    /**
     * debug模式
     */
    private var mDebug = false
    /**
     * 是否可以播放音乐
     */
    private var mMusicEnabled = true
    /**
     * 音乐资源ID
     */
    private var mMusicId = R.raw.cactus
    /**
     * 播放音乐间隔时间
     */
    private var mRepeatInterval: Long = 0L

    companion object {
        const val CACTUS_WORK = "com.gyf.cactus.work"
        const val CACTUS_STOP = "com.gyf.cactus.stop"
        internal const val CACTUS_TAG = "Cactus"
        internal const val CACTUS_CONFIG = "cactusConfig"
        internal const val CACTUS_NOTIFICATION_CONFIG = "notificationConfig"
        internal val CALLBACKS = arrayListOf<CactusCallback>()
        @JvmStatic
        val instance by lazy {
            Cactus()
        }
    }

    /**
     * 设置通知栏信息
     * @param notificationConfig NotificationConfig
     * @return Cactus
     */
    fun setNotificationConfig(notificationConfig: NotificationConfig) = run {
        mNotificationConfig = notificationConfig
        this
    }

    /**
     * 设置PendingIntent，用来处理通知栏点击事件
     * @param pendingIntent PendingIntent
     * @return Cactus
     */
    fun setPendingIntent(pendingIntent: PendingIntent) = run {
        mNotificationConfig.pendingIntent = pendingIntent
        this
    }

    /**
     * 是否隐藏通知栏，只支持sdk N(包含N)以下版本
     * @param hide Boolean
     * @return Cactus
     */
    fun hideNotification(hide: Boolean) = run {
        mNotificationConfig.hideNotification = hide
        this
    }

    /**
     * 服务Id
     * @param serviceId Int
     * @return Cactus
     */
    fun setServiceId(serviceId: Int) = run {
        mNotificationConfig.serviceId = serviceId
        this
    }

    /**
     * 渠道Id
     * @param channelId String
     * @return Cactus
     */
    fun setChannelId(channelId: String) = run {
        mNotificationConfig.channelId = channelId
        this
    }

    /**
     * 渠道名
     * @param channelName String
     * @return Cactus
     */
    fun setChannelName(channelName: String) = run {
        mNotificationConfig.channelName = channelName
        this
    }

    /**
     * 通知栏标题
     * @param title String
     * @return Cactus
     */
    fun setTitle(title: String) = run {
        mNotificationConfig.title = title
        this
    }

    /**
     * 通知栏内容
     * @param content String
     * @return Cactus
     */
    fun setContent(content: String) = run {
        mNotificationConfig.content = content
        this
    }

    /**
     * 通知栏小图标
     * @param smallIcon Int
     * @return Cactus
     */
    fun setSmallIcon(smallIcon: Int) = run {
        mNotificationConfig.smallIcon = smallIcon
        this
    }

    /**
     * 通知栏大图标
     * @param largeIcon Int
     * @return Cactus
     */
    fun setLargeIcon(largeIcon: Int) = run {
        mNotificationConfig.largeIcon = largeIcon
        this
    }

    /**
     * 增加回调
     * @param cactusCallback CactusCallback
     * @return Cactus
     */
    fun addCallback(cactusCallback: CactusCallback) = run {
        CALLBACKS.add(cactusCallback)
        this
    }

    /**
     * 是否可以播放音乐
     * @param enabled Boolean
     * @return Cactus
     */
    fun setMusicEnabled(enabled: Boolean) = run {
        mMusicEnabled = enabled
        this
    }

    /**
     * 设置自定义音乐
     * @param musicId Int
     * @return Cactus
     */
    fun setMusicId(musicId: Int) = run {
        mMusicId = musicId
        this
    }

    /**
     * 设置音乐间隔时间，时间间隔越长，越省电
     * @param repeatInterval Long
     * @return Cactus
     */
    fun setMusicInterval(repeatInterval: Long) = run {
        if (repeatInterval >= 0L) {
            mRepeatInterval = repeatInterval
        }
        this
    }

    /**
     * 是否Debug模式
     * @param isDebug Boolean
     * @return Cactus
     */
    fun isDebug(isDebug: Boolean) = run {
        mDebug = isDebug
        this
    }

    /**
     * 必须调用，建议在Application里初始化，使用Kotlin扩展函数不需要调用此方法
     * @param context Context
     */
    fun register(context: Context) {
        if (context.isMain) {
            val cactusConfig = CactusConfig(
                mDebug,
                mMusicEnabled,
                mRepeatInterval,
                mMusicId,
                mNotificationConfig
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(context, JobHandlerService::class.java)
                intent.putExtra(CACTUS_CONFIG, cactusConfig)
                context.startService(intent)
            } else {
                context.registerCactus(cactusConfig)
            }
        }
    }
}