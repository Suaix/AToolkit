package com.atoolkit.aqrcode.widget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atoolkit.aqrcode.R
import com.atoolkit.aqrcode.databinding.AqrActivityScanBinding

class AScanActivity : AppCompatActivity() {

    private lateinit var mBinding: AqrActivityScanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = AqrActivityScanBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportFragmentManager.beginTransaction().replace(R.id.fl_root, AScanFragment()).commitAllowingStateLoss()
    }
}