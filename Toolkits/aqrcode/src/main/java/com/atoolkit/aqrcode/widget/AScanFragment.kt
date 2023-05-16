package com.atoolkit.aqrcode.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.atoolkit.apermission.APermission
import com.atoolkit.apermission.IPermissionCallback
import com.atoolkit.apermission.handlePermissions
import com.atoolkit.aqrcode.R
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.databinding.AqrFragmentScanBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


/**
 * Author:summer
 * Time: 2023/4/27 15:14
 * Description: AScanFragment是扫码Fragment
 */

class AScanFragment private constructor() : Fragment() {

    private lateinit var mBinding: AqrFragmentScanBinding
    private var mViewModel: AScanViewModel? = null
    private var isScanning = false

    companion object {
        fun newInstance(bundle: Bundle? = null): AScanFragment {
            val fBundle = bundle ?: Bundle()
            val fragment = AScanFragment()
            fragment.arguments = fBundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = AqrFragmentScanBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
//        mBinding.svScanView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                mBinding.svScanView.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                val scanArea = mBinding.svScanView.getScanArea()
//                aLog?.i(TAG, "scanArea=$scanArea")
//                startScan()
//            }
//        })
    }

    override fun onStart() {
        super.onStart()
        mViewModel = ViewModelProvider(requireActivity())[AScanViewModel::class.java]
        mViewModel?.init(this, mBinding.pvCameraPreview)
    }

    override fun onResume() {
        super.onResume()
        startScan()
    }

    private fun initListener() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel?.uiState?.onEach { uiState ->
                    when (uiState) {
                        is AScanUiState.PermissionUiState -> {
                            // 请求权限
                            val aPermission = APermission.Builder(arrayOf(uiState.permission))
                                    .explainTitle(getString(R.string.aqr_camera_permission_des_title))
                                    .explainMsg(getString(R.string.aqr_camera_permission_des))
                                    .explainLeftText(getString(R.string.aqr_camera_permission_cancel))
                                    .build()
                            activity?.let {
                                handlePermissions(it, listOf(aPermission), object : IPermissionCallback {
                                    override fun onPermissionResult(granted: List<String>, denied: List<String>) {
                                        if (granted.isNotEmpty()) {
                                            // 已授权，再次开启相机
                                            startScan()
                                        } else {
                                            // 未授权，提示
                                            Toast.makeText(it, "需要获取相机权限", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })
                            }
                        }
                        is AScanUiState.ResultUiState -> {
                            val result = uiState.result
                            activity?.let {
                                Toast.makeText(it, "扫码结果是：${result.text}", Toast.LENGTH_LONG).show()
                            }
                        }
                        else -> {

                        }
                    }
                }?.collect()
            }
        }
    }

    /**
     * Description: 开始扫描
     * Author: summer
     */
    private fun startScan() {
        if (isScanning) {
            return
        }
        val scanMode = mBinding.svScanView.getScanMode()
        if (scanMode == AScanModel.FULLSCREEN) {
            mViewModel?.startScan(true)
        } else {
            val scanArea = mBinding.svScanView.getScanArea()
            if (scanArea.width() > 0 && scanArea.height() > 0) {
                mViewModel?.startScan(false, scanArea)
            } else {
                mViewModel?.startScan(false)
            }
        }
        isScanning = true
    }

}