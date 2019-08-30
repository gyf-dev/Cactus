package com.gyf.cactus

/**
 * @author geyifeng
 * @date 2019-08-28 17:58
 */
interface CactusCallback {
    /**
     * do something
     */
    fun doWork()

    /**
     * 停止时调用
     */
    fun onStop()
}
