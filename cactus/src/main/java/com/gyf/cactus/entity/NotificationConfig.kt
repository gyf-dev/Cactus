package com.gyf.cactus.entity

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import android.widget.RemoteViews
import com.gyf.cactus.R

/**
 * 通知栏信息
 *
 * @author geyifeng
 * @date 2019-08-28 17:10
 */
data class NotificationConfig(
    /**
     * 服务id
     */
    var serviceId: Int = (1..Int.MAX_VALUE).random(),
    /**
     * 渠道id
     */
    var channelId: String = "Cactus",
    /**
     * 渠道名
     */
    var channelName: String = "Cactus",
    /**
     * 标题名
     */
    var title: String = "Cactus",
    /**
     * 通知栏内容
     */
    var content: String = "The app of cactus is running",
    /**
     * 小图标
     */
    var smallIcon: Int = R.drawable.icon_cactus_small,
    /**
     * 大图标
     */
    var largeIcon: Int = R.drawable.icon_cactus_large,
    /**
     * 大图标
     */
    var largeIconBitmap: Bitmap? = null,
    /**
     * 是否隐藏通知栏，对于 Android O以下有效
     */
    var hideNotification: Boolean = true,
    /**
     * 点击标题栏跳转事件
     */
    var pendingIntent: PendingIntent? = null,
    /**
     * 自定义布局
     */
    var remoteViews: RemoteViews? = null,
    /**
     * 自定义大布局
     */
    var bigRemoteViews: RemoteViews? = null,
    /**
     * 用户传入的Notification，使用该属性，以上配置就不会生效
     */
    var notification: Notification? = null,
    /**
     * 用户传入的NotificationChannel
     */
    var notificationChannel: Parcelable? = null
) : Parcelable {
    @SuppressLint("NewApi")
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readInt(),
        source.readInt(),
        source.readParcelable<Bitmap>(Bitmap::class.java.classLoader),
        1 == source.readInt(),
        source.readParcelable<PendingIntent>(PendingIntent::class.java.classLoader),
        source.readParcelable<RemoteViews>(RemoteViews::class.java.classLoader),
        source.readParcelable<RemoteViews>(RemoteViews::class.java.classLoader),
        source.readParcelable<Notification>(Notification::class.java.classLoader),
        source.readParcelable<Parcelable>(Parcelable::class.java.classLoader)
    )

    override fun describeContents() = 0

    @SuppressLint("NewApi")
    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(serviceId)
        writeString(channelId)
        writeString(channelName)
        writeString(title)
        writeString(content)
        writeInt(smallIcon)
        writeInt(largeIcon)
        writeParcelable(largeIconBitmap, 0)
        writeInt((if (hideNotification) 1 else 0))
        writeParcelable(pendingIntent, 0)
        writeParcelable(remoteViews, 0)
        writeParcelable(bigRemoteViews, 0)
        writeParcelable(notification, 0)
        writeParcelable(notificationChannel, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<NotificationConfig> =
            object : Parcelable.Creator<NotificationConfig> {
                override fun createFromParcel(source: Parcel): NotificationConfig =
                    NotificationConfig(source)

                override fun newArray(size: Int): Array<NotificationConfig?> = arrayOfNulls(size)
            }
    }
}