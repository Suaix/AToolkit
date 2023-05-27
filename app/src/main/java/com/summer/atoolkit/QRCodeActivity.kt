package com.summer.atoolkit

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.atoolkit.aqrcode.QR_RESULT_CONTENT
import com.atoolkit.aqrcode.createBarCode
import com.atoolkit.aqrcode.createQRCode
import com.atoolkit.aqrcode.page.AScanActivity
import com.atoolkit.autils.dp2Px
import com.atoolkit.autils.sp2Px
import com.summer.atoolkit.databinding.ActivityQrcodeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class QRCodeActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityQrcodeBinding
    private val activityLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intent = it.data
        if (it.resultCode == RESULT_OK && intent != null) {
            val qrContent = intent.getStringExtra(QR_RESULT_CONTENT)
            mBinding.btScanQr.text = qrContent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        testQrAndBarCode()
        mBinding.btScanQr.setOnClickListener {
            activityLaunch.launch(Intent(this@QRCodeActivity, AScanActivity::class.java))
        }
    }

    private fun testQrAndBarCode() {
        runBlocking {
            withContext(Dispatchers.IO) {
                val logoBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_logo)
                val qrBitmap = createQRCode(
                    "https://wwww.baidu.com",
                    dp2Px(200f).toInt(),
                    centerLogo = logoBitmap,
                    logoRatio = 0.3f,
                    safeMargin = 2
                )
                runOnUiThread {
                    mBinding.ivQrCode.setImageBitmap(qrBitmap)
                }
                val barcodeBitmap = createBarCode(
                    content = "190939329832848",
                    width = dp2Px(200f).toInt(),
                    height = dp2Px(60f).toInt(),
                    isShowText = true,
                    textSize = sp2Px(12f).toInt()
                )
                runOnUiThread {
                    mBinding.ivBarCode.setImageBitmap(barcodeBitmap)
                }
            }
        }
    }
}