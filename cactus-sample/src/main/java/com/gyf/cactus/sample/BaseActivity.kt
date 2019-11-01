package com.gyf.cactus.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * @author geyifeng
 * @date 2019-11-01 17:34
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppManager.INSTANCE.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.INSTANCE.removeActivity(this)
    }
}