package com.gyf.cactus.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.gyf.cactus.Cactus
import com.gyf.cactus.entity.NotificationConfig
import com.gyf.cactus.ext.sMainHandler
import com.gyf.cactus.ext.setNotification

/**
 * 隐藏前台服务
 * @author geyifeng
 * @date 2019-08-30 14:27
 */
class HideForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<NotificationConfig>(Cactus.CACTUS_NOTIFICATION_CONFIG)
            ?.let {
                setNotification(it, true)
            }
        sMainHandler.postDelayed({
            stopForeground(true)
            stopSelf()
        }, 2000)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}