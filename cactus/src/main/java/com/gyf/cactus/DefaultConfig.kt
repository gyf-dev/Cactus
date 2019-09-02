package com.gyf.cactus

import android.os.Parcel
import android.os.Parcelable

/**
 * 默认配置信息
 * @author geyifeng
 * @date 2019-09-02 09:44
 */
data class DefaultConfig(
    /**
     * 是否debug
     */
    var debug: Boolean = false,
    /**
     * 是否可以使用后台音乐
     */
    var musicEnabled: Boolean = true,
    /**
     * 音乐播放循环间隔
     */
    var repeatInterval: Long = 0L,
    /**
     * 音乐播放声源
     */
    var musicId: Int = R.raw.cactus,
    /**
     * 是否可以使用一像素
     */
    var onePixEnabled: Boolean = true,
    /**
     * 一像素模式
     */
    var onePixModel: OnePixModel = OnePixModel.DEFAULT
) : Parcelable {
    constructor(source: Parcel) : this(
        1 == source.readInt(),
        1 == source.readInt(),
        source.readLong(),
        source.readInt(),
        1 == source.readInt(),
        OnePixModel.values()[source.readInt()]
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt((if (debug) 1 else 0))
        writeInt((if (musicEnabled) 1 else 0))
        writeLong(repeatInterval)
        writeInt(musicId)
        writeInt((if (onePixEnabled) 1 else 0))
        writeInt(onePixModel.ordinal)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DefaultConfig> =
            object : Parcelable.Creator<DefaultConfig> {
                override fun createFromParcel(source: Parcel): DefaultConfig = DefaultConfig(source)
                override fun newArray(size: Int): Array<DefaultConfig?> = arrayOfNulls(size)
            }
    }
}