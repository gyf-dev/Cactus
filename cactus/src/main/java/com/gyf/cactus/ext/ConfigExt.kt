package com.gyf.cactus.ext

import android.content.Context
import com.google.gson.Gson
import com.gyf.cactus.Cactus
import com.gyf.cactus.entity.CactusConfig

/**
 * 配置信息扩展
 *
 * @author geyifeng
 * @date 2019-11-19 13:34
 */

/**
 * 保存配置信息
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.saveConfig(cactusConfig: CactusConfig) {
    val serviceId = getServiceId()
    mCactusConfig = cactusConfig
    if (serviceId > 0) {
        cactusConfig.notificationConfig.serviceId = serviceId
    }
    getSharedPreferences(Cactus.CACTUS_TAG, Context.MODE_PRIVATE).edit().apply {
        putString(Cactus.CACTUS_CONFIG, Gson().toJson(cactusConfig))
        if (serviceId <= 0) {
            putInt(Cactus.CACTUS_SERVICE_ID, cactusConfig.notificationConfig.serviceId)
        }
    }.apply()
}

/**
 * 获取配置信息
 *
 * @receiver Context
 * @return CactusConfig
 */
internal fun Context.getConfig() = mCactusConfig ?: getSharedPreferences(
    Cactus.CACTUS_TAG,
    Context.MODE_PRIVATE
).getString(Cactus.CACTUS_CONFIG, null)?.run {
    Gson().fromJson(this, CactusConfig::class.java)
} ?: CactusConfig()

/**
 * 获得serviceId
 *
 * @receiver Context
 * @return Int
 */
private fun Context.getServiceId() = getSharedPreferences(
    Cactus.CACTUS_TAG,
    Context.MODE_PRIVATE
).getInt(Cactus.CACTUS_SERVICE_ID, -1)