package com.gyf.cactus.entity

import com.gyf.cactus.callback.CactusBackgroundCallback
import com.gyf.cactus.callback.CactusCallback

/**
 * @author geyifeng
 * @date 2019-12-17 00:12
 */
internal object Constant {
    /**
     * 停止标识符
     */
    internal const val CACTUS_FLAG_STOP = "com.gyf.cactus.flag.stop"
    /**
     * tag
     */
    internal const val CACTUS_TAG = "cactus"
    /**
     * 配置信息
     */
    internal const val CACTUS_CONFIG = "cactusConfig"
    /**
     * 通知栏配置信息
     */
    internal const val CACTUS_NOTIFICATION_CONFIG = "notificationConfig"
    /**
     * 服务ID key
     */
    internal const val CACTUS_SERVICE_ID = "serviceId"
    /**
     * 回调集合
     */
    internal val CALLBACKS = arrayListOf<CactusCallback>()
    /**
     * 前后台回调集合
     */
    internal val BACKGROUND_CALLBACKS = arrayListOf<CactusBackgroundCallback>()
}