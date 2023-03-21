package com.atoolkit.apermission

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

internal class APermissionActivity : AppCompatActivity() {
    private var isOneByOne = false
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var currentPermission: APermission? = null
    private var grantedPermissions = mutableListOf<String>()
    private var deniedPermissions = mutableListOf<String>()
    private var waitRequestPermissions = mutableListOf<APermission>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        isOneByOne = intent.getBooleanExtra(IS_ONEBYONE_FLAG, false)
        setContentView(R.layout.activity_apermission)
        createPermissionLauncher()
        originPermissions?.let {
            filterPermissions(it)
        }
    }

    override fun onStart() {
        super.onStart()
        if (waitRequestPermissions.isNotEmpty()) {
            launchPermission(waitRequestPermissions)
        } else {
            dealResult()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    /**
     * 创建权限申请器
     */
    private fun createPermissionLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            when {
                isOneByOne -> {
                    // 单权限申请，判断当前权限是否被拒
                    result.forEach { (key, value) ->
                        if (value) {
                            // 授予了权限
                            grantedPermissions.add(key)
                            if (hasNexPermission()) {
                                val ap = waitRequestPermissions.removeAt(0)
                                launchNextPermission(ap)
                            } else {
                                dealResult()
                            }
                        } else {
                            // 拒绝了权限
                            deniedPermissions.add(key)
                            if (hasNexPermission()) {
                                if (currentPermission?.permissionData?.isAbort == true) {
                                    // 中断后续权限的申请，将待申请的权限放到拒绝的集合里
                                    for (ap in waitRequestPermissions) {
                                        deniedPermissions.add(ap.getPermission())
                                    }
                                    dealResult()
                                } else {
                                    val ap = waitRequestPermissions.removeAt(0)
                                    launchNextPermission(ap)
                                }
                            } else {
                                // 后面没有待申请的权限了
                                dealResult()
                            }
                        }
                    }
                }
                else -> {
                    // 全量权限申请
                    result.forEach { (key, value) ->
                        if (value) {
                            grantedPermissions.add(key)
                        } else {
                            deniedPermissions.add(key)
                        }
                        dealResult()
                    }
                }
            }
        }
    }

    private fun hasNexPermission(): Boolean {
        // 先判断当前待申请列表里是否还有权限未申请，有的话直接返回false
        if (waitRequestPermissions.isEmpty()) {
            return false
        }
        // 如果待申请的权限不为空，则再次筛选一下，将权限组里已经有的权限筛选出来
        val temp = waitRequestPermissions
        // 创建一个新的列表
        waitRequestPermissions = mutableListOf()
        filterPermissions(temp)
        // 筛选后如果待申请权限列表还是不为空，则有下一条
        return waitRequestPermissions.isNotEmpty()
    }

    /**
     * Description: 过滤权限，筛选出已授权、未授权和拒绝不再提醒的权限
     * Author: summer
     */
    private fun filterPermissions(aPermissionList: List<APermission>) {
        aPermissionList.forEach { ap ->
            val pm = ap.getPermission()
            if (checkPermission(pm)) {
                logger?.v(tag = TAG, msg = "$pm has granted")
                grantedPermissions.add(pm)
            } else if (shouldShowRequestPermissionRationale(pm)) {
                logger?.v(tag = TAG, msg = "$pm is always denied")
                deniedPermissions.add(pm)
            } else {
                logger?.v(tag = TAG, msg = "$pm wait request")
                waitRequestPermissions.add(ap)
            }
        }
    }

    /**
     * Description: 根据模式调起权限申请
     * Author: summer
     */
    private fun launchPermission(permissionList: MutableList<APermission>) {
        if (isOneByOne) {
            val aPermission = permissionList.removeAt(0)
            launchNextPermission(aPermission)
        } else {
            permissionList[0].explanationData?.let {
                // 弹窗说明申请权限的用途
                showPermissionExplanationDialog(it) { isAgree ->
                    if (isAgree) {
                        realLaunchMultiPermission(permissionList)
                    } else {
                        // 不同意申请权限，直接返回结果
                        dealResult()
                    }
                }
            } ?: realLaunchMultiPermission(permissionList)
        }
    }

    /**
     * Description: 单权限申请模式下申请单个权限
     * Author: summer
     * @param p 单个权限的数据
     */
    private fun launchNextPermission(p: APermission) {
        currentPermission = p
        p.explanationData?.let {
            showPermissionExplanationDialog(it) { isAgree ->
                if (isAgree) {
                    // 同意申请权限，调起权限申请
                    permissionLauncher?.launch(arrayOf(p.getPermission()))
                } else {
                    // 不同意权限申请
                    deniedPermissions.add(p.getPermission())
                    if (p.permissionData.isAbort || waitRequestPermissions.isEmpty()) {
                        // 设置了终止后续权限申请，不再申请后续权限，直接返回结果
                        dealResult()
                    } else {
                        // 不终止后续权限的申请，进行下一个权限申请
                        val nextPermission = waitRequestPermissions.removeAt(0)
                        launchNextPermission(nextPermission)
                    }
                }
            }
        } ?: permissionLauncher?.launch(arrayOf(p.getPermission()))
    }

    /**
     * Description: 实际调起多权限申请
     * Author: summer
     * @param permissionList 多个权限的列表
     */
    private fun realLaunchMultiPermission(permissionList: List<APermission>) {
        val permissionArray = transPermissionArray(permissionList)
        permissionLauncher?.launch(permissionArray)
    }

    /**
     * Description: 展示权限解释说明弹窗
     * Author: summer
     * @param explain 解释说明数据
     * @param clickCallback 弹窗点击回调，true为同意申请，false为不同意申请
     */
    private fun showPermissionExplanationDialog(explain: ExplanationData, clickCallback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(this)
                .setTitle(explain.explainTitle)
                .setMessage(explain.explainMsg)
        explain.leftText?.let {
            // 没有左侧文案，只设置一个按钮即可
            builder.setNeutralButton(explain.rightText) { _, _ ->
                clickCallback.invoke(true)
            }
        } ?: run {
            // 左右按钮都有
            builder.setNegativeButton(explain.leftText) { _, _ ->
                clickCallback.invoke(false)
            }.setPositiveButton(explain.rightText) { _, _ ->
                clickCallback.invoke(true)
            }
        }
        builder.show()
    }

    /**
     * Description: 将封装的权限数据转化为权限数组
     * Author: summer
     */
    private fun transPermissionArray(permissionList: List<APermission>): Array<String> {
        val size = permissionList.size
        val result = Array(size) { "" }
        for (i in 0 until size) {
            result[i] = permissionList[i].permissionData.permission
        }
        return result
    }

    /**
     * Description: 处理结果，如果设置了回调会优先通过调用回调，之后都会设置result返回处理结果
     * Author: summer
     */
    private fun dealResult() {
        // 检查是否有回调
        val callback = permissionCallback?.get()
        callback?.apply {
            onPermissionResult(grantedPermissions, deniedPermissions)
        }
        permissionCallback = null
        // 设置result
        val data = Intent()
        data.putExtra(APERMISSION_DATA_GRANTED, grantedPermissions.toTypedArray())
        data.putExtra(APERMISSION_DATA_DENIED, deniedPermissions.toTypedArray())
        setResult(APERMISSION_RESULT_CODE, data)
        finish()
    }
}