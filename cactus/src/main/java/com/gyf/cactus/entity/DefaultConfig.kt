package com.gyf.cactus.entity

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.gyf.cactus.R

/**
 * 默认配置信息
 *
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
     * app退出到后台是否可以播放音乐
     */
    var backgroundMusicEnabled: Boolean = false,
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
     * 是否可以使用WorkManager
     */
    var workerEnabled: Boolean = true,
    /**
     * 奔溃是否可以重启
     */
    var crashRestartEnabled: Boolean = false,
    /**
     * 工作在主线程
     */
    var workOnMainThread: Boolean = false,
    /**
     * 重启Intent
     */
    var restartIntent: Intent? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        1 == source.readInt(),
        1 == source.readInt(),
        1 == source.readInt(),
        source.readLong(),
        source.readInt(),
        1 == source.readInt(),
        1 == source.readInt(),
        1 == source.readInt(),
        1 == source.readInt(),
        source.readParcelable<Intent>(Intent::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt((if (debug) 1 else 0))
        writeInt((if (musicEnabled) 1 else 0))
        writeInt((if (backgroundMusicEnabled) 1 else 0))
        writeLong(repeatInterval)
        writeInt(musicId)
        writeInt((if (onePixEnabled) 1 else 0))
        writeInt((if (workerEnabled) 1 else 0))
        writeInt((if (crashRestartEnabled) 1 else 0))
        writeInt((if (workOnMainThread) 1 else 0))
        writeParcelable(restartIntent, 0)
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