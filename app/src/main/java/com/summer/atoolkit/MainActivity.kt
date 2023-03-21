package com.summer.atoolkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atoolkit.alog.ALogUtil
import com.atoolkit.apermission.APermission
import com.atoolkit.apermission.IPermissionCallback
import com.atoolkit.apermission.requestPermissionList
import com.summer.atoolkit.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.btMultiPermission.setOnClickListener {
            requestPermissions(false)
        }
        mBinding.btSinglePermission.setOnClickListener {
            requestPermissions(true)
        }
    }

    override fun onStart() {
        super.onStart()
//        testLog()
    }

    private fun requestPermissions(isOneByOne: Boolean) {
        val permissions = buildPermissions()
        requestPermissionList(this, permissions, isOneByOne, object : IPermissionCallback {
            override fun onPermissionResult(granted: List<String>, denied: List<String>) {
                ALogUtil.v(msg = "granted permissions:")
                for (permission in granted) {
                    ALogUtil.v(msg = permission)
                }

                ALogUtil.w(msg = "denied permissions:")
                for (permission in denied) {
                    ALogUtil.w(msg = permission)
                }
            }
        })
    }

    private fun buildPermissions(): List<APermission> {
        val permissions = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        )
        val result = mutableListOf<APermission>()
        for (permission in permissions) {
            val ap = APermission.Builder(permission).explanMsg("需要申请：$permission 权限").build()
            result.add(ap)
        }
        return result
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