package com.gyf.cactus.workmanager

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gyf.cactus.Cactus
import com.gyf.cactus.ext.getConfig
import com.gyf.cactus.ext.isServiceRunning
import com.gyf.cactus.ext.register
import com.gyf.cactus.ext.registerStopReceiver

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
            WorkManager.getInstance(context).cancelAllWorkByTag(Cactus.CACTUS_FLAG_STOP)
        }
    }

    override fun doWork(): Result {
        return if (!mIsStop) {
            val cactusConfig = context.getConfig()
            if (cactusConfig.defaultConfig.debug) {
                Log.d(
                    Cactus.CACTUS_TAG,
                    "CactusWorker-isServiceRunning${applicationContext.isServiceRunning}"
                )
            }
            if (!applicationContext.isServiceRunning) {
                applicationContext.register(cactusConfig)
            }
            Result.success()
        } else {
            Result.failure()
        }
    }
}