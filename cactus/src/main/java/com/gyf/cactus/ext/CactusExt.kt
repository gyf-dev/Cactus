package com.gyf.cactus.ext

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.gyf.cactus.Cactus
import com.gyf.cactus.callback.AppBackgroundCallback
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.entity.Constant
import com.gyf.cactus.exception.CactusUncaughtExceptionHandler
import com.gyf.cactus.pix.OnePixActivity
import com.gyf.cactus.receiver.StopReceiver
import com.gyf.cactus.service.CactusJobService
import com.gyf.cactus.service.LocalService
import com.gyf.cactus.service.RemoteService
import com.gyf.cactus.workmanager.CactusWorker
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Cactus扩展
 *
 * @author geyifeng
 * @date 2019-08-28 18:23
 */

/**
 * 用以保存一像素Activity
 */
private var mWeakReference: WeakReference<Activity>? = null

/**
 * 用来表示是前台还是后台
 */
private var mIsForeground = false

/**
 * 是否注册过
 */
private var sRegistered = false

/**
 * 主Handler
 */
internal val sMainHandler by lazy {
    Handler(Looper.getMainLooper())
}

/**
 * 启动次数
 */
internal var sTimes = 0

/**
 * 启动次数，用以判断是否使用奔溃重启
 */
internal var sStartTimes = 0

/**
 * 配置信息
 */
internal var sCactusConfig: CactusConfig? = null

/**
 * 前后台切换监听
 */
private var mAppBackgroundCallback: AppBackgroundCallback? = null

/**
 * kotlin里使用Cactus
 *
 * @receiver Context
 * @param block [@kotlin.ExtensionFunctionType] Function1<Cactus, Unit>
 */
fun Context.cactus(block: Cactus.() -> Unit) =
    Cactus.instance.apply { block() }.register(this)

/**
 * 注销
 *
 * @receiver Context
 */
fun Context.cactusUnregister() = Cactus.instance.unregister(this)

/**
 * 重启
 *
 * @receiver Context
 */
fun Context.cactusRestart() = Cactus.instance.restart(this)

/**
 * 更新通知栏
 *
 * @receiver Context
 * @param block [@kotlin.ExtensionFunctionType] Function1<Cactus, Unit>
 */
fun Context.cactusUpdateNotification(block: Cactus.() -> Unit) =
    Cactus.instance.apply { block() }.updateNotification(this)

/**
 * 是否已经停止
 *
 * @receiver Context
 * @return Boolean
 */
val Context.cactusIsRunning
    get() = Cactus.instance.isRunning(this)

/**
 * kotlin里使用注册Receiver
 *
 * @receiver Context
 * @param block Function0<Unit>
 */
internal fun Context.registerStopReceiver(block: () -> Unit) =
    StopReceiver.newInstance(this).register(block)

/**
 * 注册Cactus服务
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.register(cactusConfig: CactusConfig) {
    if (isMain) {
        try {
            if (sRegistered && isCactusRunning) {
                log("Cactus is running，Please stop Cactus before registering!!")
            } else {
                sStartTimes++
                sRegistered = true
                handleRestartIntent(cactusConfig)
                saveConfig(cactusConfig)
                CactusUncaughtExceptionHandler.instance
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    registerJobCactus(cactusConfig)
                } else {
                    registerCactus(cactusConfig)
                }
                if (this is Application && mAppBackgroundCallback == null) {
                    mAppBackgroundCallback = AppBackgroundCallback(this)
                    registerActivityLifecycleCallbacks(mAppBackgroundCallback)
                }
                mAppBackgroundCallback?.useCallback(true)
            }
        } catch (e: Exception) {
            log("Unable to open cactus service!!")
        }
    }
}

/**
 * 注销Cactus
 *
 * @receiver Context
 */
internal fun Context.unregister() {
    try {
        if (isCactusRunning && sRegistered) {
            sRegistered = false
            sCactusConfig?.apply {
                if (defaultConfig.workerEnabled) {
                    unregisterWorker()
                }
            }
            sendBroadcast(Intent("${Constant.CACTUS_FLAG_STOP}.$packageName"))
            sMainHandler.postDelayed({
                mAppBackgroundCallback?.also {
                    it.useCallback(false)
                    if (this is Application) {
                        unregisterActivityLifecycleCallbacks(it)
                        mAppBackgroundCallback = null
                    }
                }
            }, 1000)
        } else {
            log("Cactus is not running，Please make sure Cactus is running!!")
        }
    } catch (e: Exception) {
    }
}

/**
 * 重新启动
 *
 * @receiver Context
 */
internal fun Context.restart() = register(getConfig())

/**
 * 更新通知栏
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.updateNotification(cactusConfig: CactusConfig) {
    if (!getConfig().notificationConfig.canUpdate(cactusConfig.notificationConfig)) {
        return
    }
    saveConfig(cactusConfig)
    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = getNotification(cactusConfig.notificationConfig)
    notificationManager.notify(cactusConfig.notificationConfig.serviceId, notification)
}

/**
 * 最终都将调用此方法，注册Cactus服务
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.registerCactus(cactusConfig: CactusConfig) {
    val intent = Intent(this, LocalService::class.java)
    intent.putExtra(Constant.CACTUS_CONFIG, cactusConfig)
    startInternService(intent)
    sMainHandler.postDelayed({
        if (cactusConfig.defaultConfig.workerEnabled) {
            registerWorker()
        } else {
            unregisterWorker()
        }
    }, 5000)
}

/**
 * 注册JobService
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.registerJobCactus(cactusConfig: CactusConfig) {
    val intent = Intent(this, CactusJobService::class.java)
    intent.putExtra(Constant.CACTUS_CONFIG, cactusConfig)
    startInternService(intent)
}

/**
 * 开启WorkManager
 *
 * @receiver Context
 */
internal fun Context.registerWorker() {
    if (isCactusRunning && sRegistered) {
        try {
            val workRequest =
                PeriodicWorkRequest.Builder(CactusWorker::class.java, 15, TimeUnit.SECONDS)
                    .build()
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                CactusWorker::class.java.name,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            unregisterWorker()
            log("WorkManager registration failed")
        }
    }
}

/**
 * 取消WorkManager
 *
 * @receiver Context
 * @return Operation
 */
internal fun Context.unregisterWorker() =
    WorkManager.getInstance().cancelUniqueWork(CactusWorker::class.java.name)

/**
 * 开启远程服务
 *
 * @receiver Service
 * @param serviceConnection ServiceConnection
 * @param cactusConfig CactusConfig
 */
internal fun Service.startRemoteService(
    serviceConnection: ServiceConnection,
    cactusConfig: CactusConfig
) = startAndBindService(RemoteService::class.java, serviceConnection, cactusConfig)

/**
 * 开启本地服务
 *
 * @receiver Service
 * @param serviceConnection ServiceConnection
 * @param cactusConfig CactusConfig
 * @param isStart Boolean
 */
internal fun Service.startLocalService(
    serviceConnection: ServiceConnection,
    cactusConfig: CactusConfig,
    isStart: Boolean = true
) = startAndBindService(LocalService::class.java, serviceConnection, cactusConfig, isStart)

/**
 * 开启并绑定服务
 *
 * @receiver Service
 * @param cls Class<*>
 * @param serviceConnection ServiceConnection
 * @param cactusConfig CactusConfig
 * @param isStart Boolean
 * @return Boolean
 */
private fun Service.startAndBindService(
    cls: Class<*>,
    serviceConnection: ServiceConnection,
    cactusConfig: CactusConfig,
    isStart: Boolean = true
) = run {
    val intent = Intent(this, cls)
    intent.putExtra(Constant.CACTUS_CONFIG, cactusConfig)
    if (isStart) {
        startInternService(intent)
    }
    val bindService = bindService(intent, serviceConnection, Context.BIND_IMPORTANT)
    bindService
}

/**
 * 开启Service
 *
 * @receiver Context
 * @param intent Intent
 */
internal fun Context.startInternService(intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

/**
 * 停止服务
 *
 * @receiver Service
 */
internal fun Service.stopService() {
    sMainHandler.postDelayed({
        try {
            this.stopSelf()
        } catch (e: Exception) {
        }
    }, 1000)
}

/**
 * 设置重启Intent
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
private fun Context.handleRestartIntent(cactusConfig: CactusConfig) {
    cactusConfig.defaultConfig.apply {
        if (crashRestartEnabled) {
            restartIntent = packageManager.getLaunchIntentForPackage(packageName)
            restartIntent?.apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            restartIntent = null
        }
    }
}

/**
 * 开启一像素界面
 *
 * @receiver Context
 */
internal fun Context.startOnePixActivity() {
    if (!isScreenOn && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        mIsForeground = isForeground
        log("isForeground:$mIsForeground")
        val onePixIntent = Intent(this, OnePixActivity::class.java)
        onePixIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        onePixIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, onePixIntent, 0)
        try {
            pendingIntent.send()
        } catch (e: Exception) {
        }
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
 * 保存一像素，方便销毁
 *
 * @receiver OnePixActivity
 */
internal fun OnePixActivity.setOnePix() {
    if (mWeakReference == null) {
        mWeakReference = WeakReference(this)
    }
}

/**
 * 退到后台
 *
 * @receiver Context
 */
internal fun backBackground() {
    mWeakReference?.apply {
        get()?.apply {
            if (!mIsForeground && isScreenOn) {
                val home = Intent(Intent.ACTION_MAIN)
                home.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                home.addCategory(Intent.CATEGORY_HOME)
                startActivity(home)
            }
        }
    }
}

/**
 * WaterBear是否在运行中
 */
internal val Context.isCactusRunning
    get() = run {
        isServiceRunning(LocalService::class.java.name) and isRunningTaskExist(Constant.CACTUS_EMOTE_SERVICE)
    }

/**
 * 获得带id值的字段值
 */
internal val String.fieldById get() = "${Constant.CACTUS_PACKAGE}.${this}.$id"

/**
 * 获取id
 */
internal val id get() = if (Process.myUid() <= 0) Process.myPid() else Process.myUid()

/**
 * 解除DeathRecipient绑定
 *
 * @receiver IBinder.DeathRecipient
 * @param iInterface IInterface?
 * @param block Function0<Unit>?
 */
internal fun IBinder.DeathRecipient.unlinkToDeath(
    iInterface: IInterface? = null,
    block: (() -> Unit)? = null
) {
    iInterface?.asBinder()?.unlinkToDeath(this, 0)
    block?.invoke()
}

/**
 * 全局log
 *
 * @param msg String
 */
internal fun log(msg: String) {
    sCactusConfig?.defaultConfig?.apply {
        if (debug) {
            Log.d(Constant.CACTUS_TAG, msg)
        }
    } ?: Log.v(Constant.CACTUS_TAG, msg)
}