package com.gyf.cactus.service

import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.gyf.cactus.Cactus
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.ext.getConfig
import com.gyf.cactus.ext.isServiceRunning
import com.gyf.cactus.ext.registerCactus
import com.gyf.cactus.ext.setNotification

/**
 * @author geyifeng
 * @date 2019-08-30 13:03
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CactusJobService : JobService() {

    private lateinit var mCactusConfig: CactusConfig

    private val jobId = 100

    override fun onCreate() {
        super.onCreate()
        mCactusConfig = getConfig()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<CactusConfig>(Cactus.CACTUS_CONFIG)?.let {
            mCactusConfig = it
        }
        setNotification(mCactusConfig.notificationConfig)
        registerCactus(mCactusConfig)
        startJob()
        return Service.START_STICKY
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d(Cactus.CACTUS_TAG, "onStartJob")
        if (!isServiceRunning) {
            registerCactus(mCactusConfig)
        }
        return false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        Log.d(Cactus.CACTUS_TAG, "onStopJob")
        if (!isServiceRunning) {
            registerCactus(mCactusConfig)
        }
        return false
    }

    /**
     * 开始Job
     */
    private fun startJob() {
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(jobId)
        val builder = JobInfo.Builder(
            jobId,
            ComponentName(packageName, CactusJobService::class.java.name)
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS) //执行的最小延迟时间
                setOverrideDeadline(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)  //执行的最长延时时间
                setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)
                setBackoffCriteria(
                    JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS,
                    JobInfo.BACKOFF_POLICY_LINEAR
                )//线性重试方案
            } else {
                setPeriodic(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)
                setRequiresDeviceIdle(true)
            }
            setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            setRequiresCharging(true) // 当插入充电器，执行该任务
            setPersisted(true)
        }
        jobScheduler.schedule(builder.build())
    }
}
