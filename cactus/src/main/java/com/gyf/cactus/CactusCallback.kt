package com.gyf.cactus

/**
 * 监听回调
 *
 * @author geyifeng
 * @date 2019-08-28 17:58
 */
interface CactusCallback {

    /**
     * do something
     * @param times Int 连接次数
     */
    fun doWork(times: Int)

    /**
     * 停止时调用
     */
    fun onStop()
}
