package com.gyf.cactus.sample

import android.app.Activity
import android.os.Build
import java.util.*
import kotlin.system.exitProcess

/**
 * Activity管理类
 *
 * @author geyifeng
 * @date 2018/7/17
 */
class AppManager {

    companion object {
        val INSTANCE: AppManager = Holder.INSTANCE
    }

    private object Holder {
        val INSTANCE = AppManager()
    }

    private val stackActivity = Stack<Activity>()

    /**
     * 增加Activity
     *
     * @param activity Activity
     */
    fun addActivity(activity: Activity) {
        stackActivity.add(activity)
    }

    /**
     * 移除Activity
     *
     * @param activity Activity
     */
    fun removeActivity(activity: Activity) {
        activity.finish()
        stackActivity.remove(activity)
    }

    /**
     * 删除所有Activity
     */
    fun removeAllActivity() {
        for (activity in stackActivity) {
            activity.finish()
        }
        stackActivity.clear()
    }

    /**
     * 获得最顶部的Activity
     *
     * @return Activity
     */
    fun getTopActivity(): Activity? = if (stackActivity.isNotEmpty()) {
        stackActivity[stackActivity.size - 1]
    } else {
        null
    }

    /**
     * 是否有某个Activity
     *
     * @param clazz Class<T>
     * @return Boolean
     */
    fun <T : Activity> hasActivity(clazz: Class<T>): Boolean {
        stackActivity.forEach {
            if (it::class.java.name == clazz.name) {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    !(it.isDestroyed || it.isFinishing)
                } else {
                    !it.isFinishing
                }
            }
        }
        return false
    }

    /**
     * 退出
     */
    fun exitApp() {
        removeAllActivity()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(1)
    }
}