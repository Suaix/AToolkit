package com.atoolkit.apermission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atoolkit.apermission.uistate.APermissionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Author:summer
 * Time: 2023/3/25 11:05
 * Description: APermissionViewModel是APermissionActivity的ViewModel，负责权限申请逻辑处理
 */
internal class APermissionViewModel : ViewModel() {
    private var grantedPermissions = mutableListOf<String>()
    private var deniedPermissions = mutableListOf<String>()
    private var waitRequestPermissions = mutableListOf<APermission>()
    private val _mPermissionUiState = MutableStateFlow<APermissionUiState>(APermissionUiState.InitUiState)
    private val repository = APermissionRepository(APermissionLocalDataSource())
    val uiState = _mPermissionUiState.asStateFlow()

    fun start() {
        originPermissions?.let {
            filterPermission(it)
        }
        doPermissionAction()
    }

    /**
     * Description: 处理权限申请后用户操作权限弹窗后的结果
     * Author: summer
     */
    fun dealPermissionResult(result: Map<String, Boolean>) {
        var hasDenied = false
        result.forEach { (key, value) ->
            if (value) {
                grantedPermissions.add(key)
                viewModelScope.launch {
                    repository.savePermissionRequestFlag(key, FLAG_PERMISSION_REQUESTED_GRANTED)
                }
            } else {
                deniedPermissions.add(key)
                viewModelScope.launch {
                    repository.savePermissionRequestFlag(key, FLAG_PERMISSION_REQUESTED_DENIED)
                }
                hasDenied = true
            }
        }
        if (hasDenied) {
            val state = _mPermissionUiState.value
            if (state is APermissionUiState.PermissionUiState && state.permissionData.isAbort) {
                // 有被拒绝的权限，且是终止后续操作，将剩余权限全部添加到拒绝列表里，并结束回调结果给调用者
                abortLastPermission()
                callbackAndFinishRequest()
                return
            }
        }
        // 检查是否还有未申请的权限，进行下一步操作
        doPermissionAction()
    }

    private fun doPermissionAction() {
        if (waitRequestPermissions.isNotEmpty()) {
            val ap = waitRequestPermissions.removeAt(0)
            ap.explanationData?.let { explain ->
                _mPermissionUiState.update {
                    APermissionUiState.ExplainUiState(
                        title = explain.explainTitle,
                        msg = explain.explainMsg,
                        leftText = explain.leftText,
                        rightText = explain.rightText,
                        callback = { isAgree ->
                            handleExplainOption(isAgree, ap.permissionData)
                        }
                    )
                }
            } ?: handlePermissionUiState(ap.permissionData)
        } else {
            callbackAndFinishRequest()
        }
    }

    /**
     * Description: 更新权限申请uistate，触发页面展示系统权限申请弹窗
     * Author: summer
     * @param permissionData 待申请的权限
     */
    private fun handlePermissionUiState(permissionData: PermissionData) {
        _mPermissionUiState.update {
            APermissionUiState.PermissionUiState(permissionData)
        }
    }

    /**
     * Description: 处理用户点击权限申请解释说明弹窗的操作
     * Author: summer
     * @param isAgree 是否同意申请，true：同意，false：不同意
     * @param permissionData 待申请的权限数据
     */
    private fun handleExplainOption(isAgree: Boolean, permissionData: PermissionData) {
        if (isAgree) {
            // 用户同意了申请，则触发系统权限申请弹窗
            handlePermissionUiState(permissionData)
        } else {
            // 不同意，将此权限添加到不同意中
            deniedPermissions.addAll(permissionData.permissions)
            // 判断剩余的权限是否继续
            if (permissionData.isAbort && waitRequestPermissions.isNotEmpty()) {
                // 将剩余权限全部添加进拒绝列表里，并回调结果给调用者
                abortLastPermission()
                callbackAndFinishRequest()
            } else {
                // 不中断，进行剩余权限的申请
                doPermissionAction()
            }
        }
    }

    /**
     * Description: 回调结果并结束申请
     * Author: summer
     */
    private fun callbackAndFinishRequest() {
        val callback = permissionCallback?.get()
        callback?.onPermissionResult(grantedPermissions, deniedPermissions)
        _mPermissionUiState.update {
            APermissionUiState.ResultUiState(grantedPermissions, deniedPermissions)
        }
    }

    /**
     * Description: 终止剩余权限的申请，全部将其放到拒绝权限中
     * Author: summer
     */
    private fun abortLastPermission() {
        waitRequestPermissions.forEach {
            deniedPermissions.addAll(it.getPermissions())
        }
    }

    /**
     * Description: 过滤权限列表
     * Author: summer
     */
    private fun filterPermission(permissions: List<APermission>) {
        permissions.forEach continuing@{ ap ->
            val pm = ap.getPermissions()
            pm.forEach { permission ->
                when {
                    // 判断被授予的权限
                    isPermissionGranted(permission) -> {
                        logger?.v(tag = TAG, msg = "$permission has granted")
                        grantedPermissions.add(permission)
                    }
                    // 判断被拒绝的权限
                    repository.getPermissionRequestFlag(
                        permission,
                        FLAG_PERMISSION_NEVER_REQUEST
                    ) == FLAG_PERMISSION_REQUESTED_DENIED -> {
                        logger?.v(tag = TAG, msg = "$permission has denied")
                        deniedPermissions.add(permission)
                    }
                    // 其他是待申请的权限
                    else -> {
                        logger?.v(tag = TAG, msg = "$permission wait request")
                        waitRequestPermissions.add(ap)
                        return@continuing
                    }
                }
            }
        }
    }
}