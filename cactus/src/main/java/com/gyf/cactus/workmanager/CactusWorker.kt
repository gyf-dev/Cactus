package com.gyf.cactus.workmanager

import android.content.Context
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gyf.cactus.entity.Constant
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
            WorkManager.getInstance(context).cancelAllWorkByTag(Constant.CACTUS_FLAG_STOP)
        }
    }

    override fun doWork(): Result {
        return if (!mIsStop && !context.isStopFlag()) {
            context.apply {
                val cactusConfig = getConfig()
                if (cactusConfig.defaultConfig.debug) {
                    log("CactusWorker-isServiceRunning:$isServiceRunning")
                }
                if (!isServiceRunning) {
                    register(cactusConfig)
                }
            }
            Result.success()
        } else {
            Result.failure()
        }
    }
}