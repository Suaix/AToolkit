package com.summer.atoolkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atoolkit.alog.ALogUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        testLog()
    }

    private fun testLog() {
        var flag = 0
        GlobalScope.launch {
            var count = 0
            repeat(100) {
                count++
                when (flag) {
                    1 -> ALogUtil.d(msg = "$count 测试D级别log，time=${System.currentTimeMillis()}")
                    2 -> ALogUtil.i(msg = "$count 测试I级别log，time=${System.currentTimeMillis()}")
                    3 -> ALogUtil.w(msg = "$count 测试W级别log，time=${System.currentTimeMillis()}")
                    4 -> ALogUtil.e(
                        tag = "Summer",
                        msg = "$count 测试E级别的log， time=${System.currentTimeMillis()}",
                        t = IllegalStateException("状态异常了")
                    )
                    else -> ALogUtil.v(msg = "测试v级别log，time=${System.currentTimeMillis()}")
                }
                flag++
                if (flag > 4) {
                    flag = 0
                }
                delay(200)
            }
        }
    }
}