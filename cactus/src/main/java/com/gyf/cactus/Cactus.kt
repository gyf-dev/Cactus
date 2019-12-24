package com.gyf.cactus

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import com.gyf.cactus.callback.CactusBackgroundCallback
import com.gyf.cactus.callback.CactusCallback
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.Constant
import com.gyf.cactus.entity.DefaultConfig
import com.gyf.cactus.entity.NotificationConfig
import com.gyf.cactus.ext.*

/**
 * Cactus保活方案，Cactus有两种形式处理回调事件，
 * 第一种使用CactusCallback，
 * 第二种注册CACTUS_WORK和CACTUS_STOP广播监听器。
 *
 * @author geyifeng
 * @date 2019-08-28 17:22
 */
class Cactus private constructor() {

    /**
     * 配置信息
     */
    private var mCactusConfig = CactusConfig()
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
        @JvmField
        val CACTUS_WORK = "work".fieldById
        /**
         * 停止时回调广播ACTION
         */
        @JvmField
        val CACTUS_STOP = "stop".fieldById
        /**
         * 后台回调广播ACTION
         */
        @JvmField
        val CACTUS_BACKGROUND = "background".fieldById
        /**
         * 前台后调广播ACTION
         */
        @JvmField
        val CACTUS_FOREGROUND = "foreground".fieldById
        /**
         * key，通过广播形式获取启动次数
         */
        const val CACTUS_TIMES = "times"
        @JvmStatic
        val instance by lazy { Cactus() }
    }

    /**
     * 设置notification，非必传，如果不传，将使用用户根据其他api设置的信息构建Notification
     *
     * @param notification Notification
     * @return Cactus
     */
    fun setNotification(notification: Notification) = apply {
        mNotificationConfig.notification = notification
    }

    /**
     * 设置NotificationChannel，非必传，如果不传，将使用默认的NotificationChannel
     *
     * @param notificationChannel NotificationChannel
     * @return Cactus
     */
    fun setNotificationChannel(notificationChannel: NotificationChannel) = apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationConfig.notificationChannel = notificationChannel
        }
    }

    /**
     * 是否隐藏通知栏，经测试，除了android 7.1手机之外都可以隐藏，默认隐藏，非必传
     *
     * @param hide Boolean
     * @return Cactus
     */
    fun hideNotification(hide: Boolean) = apply {
        mNotificationConfig.hideNotification = hide
    }

    /**
     * 是否隐藏Android 8.0以上通知栏
     *
     * @param hide Boolean
     * @return Cactus
     */
    fun hideNotificationAfterO(hide: Boolean) = apply {
        mNotificationConfig.hideNotificationAfterO = hide
    }

    /**
     * 设置PendingIntent，用来处理通知栏点击事件，非必传
     *
     * @param pendingIntent PendingIntent
     * @return Cactus
     */
    fun setPendingIntent(pendingIntent: PendingIntent) = apply {
        mNotificationConfig.pendingIntent = pendingIntent
    }

    /**
     * 服务Id，默认是1到Int.MAX_VALUE随机数，非必传
     *
     * @param serviceId Int
     * @return Cactus
     */
    fun setServiceId(serviceId: Int) = apply {
        mNotificationConfig.serviceId = serviceId
    }

    /**
     * 渠道Id，默认是Cactus，建议用户修改，非必传
     *
     * @param channelId String
     * @return Cactus
     */
    fun setChannelId(channelId: String) = apply {
        mNotificationConfig.channelId = channelId
    }

    /**
     * 渠道名，用于设置里通知渠道展示，默认是Cactus，建议用户修改，非必传
     *
     * @param channelName String
     * @return Cactus
     */
    fun setChannelName(channelName: String) = apply {
        mNotificationConfig.channelName = channelName
    }

    /**
     * 通知栏标题，默认是Cactus，建议用户修改，非必传
     *
     * @param title String
     * @return Cactus
     */
    fun setTitle(title: String) = apply {
        mNotificationConfig.title = title
    }

    /**
     * 通知栏内容，默认是Cactus is running，建议用户修改，非必传
     *
     * @param content String
     * @return Cactus
     */
    fun setContent(content: String) = apply {
        mNotificationConfig.content = content
    }

    /**
     * 设置RemoteViews（自定义布局），非必传
     *
     * @param remoteViews RemoteViews
     * @return Cactus
     */
    fun setRemoteViews(remoteViews: RemoteViews) = apply {
        mNotificationConfig.hideNotification = false
        mNotificationConfig.remoteViews = remoteViews
    }

    /**
     * 设置BigRemoteViews（自定义布局），非必传
     *
     * @param bigRemoteViews RemoteViews
     * @return Cactus
     */
    fun setBigRemoteViews(bigRemoteViews: RemoteViews) = apply {
        mNotificationConfig.hideNotification = false
        mNotificationConfig.bigRemoteViews = bigRemoteViews
    }

    /**
     * 通知栏小图标，默认是库里的图标，建议用户修改，非必传
     *
     * @param smallIcon Int
     * @return Cactus
     */
    fun setSmallIcon(smallIcon: Int) = apply {
        mNotificationConfig.smallIcon = smallIcon
    }

    /**
     * 通知栏大图标，默认是库里的图标，建议用户修改，非必传
     *
     * @param largeIcon Int
     * @return Cactus
     */
    fun setLargeIcon(largeIcon: Int) = apply {
        mNotificationConfig.largeIcon = largeIcon
    }

    /**
     * 通知栏大图标，非必传
     *
     * @param largeIcon Bitmap
     * @return Cactus
     */
    fun setLargeIcon(largeIcon: Bitmap) = apply {
        mNotificationConfig.largeIconBitmap = largeIcon
    }


    /**
     * 是否可以播放音乐，默认可以播放音乐，非必传
     *
     * @param enabled Boolean
     * @return Cactus
     */
    fun setMusicEnabled(enabled: Boolean) = apply {
        mDefaultConfig.musicEnabled = enabled
    }

    /**
     * 后台是否可以播放音乐，默认不可以后台播放音乐，非必传
     *
     * @param enabled Boolean
     * @return Cactus
     */
    fun setBackgroundMusicEnabled(enabled: Boolean) = apply {
        mDefaultConfig.backgroundMusicEnabled = enabled
    }

    /**
     * 设置自定义音乐，默认是无声音乐，非必传
     *
     * @param musicId Int
     * @return Cactus
     */
    fun setMusicId(musicId: Int) = apply {
        mDefaultConfig.musicId = musicId
    }

    /**
     * 设置音乐间隔时间，时间间隔越长，越省电，默认间隔时间是0，非必传
     *
     * @param repeatInterval Long
     * @return Cactus
     */
    fun setMusicInterval(repeatInterval: Long) = apply {
        if (repeatInterval >= 0L) {
            mDefaultConfig.repeatInterval = repeatInterval
        }
    }

    /**
     * 是否可以使用一像素，默认可以使用，只有在android p以下可以使用，非必传
     *
     * @param enabled Boolean
     * @return Cactus
     */
    fun setOnePixEnabled(enabled: Boolean) = apply {
        mDefaultConfig.onePixEnabled = enabled
    }

    /**
     * 是否可以使用WorkManager，默认可以使用，非必传
     *
     * @param enabled Boolean
     * @return WaterBear
     */
    fun setWorkerEnabled(enabled: Boolean) = apply {
        mDefaultConfig.workerEnabled = enabled
    }

    /**
     * 奔溃是否可以重启，默认为false，google原生rom android 10 以下可以正常重启，非必传
     *
     * @param enabled Boolean
     * @return WaterBear
     */
    fun setCrashRestartEnabled(enabled: Boolean) = apply {
        mDefaultConfig.crashRestartEnabled = enabled
    }

    /**
     * 是否Debug模式，默认没有调试信息，非必传
     *
     * @param isDebug Boolean
     * @return Cactus
     */
    fun isDebug(isDebug: Boolean) = apply {
        mDefaultConfig.debug = isDebug
    }

    /**
     * 增加回调，用于处理一些额外的工作，非必传
     *
     * @param cactusCallback CactusCallback
     * @return Cactus
     */
    fun addCallback(cactusCallback: CactusCallback) = apply {
        Constant.CALLBACKS.add(cactusCallback)
    }

    /**
     * 增加回调，lambda形式，用于处理一些额外的工作，非必传
     *
     * @param stop Function0<Unit>
     * @param work Function1<Int, Unit>
     * @return Cactus
     */
    fun addCallback(stop: (() -> Unit)? = null, work: (Int) -> Unit) = apply {
        Constant.CALLBACKS.add(object : CactusCallback {
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
     * 前后台切换回调，用于处理app前后台切换，非必传
     *
     * @param cactusBackgroundCallback CactusBackgroundCallback
     * @return Cactus
     */
    fun addBackgroundCallback(cactusBackgroundCallback: CactusBackgroundCallback) = apply {
        Constant.BACKGROUND_CALLBACKS.add(cactusBackgroundCallback)
    }

    /**
     * 前后台切换回调，用于处理app前后台切换，非必传
     *
     * @param block Function1<Boolean, Unit>
     * @return Cactus
     */
    fun addBackgroundCallback(block: (Boolean) -> Unit) = apply {
        Constant.BACKGROUND_CALLBACKS.add(object : CactusBackgroundCallback {
            override fun onBackground(background: Boolean) {
                block(background)
            }
        })
    }

    /**
     * 必须调用，建议在Application里初始化，使用Kotlin扩展函数不需要调用此方法
     *
     * @param context Context
     */
    fun register(context: Context) {
        mCactusConfig = CactusConfig(
            mNotificationConfig,
            mDefaultConfig
        )
        context.register(mCactusConfig)
    }

    /**
     * 注销，并不会立马停止，而是在1s之后停止，非必须调用，比如可以在app完全退出的时候可以调用，根据你的需求调用
     *
     * @param context Context
     */
    fun unregister(context: Context) = context.unregister()

    /**
     * 重启，与register区别在于不会重新配置CactusConfig信息，而是使用上一次配置的信息
     *
     * @param context Context
     */
    fun restart(context: Context) = context.restart()

    /**
     * 是否在运行
     */
    fun isRunning(context: Context) = context.isCactusRunning
}