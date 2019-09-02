package com.gyf.cactus

import android.app.PendingIntent
import android.os.Parcel
import android.os.Parcelable

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
    var channelId: String = "WaterBear",
    /**
     * 渠道名
     */
    var channelName: String = "WaterBear",
    /**
     * 标题名
     */
    var title: String = "WaterBear",
    /**
     * 通知栏内容
     */
    var content: String = "WaterBear is running",
    /**
     * 小图标
     */
    var smallIcon: Int = R.drawable.icon_cactus_small,
    /**
     * 大图标
     */
    var largeIcon: Int = R.drawable.icon_cactus_large,
    /**
     * 是否隐藏通知栏，对于 Android O以下有效
     */
    var hideNotification: Boolean = true,
    /**
     * 点击标题栏跳转事件
     */
    var pendingIntent: PendingIntent? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readInt(),
        source.readInt(),
        1 == source.readInt(),
        source.readParcelable<PendingIntent>(PendingIntent::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(serviceId)
        writeString(channelId)
        writeString(channelName)
        writeString(title)
        writeString(content)
        writeInt(smallIcon)
        writeInt(largeIcon)
        writeInt((if (hideNotification) 1 else 0))
        writeParcelable(pendingIntent, 0)
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