package com.gyf.cactus.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.gyf.cactus.ext.cactusRestart
import com.gyf.cactus.ext.cactusUnregister
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author geyifeng
 * @date 2019-08-28 17:22
 */
@Suppress("DIVISION_BY_ZERO")
@SuppressLint("SetTextI18n")
class MainActivity : BaseActivity() {

    private var times = 0L

    companion object {
        private const val TIME = 4000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        setListener()
    }

    private fun initData() {
        tvVersion.text = "Version(版本)：${BuildConfig.VERSION_NAME}"
        App.mEndDate.observe(this, Observer {
            tvLastDate.text = it
        })
        App.mLastTimer.observe(this, Observer<String> {
            tvLastTimer.text = it
        })
        App.mTimer.observe(this, Observer<String> {
            tvTimer.text = it
        })
        App.mStatus.observe(this, Observer {
            tvStatus.text = if (it) {
                "Operating status(运行状态):Running(运行中)"
            } else {
                "Operating status(运行状态):Stopped(已停止)"
            }
        })
    }

    private fun setListener() {
        //停止
        btnStop.onClick {
            cactusUnregister()
        }
        //重启
        btnRestart.onClick {
            cactusRestart()
        }
        //奔溃
        btnCrash.setOnClickListener {
//            Toast.makeText(
//                this,
//                "The app will crash after three seconds(3s后退出)",
//                Toast.LENGTH_SHORT
//            ).show()
//            Handler().postDelayed({
//                2 / 0
//            }, 3000)
        }
    }

    private inline fun View.onClick(crossinline block: () -> Unit) {
        setOnClickListener {
            val nowTime = System.currentTimeMillis()
            val intervals = nowTime - times
            if (intervals > TIME) {
                times = nowTime
                block()
            } else {
                Toast.makeText(
                    context,
                    ((TIME.toFloat() - intervals) / 1000).toString() + "秒之后再点击",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}