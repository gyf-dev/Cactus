package com.gyf.cactus

import android.os.Parcel
import android.os.Parcelable

/**
 * @author geyifeng
 * @date 2019-08-29 17:46
 */
data class CactusConfig(
    val debug: Boolean = false,
    val musicEnabled: Boolean = true,
    val repeatInterval: Long = 0L,
    val musicId: Int = R.raw.cactus,
    val notificationConfig: NotificationConfig = NotificationConfig()
) : Parcelable {
    constructor(source: Parcel) : this(
        1 == source.readInt(),
        1 == source.readInt(),
        source.readLong(),
        source.readInt(),
        source.readParcelable<NotificationConfig>(NotificationConfig::class.java.classLoader)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt((if (debug) 1 else 0))
        writeInt((if (musicEnabled) 1 else 0))
        writeLong(repeatInterval)
        writeInt(musicId)
        writeParcelable(notificationConfig, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CactusConfig> =
            object : Parcelable.Creator<CactusConfig> {
                override fun createFromParcel(source: Parcel): CactusConfig =
                    CactusConfig(source)

                override fun newArray(size: Int): Array<CactusConfig?> = arrayOfNulls(size)
            }
    }
}