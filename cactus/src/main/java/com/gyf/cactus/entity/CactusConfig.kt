package com.gyf.cactus.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * 用户配置的信息
 * @author geyifeng
 * @date 2019-08-29 17:46
 */
data class CactusConfig(
    /**
     * 通知栏信息
     */
    val notificationConfig: NotificationConfig = NotificationConfig(),
    /**
     * 默认配置信息
     */
    val defaultConfig: DefaultConfig = DefaultConfig()
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readParcelable<NotificationConfig>(NotificationConfig::class.java.classLoader)!!,
        source.readParcelable<DefaultConfig>(DefaultConfig::class.java.classLoader)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(notificationConfig, 0)
        writeParcelable(defaultConfig, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CactusConfig> = object : Parcelable.Creator<CactusConfig> {
            override fun createFromParcel(source: Parcel): CactusConfig =
                CactusConfig(source)

            override fun newArray(size: Int): Array<CactusConfig?> = arrayOfNulls(size)
        }
    }
}