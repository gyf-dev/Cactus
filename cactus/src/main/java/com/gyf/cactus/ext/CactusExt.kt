package com.gyf.cactus.ext

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.gyf.cactus.Cactus
import com.gyf.cactus.CactusConfig
import com.gyf.cactus.NotificationConfig
import com.gyf.cactus.activity.OnePixActivity
import com.gyf.cactus.service.CactusJobService
import com.gyf.cactus.service.HideForegroundService
import com.gyf.cactus.service.LocalService
import com.gyf.cactus.service.RemoteService
import java.lang.ref.WeakReference


/**
 * Cactus扩展
 * @author geyifeng
 * @date 2019-08-28 18:23
 */

/**
 * 用以保存一像素Activity
 */
private var mWeakReference: WeakReference<Activity>? = null

/**
 * kotlin里使用Cactus
 * @receiver Context
 * @param block [@kotlin.ExtensionFunctionType] Function1<Cactus, Unit>
 */
fun Context.cactus(block: Cactus.() -> Unit) =
    Cactus.instance.apply { block() }.register(this)

/**
 * 注册服务
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.registerCactus(cactusConfig: CactusConfig) {
    val intent = Intent(this, LocalService::class.java)
    intent.putExtra(Cactus.CACTUS_CONFIG, cactusConfig)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

/**
 * 注册JobService
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.registerJobCactus(cactusConfig: CactusConfig) {
    val intent = Intent(this, CactusJobService::class.java)
    intent.putExtra(Cactus.CACTUS_CONFIG, cactusConfig)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

/**
 * 设置通知栏信息
 * @receiver Service
 * @param notificationConfig NotificationConfig
 */
internal fun Service.setNotification(
    notificationConfig: NotificationConfig,
    isHideService: Boolean = false
) {
    notificationConfig.apply {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification =
            NotificationCompat.Builder(this@setNotification, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(smallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, largeIcon))
                .setContentIntent(pendingIntent)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //设置渠道
            val channel = NotificationChannel(
                channelId,
                notificationConfig.channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            notificationManager.createNotificationChannel(channel)
        }
        startForeground(serviceId, notification)
        notificationManager.notify(serviceId, notification)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 && hideNotification && !isHideService) {
            val intent = Intent(this@setNotification, HideForegroundService::class.java)
            intent.putExtra(Cactus.CACTUS_NOTIFICATION_CONFIG, this)
            startService(intent)
        }
    }
}

/**
 * 隐藏通知栏
 * @receiver Service
 * @param notificationConfig NotificationConfig
 */
internal fun Service.hideNotification(notificationConfig: NotificationConfig) {
    try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            val intent = Intent(this, HideForegroundService::class.java)
            intent.putExtra(Cactus.CACTUS_NOTIFICATION_CONFIG, notificationConfig)
            startService(intent)
        }
    } catch (e: Exception) {
    }
}

/**
 * 开启远程服务
 * @receiver Service
 * @param serviceConnection ServiceConnection
 * @param cactusConfig CactusConfig
 */
internal fun Service.startRemoteService(
    serviceConnection: ServiceConnection,
    cactusConfig: CactusConfig
) {
    val intent = Intent(this, RemoteService::class.java)
    intent.putExtra(Cactus.CACTUS_CONFIG, cactusConfig)
    startService(intent)
    bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT)
}

/**
 * 开启本地服务
 * @receiver Service
 * @param serviceConnection ServiceConnection
 * @param isStart Boolean
 * @param cactusConfig CactusConfig?
 */
internal fun Service.startLocalService(
    serviceConnection: ServiceConnection,
    isStart: Boolean = false,
    cactusConfig: CactusConfig? = null
) {
    val intent = Intent(this, LocalService::class.java)
    cactusConfig?.let {
        intent.putExtra(Cactus.CACTUS_CONFIG, it)
    }
    if (isStart) {
        startService(intent)
    }
    bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT)
}

/**
 * 保存一像素，方便销毁
 * @receiver OnePixActivity
 */
internal fun OnePixActivity.setOnePix() {
    if (mWeakReference == null) {
        mWeakReference = WeakReference(this)
    }
}

/**
 * 开启一像素界面
 * @receiver Context
 */
internal fun Context.startOnePixActivity() {
    val onePixIntent = Intent(this, OnePixActivity::class.java)
    onePixIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    onePixIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val pendingIntent = PendingIntent.getActivity(this, 0, onePixIntent, 0)
    try {
        pendingIntent.send()
    } catch (e: Exception) {
    }
}

/**
 * 销毁一像素
 */
internal fun finishOnePix() {
    mWeakReference?.apply {
        get()?.apply {
            finish()
        }
        mWeakReference = null
    }
}

/**
 * 屏幕是否亮屏
 */
internal val Context.isScreenOn
    get() = run {
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.isScreenOn
    }

/**
 * 是否主进程
 */
internal val Context.isMain
    get() = run {
        val pid = android.os.Process.myPid()
        var processName = ""
        val mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (mActivityManager.runningAppProcesses != null) {
            for (appProcess in mActivityManager.runningAppProcesses) {
                if (appProcess.pid == pid) {
                    processName = appProcess.processName
                    break
                }
            }
            if (processName == packageName) {
                return@run true
            }
        }
        false
    }

/**
 * Cactus是否在运行中
 */
internal val Context.isServiceRunning
    get() = run {
        isServiceRunning("com.gyf.cactus.service.LocalService") and isRunningTaskExist(":cactusRemoteService")
    }

/**
 * 判断服务是否在运行
 * @receiver Context
 * @param className String
 * @return Boolean
 */
internal fun Context.isServiceRunning(className: String): Boolean {
    var isRunning = false
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val servicesList = activityManager.getRunningServices(Integer.MAX_VALUE)
    if (servicesList != null) {
        val l = servicesList.iterator()
        while (l.hasNext()) {
            val si = l.next()
            if (className == si.service.className) {
                isRunning = true
            }
        }
    }
    return isRunning
}

/**
 * 判断任务是否在运行
 * @receiver Context
 * @param processName String
 * @return Boolean
 */
internal fun Context.isRunningTaskExist(processName: String): Boolean {
    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val processList = am.runningAppProcesses
    if (processList != null) {
        for (info in processList) {
            if (info.processName == processName) {
                return true
            }
        }
    }
    return false
}