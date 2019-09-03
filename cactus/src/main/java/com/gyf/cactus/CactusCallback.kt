package com.gyf.cactus

/**
 * 增加时间监听回调函数
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
