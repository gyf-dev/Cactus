package com.gyf.cactus.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gyf.cactus.ext.*

/**
 * WorkManager定时器
 *
 * @author geyifeng
 * @date 2019-09-02 11:22
 */
class CactusWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    /**
     * 停止标识符
     */
    private var mIsStop = false

    init {
        context.registerStopReceiver {
            mIsStop = true
        }
    }

    override fun doWork(): Result {
        context.apply {
            val cactusConfig = getConfig()
            log("${toString()}-doWork")
            if (!isCactusRunning && !mIsStop && !isStopped) {
                register(cactusConfig)
            }
        }
        return Result.success()
    }
}