package com.gyf.cactus.ext

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.gyf.cactus.Cactus
import com.gyf.cactus.callback.AppBackgroundCallback
import com.gyf.cactus.entity.CactusConfig
import com.gyf.cactus.pix.OnePixActivity
import com.gyf.cactus.service.CactusJobService
import com.gyf.cactus.service.LocalService
import com.gyf.cactus.service.RemoteService
import com.gyf.cactus.workmanager.CactusWorker
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit


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
 * 用来表示是前台还是后台
 */
private var mIsForeground = false
/**
 * 主Handler
 */
internal val mMainHandler by lazy {
    Handler(Looper.getMainLooper())
}

/**
 * kotlin里使用Cactus
 * @receiver Context
 * @param block [@kotlin.ExtensionFunctionType] Function1<Cactus, Unit>
 */
fun Context.cactus(block: Cactus.() -> Unit) =
    Cactus.instance.apply { block() }.register(this)

/**
 * 注册Cactus服务
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.register(cactusConfig: CactusConfig) {
    if (isMain) {
        try {
            saveConfig(cactusConfig)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                registerJobCactus(cactusConfig)
            } else {
                registerCactus(cactusConfig)
            }
            if (this is Application) {
                registerActivityLifecycleCallbacks(AppBackgroundCallback(this))
            }
        } catch (e: Exception) {
            Log.d(Cactus.CACTUS_TAG, "Unable to open cactus service!!")
        }
    }
}

/**
 * 最终都将调用此方法，注册Cactus服务
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.registerCactus(cactusConfig: CactusConfig) {
    val intent = Intent(this, LocalService::class.java)
    intent.putExtra(Cactus.CACTUS_CONFIG, cactusConfig)
    startInternService(intent)
    mMainHandler.postDelayed({ registerWorker() }, 5000)
}

/**
 * 注册JobService
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
internal fun Context.registerJobCactus(cactusConfig: CactusConfig) {
    val intent = Intent(this, CactusJobService::class.java)
    intent.putExtra(Cactus.CACTUS_CONFIG, cactusConfig)
    startInternService(intent)
}

/**
 * 开启WorkManager
 * @receiver Context
 */
internal fun Context.registerWorker() {
    val constraintsBuilder = Constraints.Builder()
    constraintsBuilder.setRequiredNetworkType(NetworkType.NOT_REQUIRED)
    val workRequest = PeriodicWorkRequest.Builder(CactusWorker::class.java, 15, TimeUnit.SECONDS)
        .setConstraints(constraintsBuilder.build())
        .addTag(Cactus.CACTUS_TAG)
        .build()
    WorkManager.getInstance(this).enqueue(workRequest)
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
    startInternService(intent)
    bindService(intent, serviceConnection, Context.BIND_IMPORTANT)
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
        startInternService(intent)
    }
    bindService(intent, serviceConnection, Context.BIND_IMPORTANT)
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
 * 开启一像素界面
 * @receiver Context
 */
internal fun Context.startOnePixActivity() {
    if (!isScreenOn && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        mIsForeground = isForeground
        Log.d(Cactus.CACTUS_TAG, "isForeground:$mIsForeground")
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
 * 保存配置信息
 *
 * @receiver Context
 * @param cactusConfig CactusConfig
 */
private fun Context.saveConfig(cactusConfig: CactusConfig) {
    val serviceId = getServiceId()
    if (serviceId > 0) {
        cactusConfig.notificationConfig.serviceId = serviceId
    }
    getSharedPreferences(Cactus.CACTUS_TAG, Context.MODE_PRIVATE).edit().apply {
        putString(Cactus.CACTUS_CONFIG, Gson().toJson(cactusConfig))
        if (serviceId <= 0) {
            putInt(Cactus.CACTUS_SERVICE_ID, cactusConfig.notificationConfig.serviceId)
        }
    }.apply()
}

/**
 * 获取配置信息
 *
 * @receiver Context
 * @return CactusConfig
 */
internal fun Context.getConfig() = getSharedPreferences(
    Cactus.CACTUS_TAG,
    Context.MODE_PRIVATE
).getString(Cactus.CACTUS_CONFIG, null)?.run {
    Gson().fromJson(this, CactusConfig::class.java)
} ?: CactusConfig()

/**
 * 获得serviceId
 *
 * @receiver Context
 * @return Int
 */
private fun Context.getServiceId() = getSharedPreferences(
    Cactus.CACTUS_TAG,
    Context.MODE_PRIVATE
).getInt(Cactus.CACTUS_SERVICE_ID, -1)

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
 * @receiver OnePixActivity
 */
internal fun OnePixActivity.setOnePix() {
    if (mWeakReference == null) {
        mWeakReference = WeakReference(this)
    }
}

/**
 * 退到后台
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
 * 是否在前台
 */
internal val Context.isForeground
    @SuppressLint("NewApi")
    get() = run {
        var foreground = false
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        tasks?.apply {
            if (isNotEmpty()) {
                this[0].topActivity?.let {
                    foreground = (it.packageName == packageName)
                }
            }
        }
        foreground
    }

/**
 * 屏幕是否亮屏
 */
internal val Context.isScreenOn
    get() = run {
        try {
            val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isScreenOn
        } catch (e: Exception) {
            false
        }
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
            if (info.processName.contains(processName)) {
                return true
            }
        }
    }
    return false
}