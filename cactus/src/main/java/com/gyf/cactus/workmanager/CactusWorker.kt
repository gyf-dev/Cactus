package com.gyf.cactus.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.gyf.cactus.Cactus
import com.gyf.cactus.ext.isServiceRunning
import com.gyf.cactus.ext.register

/**
 * @author geyifeng
 * @date 2019-09-02 11:22
 */
class CactusWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        if (Cactus.mCactusConfig.defaultConfig.debug) {
            Log.d(
                Cactus.CACTUS_TAG,
                "CactusWorker-isServiceRunning${applicationContext.isServiceRunning}"
            )
        }
        if (!applicationContext.isServiceRunning) {
            applicationContext.register(Cactus.mCactusConfig)
        }
        return Result.success()
    }
}