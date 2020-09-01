package com.gyf.cactus.sample

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.gyf.cactus.ext.cactusRestart
import com.gyf.cactus.ext.cactusUnregister
import com.gyf.cactus.ext.cactusUpdateNotification
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author geyifeng
 * @date 2019-08-28 17:22
 */
@Suppress("DIVISION_BY_ZERO")
@SuppressLint("SetTextI18n")
class MainActivity : BaseActivity() {

    private var times = 0L

    private val list = listOf(
        Pair("今日头条", "抖音全世界通用"),
        Pair("微博", "赵丽颖吐槽中餐厅"),
        Pair("绿洲", "今天又是美好的一天"),
        Pair("QQ", "好友申请"),
        Pair("微信", "在吗？"),
        Pair("百度地图", "新的路径规划"),
        Pair("墨迹天气", "明日大风，注意出行"),
        Pair("信息", "1条文本信息"),
        Pair("手机天猫", "你关注的宝贝降价啦")
    )

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
        App.mLastTimer.observe(this, Observer {
            tvLastTimer.text = it
        })
        App.mTimer.observe(this, Observer {
            tvTimer.text = it
        })
        App.mStatus.observe(this, Observer {
            it?.also {
                tvStatus.text = if (it) {
                    "Operating status(运行状态):Running(运行中)"
                } else {
                    "Operating status(运行状态):Stopped(已停止)"
                }
            }
        })
    }

    private fun setListener() {
        //更新通知栏信息
        btnUpdate.onClick {
            val num = (0..8).random()
            cactusUpdateNotification {
                setTitle(list[num].first)
                setContent(list[num].second)
            }
        }
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
            Toast.makeText(
                this,
                "The app will crash after three seconds(3s后奔溃)",
                Toast.LENGTH_SHORT
            ).show()
            Handler().postDelayed({
                2 / 0
            }, 3000)
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