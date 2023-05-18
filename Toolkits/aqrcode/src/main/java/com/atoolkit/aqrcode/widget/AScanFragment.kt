package com.atoolkit.aqrcode.widget

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.atoolkit.apermission.APermission
import com.atoolkit.apermission.IPermissionCallback
import com.atoolkit.apermission.handlePermissions
import com.atoolkit.apermission.isPermissionGranted
import com.atoolkit.aqrcode.QR_RESULT_CONTENT
import com.atoolkit.aqrcode.R
import com.atoolkit.aqrcode.application
import com.atoolkit.aqrcode.databinding.AqrFragmentScanBinding
import com.atoolkit.aqrcode.getBitmapFromUri
import com.atoolkit.aqrcode.parseCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * Author:summer
 * Time: 2023/4/27 15:14
 * Description: AScanFragment是扫码Fragment
 */

class AScanFragment private constructor() : Fragment() {

    private lateinit var mBinding: AqrFragmentScanBinding
    private var mViewModel: AScanViewModel? = null
    private var isScanning = false
    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.data
        uri?.let { picUri ->
            val bitmap = getBitmapFromUri(picUri, 1)
            bitmap?.let {
                runBlocking(Dispatchers.IO) {
                    val text = parseCode(it)
                    activity?.runOnUiThread {
                        Toast.makeText(context, "解析图片内容：$text", Toast.LENGTH_LONG).show()
                        dealResult(text)
                    }
                }
            }
        }
    }

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
                                    .explainTitle(getString(R.string.aqr_common_permission_des_title))
                                    .explainMsg(getString(R.string.aqr_camera_permission_des))
                                    .explainLeftText(getString(R.string.aqr_common_cancel))
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
                                dealResult(result.text)
                            }
                        }
                        else -> {

                        }
                    }
                }?.collect()
            }
        }
        mBinding.ivPickPhoto.setOnClickListener {
            if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                val aPermission = APermission.Builder(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                        .explainTitle(getString(R.string.aqr_common_permission_des_title))
                        .explainMsg(getString(R.string.aqr_read_storage_permission_des))
                        .explainLeftText(getString(R.string.aqr_common_cancel))
                        .build()
                activity?.let {
                    handlePermissions(it, listOf(aPermission), object : IPermissionCallback {
                        override fun onPermissionResult(granted: List<String>, denied: List<String>) {
                            if (granted.isNotEmpty()) {
                                choosePicture()
                            } else {
                                Toast.makeText(it, "需要获取读取外部存储权限", Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                }
                return@setOnClickListener
            }
            choosePicture()
        }
    }

    private fun choosePicture() {
        // 选择图片
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        activityLauncher.launch(pickPhotoIntent)
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

    private fun dealResult(result: String?) {
        if (result == null) {
            return
        }
        activity?.let {
            val intent = Intent()
            intent.putExtra(QR_RESULT_CONTENT, result)
            it.setResult(RESULT_OK, intent)
            it.finish()
        }
    }

}