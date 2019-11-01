package com.gyf.cactus

/**
 * 前后台切换回调
 *
 * @author geyifeng
 * @date 2019-11-01 12:01
 */
interface CactusBackgroundCallback {
    /**
     * 前后台切换回调
     * @param background Boolean
     */
    fun onBackground(background: Boolean)
}