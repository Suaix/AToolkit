package com.summer.atoolkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atoolkit.alog.ALogUtil
import com.atoolkit.apermission.APermission
import com.atoolkit.apermission.IPermissionCallback
import com.atoolkit.apermission.handlePermissions
import com.summer.atoolkit.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.btSinglePermission.setOnClickListener {
            requestPermissions()
        }
    }

    override fun onStart() {
        super.onStart()
//        testLog()
    }

    private fun requestPermissions() {
        val permissions = buildPermissions()
        handlePermissions(this, permissions, object : IPermissionCallback {
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
        val readPhonePermission =
            APermission.Builder(arrayOf(android.Manifest.permission.READ_PHONE_STATE))
                    .isAbortWhenDeny(false)
                    .explainMsg("需要申请读取电话状态权限，已获取设备信息")
                    .explainLeftText("cancel")
                    .explainRightText("Ok")
                    .build()
        val locationPermission = APermission.Builder(
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
                .isAbortWhenDeny(false)
                .explainMsg("需要获取定位信息，请输入位置权限")
                .explainTitle("获取位置权限")
                .explainLeftText("取消")
                .explainRightText("确定")
                .build()

        val storagePermission = APermission.Builder(
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
                .isAbortWhenDeny(true)
                .explainMsg("需求获取存储权限，来进行数据存储和获取")
                .explainLeftText("取消")
                .explainRightText("确定")
                .build()
        val cameraPermission =
            APermission.Builder(arrayOf(android.Manifest.permission.CAMERA))
                    .isAbortWhenDeny(false)
                    .explainMsg("需要申请相机权限，用来进行拍照")
                    .explainLeftText("cancel")
                    .explainRightText("Ok")
                    .build()

        return listOf(readPhonePermission, locationPermission, storagePermission, cameraPermission)
    }

    @OptIn(DelicateCoroutinesApi::class)
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