package com.gyf.cactus.ext

import android.content.Context
import com.google.gson.Gson
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.Constant

/**
 * 配置信息扩展
 *
 * @author geyifeng
 * @date 2019-11-19 13:34
 */

/**
 * 保存Stop标识符
 *
 * @receiver Context
 * @param boolean Boolean
 */
internal fun Context.saveStopFlag(boolean: Boolean) {
    getSharedPreferences(Constant.CACTUS_TAG, Context.MODE_PRIVATE).edit().apply {
        putBoolean(Constant.CACTUS_FLAG_STOP, boolean)
    }.apply()
}

/**
 * 获得Stop标识符
 *
 * @receiver Context
 */
internal fun Context.isStopFlag() = getSharedPreferences(
    Constant.CACTUS_TAG,
    Context.MODE_PRIVATE
).getBoolean(Constant.CACTUS_FLAG_STOP, false)

/**
 * 保存配置信息
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.saveConfig(cactusConfig: CactusConfig) {
    sCactusConfig = cactusConfig
    saveStopFlag(false)
    val serviceId = getServiceId()
    if (serviceId > 0) {
        cactusConfig.notificationConfig.serviceId = serviceId
    }
    getSharedPreferences(Constant.CACTUS_TAG, Context.MODE_PRIVATE).edit().apply {
        putString(Constant.CACTUS_CONFIG, Gson().toJson(cactusConfig))
        if (serviceId <= 0) {
            putInt(Constant.CACTUS_SERVICE_ID, cactusConfig.notificationConfig.serviceId)
        }
    }.apply()
}

/**
 * 获取配置信息
 *
 * @receiver Context
 * @return CactusConfig
 */
internal fun Context.getConfig() = sCactusConfig ?: getSharedPreferences(
    Constant.CACTUS_TAG,
    Context.MODE_PRIVATE
).getString(Constant.CACTUS_CONFIG, null)?.run {
    Gson().fromJson(this, CactusConfig::class.java)
} ?: CactusConfig()

/**
 * 获得serviceId
 *
 * @receiver Context
 * @return Int
 */
private fun Context.getServiceId() = getSharedPreferences(
    Constant.CACTUS_TAG,
    Context.MODE_PRIVATE
).getInt(Constant.CACTUS_SERVICE_ID, -1)