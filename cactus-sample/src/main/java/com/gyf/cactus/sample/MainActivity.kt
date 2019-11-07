package com.gyf.cactus.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvVersion.text = "Version(版本)：${BuildConfig.VERSION_NAME}"
        btn.setOnClickListener {
            Toast.makeText(
                this,
                "The app will crash after three seconds(3s后退出)",
                Toast.LENGTH_SHORT
            )
                .show()
            Handler().postDelayed({
                2 / 0
            }, 3000)
        }
        App.mEndDate.observe(this, Observer {
            tvLastDate.text = it
        })
        App.mLastTimer.observe(this, Observer<String> {
            tvLastTimer.text = it
        })
        App.mTimer.observe(this, Observer<String> {
            tvTimer.text = it
        })
    }
}
