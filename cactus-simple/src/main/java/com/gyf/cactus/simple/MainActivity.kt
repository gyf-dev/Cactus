package com.gyf.cactus.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            Toast.makeText(this, "The app will crash in three seconds", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                2 / 0
            }, 3000)
        }
    }
}
