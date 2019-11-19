package com.gyf.cactus.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
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
        btnStop.setOnClickListener {
            cactusUnregister()
        }
        //重启
        btnRestart.setOnClickListener {
            cactusRestart()
        }
        //奔溃
        btnCrash.setOnClickListener {
            Toast.makeText(
                this,
                "The app will crash after three seconds(3s后退出)",
                Toast.LENGTH_SHORT
            ).show()
            Handler().postDelayed({
                2 / 0
            }, 3000)
        }
    }
}