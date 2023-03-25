package com.atoolkit.apermission

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.atoolkit.apermission.uistate.APermissionUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class APermissionActivity : AppCompatActivity() {
    private val mViewModel: APermissionViewModel by viewModels()
    private var permissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            mViewModel.dealPermissionResult(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.uiState
                        .onEach {
                            when (it) {
                                APermissionUiState.InitUiState -> {
                                    mViewModel.start()
                                }
                                is APermissionUiState.ExplainUiState -> {
                                    showPermissionExplanationDialog(it)
                                }
                                is APermissionUiState.PermissionUiState -> {
                                    permissionLauncher.launch(it.permissionData.permissions)
                                }
                                is APermissionUiState.ResultUiState -> {
                                    // 设置result
                                    val data = Intent()
                                    data.putExtra(APERMISSION_DATA_GRANTED, it.grantedPermissions.toTypedArray())
                                    data.putExtra(APERMISSION_DATA_DENIED, it.deniedPermissions.toTypedArray())
                                    setResult(APERMISSION_RESULT_CODE, data)
                                    finish()
                                }
                            }
                        }
                        .collect()
            }
        }
    }

    /**
     * Description: 展示权限解释说明弹窗
     * Author: summer
     * @param explain 解释说明数据
     */
    private fun showPermissionExplanationDialog(explain: APermissionUiState.ExplainUiState) {
        val builder = AlertDialog.Builder(this)
                .setTitle(explain.title)
                .setMessage(explain.msg)
        explain.leftText?.let {
            // 左右按钮都有
            builder.setNegativeButton(explain.leftText) { _, _ ->
                explain.callback.invoke(false)
            }.setPositiveButton(explain.rightText) { _, _ ->
                explain.callback.invoke(true)
            }
        } ?: run {
            // 没有左侧文案，只设置一个按钮即可
            builder.setNeutralButton(explain.rightText) { _, _ ->
                explain.callback.invoke(true)
            }
        }
        builder.show()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}