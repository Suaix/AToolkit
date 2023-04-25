package com.summer.atoolkit

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atoolkit.aqrcode.createBarCode
import com.atoolkit.aqrcode.createQRCode
import com.atoolkit.autils.dp2Px
import com.atoolkit.autils.sp2Px
import com.summer.atoolkit.databinding.ActivityQrcodeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class QRCodeActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityQrcodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        testQrAndBarCode()
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