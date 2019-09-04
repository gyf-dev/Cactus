package com.gyf.cactus.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            Toast.makeText(this, "The app will crash after three seconds", Toast.LENGTH_SHORT)
                .show()
            Handler().postDelayed({
                2 / 0
            }, 3000)
        }
        App.mTimer.observe(this, Observer<String> {
            tvTimer.text = it
        })
        App.mLastTimer.observe(this, Observer<String> {
            tvLastTimer.text = it
        })
    }
}
