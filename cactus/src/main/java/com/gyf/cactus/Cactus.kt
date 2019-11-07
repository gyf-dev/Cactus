package com.gyf.cactus

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import com.gyf.cactus.callback.CactusBackgroundCallback
import com.gyf.cactus.callback.CactusCallback
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.DefaultConfig
import com.gyf.cactus.entity.NotificationConfig
import com.gyf.cactus.ext.register
import com.gyf.cactus.pix.OnePixModel

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
     * 默认配置信息
     */
    private val mDefaultConfig = DefaultConfig()

    companion object {
        /**
         * 运行时回调广播ACTION
         */
        const val CACTUS_WORK = "com.gyf.cactus.work"
        /**
         * 停止时回调广播ACTION
         */
        const val CACTUS_STOP = "com.gyf.cactus.stop"
        /**
         * 后台回调广播ACTION
         */
        const val CACTUS_BACKGROUND = "com.gyf.cactus.background"
        /**
         * 前台后调广播ACTION
         */
        const val CACTUS_FOREGROUND = "com.gyf.cactus.foreground"
        /**
         * key，通过广播形式获取启动次数
         */
        const val CACTUS_TIMES = "times"
        internal const val CACTUS_TAG = "cactus"
        internal const val CACTUS_CONFIG = "cactusConfig"
        internal const val CACTUS_NOTIFICATION_CONFIG = "notificationConfig"
        internal val CALLBACKS = arrayListOf<CactusCallback>()
        internal val BACKGROUND_CALLBACKS = arrayListOf<CactusBackgroundCallback>()
        internal var mCactusConfig = CactusConfig()
        @JvmStatic
        val instance by lazy { Cactus() }
    }

    /**
     * 设置通知栏信息
     * @param notificationConfig NotificationConfig
     * @return Cactus
     */
    fun setNotificationConfig(notificationConfig: NotificationConfig) = apply {
        mNotificationConfig = notificationConfig
    }

    /**
     * 设置PendingIntent，用来处理通知栏点击事件
     * @param pendingIntent PendingIntent
     * @return Cactus
     */
    fun setPendingIntent(pendingIntent: PendingIntent) = apply {
        mNotificationConfig.pendingIntent = pendingIntent
    }

    /**
     * 是否隐藏通知栏，经测试，除了android 7.1手机之外都可以隐藏
     *
     * @param hide Boolean
     * @return Cactus
     */
    fun hideNotification(hide: Boolean) = apply {
        mNotificationConfig.hideNotification = hide
    }

    /**
     * 服务Id
     * @param serviceId Int
     * @return Cactus
     */
    fun setServiceId(serviceId: Int) = apply {
        mNotificationConfig.serviceId = serviceId
    }

    /**
     * 渠道Id
     * @param channelId String
     * @return Cactus
     */
    fun setChannelId(channelId: String) = apply {
        mNotificationConfig.channelId = channelId
    }

    /**
     * 渠道名
     * @param channelName String
     * @return Cactus
     */
    fun setChannelName(channelName: String) = apply {
        mNotificationConfig.channelName = channelName
    }

    /**
     * 通知栏标题
     * @param title String
     * @return Cactus
     */
    fun setTitle(title: String) = apply {
        mNotificationConfig.title = title
    }

    /**
     * 通知栏内容
     * @param content String
     * @return Cactus
     */
    fun setContent(content: String) = apply {
        mNotificationConfig.content = content
    }

    /**
     * 通知栏小图标
     * @param smallIcon Int
     * @return Cactus
     */
    fun setSmallIcon(smallIcon: Int) = apply {
        mNotificationConfig.smallIcon = smallIcon
    }

    /**
     * 通知栏大图标
     * @param largeIcon Int
     * @return Cactus
     */
    fun setLargeIcon(largeIcon: Int) = apply {
        mNotificationConfig.largeIcon = largeIcon
    }

    /**
     * 通知栏大图标
     * @param largeIcon Bitmap
     * @return Cactus
     */
    fun setLargeIcon(largeIcon: Bitmap) = apply {
        mNotificationConfig.largeIconBitmap = largeIcon
    }

    /**
     * 增加回调
     * @param cactusCallback CactusCallback
     * @return Cactus
     */
    fun addCallback(cactusCallback: CactusCallback) = apply {
        CALLBACKS.add(cactusCallback)
    }

    /**
     * 增加回调 lambda形式
     * @param stop Function0<Unit>
     * @param work Function1<Int, Unit>
     * @return Cactus
     */
    fun addCallback(stop: (() -> Unit)? = null, work: (Int) -> Unit) = apply {
        CALLBACKS.add(object : CactusCallback {
            override fun doWork(times: Int) {
                work(times)
            }

            override fun onStop() {
                stop?.let {
                    it()
                }
            }
        })
    }

    /**
     * 前后台切换回调
     *
     * @param cactusBackgroundCallback CactusBackgroundCallback
     */
    fun addBackgroundCallback(cactusBackgroundCallback: CactusBackgroundCallback) {
        BACKGROUND_CALLBACKS.add(cactusBackgroundCallback)
    }

    /**
     * 前后台切换回调
     *
     * @param block [@kotlin.ExtensionFunctionType] Function1<Boolean, Unit>
     */
    fun addBackgroundCallback(block: (Boolean) -> Unit) {
        BACKGROUND_CALLBACKS.add(object : CactusBackgroundCallback {
            override fun onBackground(background: Boolean) {
                block(background)
            }
        })
    }

    /**
     * 是否可以播放音乐
     * @param enabled Boolean
     * @return Cactus
     */
    fun setMusicEnabled(enabled: Boolean) = apply {
        mDefaultConfig.musicEnabled = enabled
    }

    /**
     * 后台是否可以播放音乐
     *
     * @param enabled Boolean
     * @return WaterBear
     */
    fun setBackgroundMusicEnabled(enabled: Boolean) = apply {
        mDefaultConfig.backgroundMusicEnabled = enabled
    }

    /**
     * 设置自定义音乐
     * @param musicId Int
     * @return Cactus
     */
    fun setMusicId(musicId: Int) = apply {
        mDefaultConfig.musicId = musicId
    }

    /**
     * 设置音乐间隔时间，时间间隔越长，越省电
     * @param repeatInterval Long
     * @return Cactus
     */
    fun setMusicInterval(repeatInterval: Long) = apply {
        if (repeatInterval >= 0L) {
            mDefaultConfig.repeatInterval = repeatInterval
        }
    }

    /**
     * 是否可以使用一像素
     * @param enabled Boolean
     * @return Cactus
     */
    fun setOnePixEnabled(enabled: Boolean) = apply {
        mDefaultConfig.onePixEnabled = enabled
    }

    /**
     * 一像素模式，感觉没啥用
     * @param onePixModel OnePixModel
     * @return Cactus
     */
    fun setOnePixModel(onePixModel: OnePixModel) = apply {
        mDefaultConfig.onePixModel = onePixModel
    }

    /**
     * 是否Debug模式
     * @param isDebug Boolean
     * @return Cactus
     */
    fun isDebug(isDebug: Boolean) = apply {
        mDefaultConfig.debug = isDebug
    }

    /**
     * 必须调用，建议在Application里初始化，使用Kotlin扩展函数不需要调用此方法
     * @param context Context
     */
    fun register(context: Context) {
        mCactusConfig = CactusConfig(
            mNotificationConfig,
            mDefaultConfig
        )
        context.register(mCactusConfig)
    }
}