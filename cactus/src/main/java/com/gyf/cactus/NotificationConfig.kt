package com.gyf.cactus

import android.app.PendingIntent
import android.os.Parcel
import android.os.Parcelable

/**
 * @author geyifeng
 * @date 2019-08-28 17:10
 */
data class NotificationConfig(
    var serviceId: Int = (1..Int.MAX_VALUE).random(),
    var channelId: String = "Cactus",
    var channelName: String = "Cactus",
    var title: String = "Cactus",
    var content: String = "The app of cactus is running",
    var smallIcon: Int = R.drawable.icon_cactus_small,
    var largeIcon: Int = R.drawable.icon_cactus_large,
    var hideNotification: Boolean = true,
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