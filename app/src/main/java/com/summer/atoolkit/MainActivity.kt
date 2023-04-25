package com.summer.atoolkit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import com.atoolkit.alog.ALogUtil
import com.atoolkit.apermission.APERMISSION_DATA_DENIED
import com.atoolkit.apermission.APERMISSION_DATA_GRANTED
import com.atoolkit.apermission.APERMISSION_RESULT_CODE
import com.atoolkit.apermission.APermission
import com.atoolkit.apermission.IPermissionCallback
import com.atoolkit.apermission.goToPermissionSetting
import com.atoolkit.apermission.handlePermissions
import com.atoolkit.autils.createFile
import com.atoolkit.autils.deleteFileByPath
import com.atoolkit.autils.getInternalCacheDir
import com.atoolkit.autils.getInternalFileDir
import com.atoolkit.autils.isExternalStorageReadable
import com.atoolkit.autils.isExternalStorageWritable
import com.atoolkit.autils.renameFile
import com.atoolkit.autils.unzipFile
import com.atoolkit.autils.zipFile
import com.atoolkit.autils.dp2Px
import com.atoolkit.autils.getAndroidId
import com.atoolkit.autils.getBSSID
import com.atoolkit.autils.getImei
import com.atoolkit.autils.getMac
import com.atoolkit.autils.getNavigationBarHeight
import com.atoolkit.autils.getPhoneBrand
import com.atoolkit.autils.getPhoneDevice
import com.atoolkit.autils.getPhoneManufacturer
import com.atoolkit.autils.getPhoneModel
import com.atoolkit.autils.getSSID
import com.atoolkit.autils.getScreenHeight
import com.atoolkit.autils.getScreenWidth
import com.atoolkit.autils.getStatusBarHeight
import com.atoolkit.autils.goBluetoothSetting
import com.atoolkit.autils.goLocationSetting
import com.atoolkit.autils.goNFCSetting
import com.atoolkit.autils.goNotificationSetting
import com.atoolkit.autils.goWifiSetting
import com.atoolkit.autils.isBluetoothEnabled
import com.atoolkit.autils.isLocationEnabled
import com.atoolkit.autils.isNFCEnabled
import com.atoolkit.autils.isNavigationBarShow
import com.atoolkit.autils.isNotificationEnabled
import com.atoolkit.autils.isWifiEnabled
import com.atoolkit.autils.px2Dp
import com.atoolkit.autils.sp2Px
import com.summer.atoolkit.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.btSinglePermission.setOnClickListener {
            requestPermissions()
        }
        mBinding.goToPermissionSetting.setOnClickListener {
            goToPermissionSetting()
        }
        mBinding.btFileUtils.setOnClickListener {
            testFileUtils()
        }
        mBinding.btQrCode.setOnClickListener {
            startActivity(Intent(this@MainActivity, QRCodeActivity::class.java))
        }
        testDeviceInfo()
    }

    private fun testFileUtils() {
        val sb = StringBuilder()
        // 创建File
        for (i in 0..3) {
            val testFile = createFile(this.filesDir.absolutePath + File.separator + "AStorage", "test$i.txt")
            sb.append("testFile:${testFile?.absolutePath}\n")
            runBlocking {
                withContext(Dispatchers.IO) {
                    val buffer = "This is test content $i".toByteArray()
                    val outputStream = BufferedOutputStream(FileOutputStream(testFile))
                    outputStream.write(buffer)
                    outputStream.close()
                }
            }
        }
        val rootPath = this.filesDir.absolutePath + File.separator + "AStorage"
        val renameSuccess =
            renameFile(rootPath + File.separator + "test3.txt", rootPath + File.separator + "rename_test3.txt")
        sb.append("renameSuccess: $renameSuccess\n")
        val tempDirPath = rootPath + File.separator + "android"
        for (index in 0..3) {
            val androidFile = if (index < 3) {
                createFile(tempDirPath, "android$index.java")
            } else {
                createFile(tempDirPath + File.separator+"kotlin", "android$index.kotlin")
            }
            sb.append("androidFile:${androidFile?.absolutePath}\n")
            runBlocking {
                withContext(Dispatchers.IO) {
                    val buffer = "This is test android content $index".toByteArray()
                    val outputStream = BufferedOutputStream(FileOutputStream(androidFile))
                    outputStream.write(buffer)
                    outputStream.close()
                }
            }
        }
        val deleteSuccess = deleteFileByPath(tempDirPath + File.separator + "android1.java")
        sb.append("deleteSuccess:$deleteSuccess\n")
        mBinding.tvDeviceInfo.text = sb.toString()
        var zipFile: File? = null
        runBlocking {
            zipFile(rootPath) { isSuccess, msg, file ->
                zipFile = file
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "zip file --> isSuccess=$isSuccess, msg=$msg, file=${file?.absolutePath}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
            delay(5*1000)
            zipFile?.let {
                unzipFile(it, isUnzipWithParentDir =  true, isDeleteZipFileAfterUnzipSuccess = true){ isUnzipSuccess, msg ->
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "unzip file --> isUnzipSuccess=$isUnzipSuccess, msg=$msg",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun testDeviceInfo() {
        val sb = StringBuilder()
        sb.append("imei = ${getImei()}\n")
                .append("androidId = ${getAndroidId()}\n")
                .append("mac = ${getMac()}\n")
                .append("bssid = ${getBSSID()}\n")
                .append("ssid = ${getSSID()}\n")
                .append("phoneModel = ${getPhoneModel()}\n")
                .append("phoneBrand = ${getPhoneBrand()}\n")
                .append("phoneManufacturer = ${getPhoneManufacturer()}\n")
                .append("phoneDevice = ${getPhoneDevice()}\n")
                .append("screenWidth = ${getScreenWidth()}\n")
                .append("screenHeight = ${getScreenHeight()}\n")
                .append("statusBarHeight = ${getStatusBarHeight()}\n")
                .append("navigationBarHeight = ${getNavigationBarHeight()}\n")
                .append("100dp = ${dp2Px(100F)}px\n")
                .append("12sp = ${sp2Px(12F)}px\n")
                .append("100px = ${px2Dp(100F)}dp\n")
                .append("internal file path: ${getInternalFileDir().absolutePath}\n")
                .append("internal cache path: ${getInternalCacheDir().absolutePath}\n")
                .append("is external storage writable=${isExternalStorageWritable()}\n")
                .append("is external storage readable=${isExternalStorageReadable()}\n")
                .append("external file path: ${com.atoolkit.autils.getExternalFilesDir()?.absolutePath}\n")
                .append("external cache path:${com.atoolkit.autils.getExternalCacheDir()?.absolutePath}\n")
        mBinding.tvDeviceInfo.text = sb.toString()
    }

    override fun onResume() {
        super.onResume()
//        testPhoneStatus()
    }

    private val phoneStatusList = mutableListOf<PhoneStatusInfo>()
    private fun testPhoneStatus() {
        phoneStatusList.clear()
        mBinding.llContainer.removeAllViews()
        val temp = listOf(
            PhoneStatusInfo("底部导航栏是否展示：${isNavigationBarShow()}") {},
            PhoneStatusInfo("App通知是否可用：${isNotificationEnabled()}") {
                goNotificationSetting()
            },
            PhoneStatusInfo("定位是否可用：${isLocationEnabled()}") {
                goLocationSetting()
            },
            PhoneStatusInfo("WIFI是否开启：${isWifiEnabled()}") {
                goWifiSetting()
            },
            PhoneStatusInfo("蓝牙是否开启：${isBluetoothEnabled()}") {
                goBluetoothSetting()
            },
            PhoneStatusInfo("NFC是否可用：${isNFCEnabled()}") {
                goNFCSetting()
            }
        )
        phoneStatusList.addAll(temp)
        phoneStatusList.forEach { phoneStatus ->
            val button = Button(this)
            button.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            button.text = phoneStatus.value
            button.setOnClickListener {
                phoneStatus.function.invoke()
            }
            mBinding.llContainer.addView(button)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == APERMISSION_RESULT_CODE) {
            data?.let {
                val grantedList = it.getStringArrayExtra(APERMISSION_DATA_GRANTED)
                val deniedList = it.getStringArrayExtra(APERMISSION_DATA_DENIED)
                ALogUtil.v(msg = "onActivityResult granted permissions:")
                if (grantedList != null) {
                    for (permission in grantedList) {
                        ALogUtil.v(msg = permission)
                    }
                }

                ALogUtil.w(msg = "onActivityResult denied permissions:")
                if (deniedList != null) {
                    for (permission in deniedList) {
                        ALogUtil.w(msg = permission)
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = buildPermissions()
        handlePermissions(this, permissions, object : IPermissionCallback {
            override fun onPermissionResult(granted: List<String>, denied: List<String>) {
                ALogUtil.v(msg = "callback granted permissions:")
                for (permission in granted) {
                    ALogUtil.v(msg = permission)
                }

                ALogUtil.w(msg = "callback denied permissions:")
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