package com.gyf.cactus.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import com.gyf.cactus.Cactus
import com.gyf.cactus.ext.finishOnePix
import com.gyf.cactus.ext.isScreenOn
import com.gyf.cactus.ext.setOnePix

/**
 * @author geyifeng
 * @date 2019-08-29 13:29
 */
class OnePixActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Cactus.CACTUS_TAG, "one pix is created")
        overridePendingTransition(0, 0)
        //设定一像素的activity
        window.setGravity(Gravity.START or Gravity.TOP)
        window.attributes = window.attributes.apply {
            x = 0
            y = 0
            height = 1
            width = 1
        }
        setOnePix()
    }

    override fun onResume() {
        super.onResume()
        if (isScreenOn) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finishOnePix()
        Log.d(Cactus.CACTUS_TAG, "one pix is destroyed")
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}