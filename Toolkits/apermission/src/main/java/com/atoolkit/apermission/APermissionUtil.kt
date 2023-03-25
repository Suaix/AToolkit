package com.atoolkit.apermission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.atoolkit.common.ILog
import java.lang.ref.WeakReference

/**
 * 权限申请时的request code，当页面使用onActivityResult或
 */
const val APERMISSION_REQUEST_CODE = 1000
const val APERMISSION_RESULT_CODE = 1001
const val APERMISSION_DATA_GRANTED = "apermission_granted_list"
const val APERMISSION_DATA_DENIED = "apermission_denied_list"
internal const val TAG = "APermission"

// 是不是对权限逐个申请的标识，在intent中传递使用
internal const val IS_ONEBYONE_FLAG = "permission_is_onebyone"
internal lateinit var application: Context
internal var isInited = false
internal var logger: ILog? = null
internal var originPermissions: List<APermission>? = null
internal var permissionCallback: WeakReference<IPermissionCallback>? = null

/**
 * 初始化permission工具类
 */
fun initAPermission(context: Context, log: ILog? = null) {
    application = context.applicationContext
    logger = log
    isInited = true
}

/**
 * Description: 检查应用是否被授权该权限
 * Author: summer
 * @param permission 权限内容
 * @return true：已被授权该权限；false：未被授权该权限
 */
fun isPermissionGranted(permission: String): Boolean {
    check(isInited) {
        throw IllegalStateException("请先调用init(context, log)方法进行初始化")
    }
    return when (permission) {
        Manifest.permission.WRITE_SETTINGS -> {
            // 写入设置权限
            Settings.System.canWrite(application)
        }
        Manifest.permission.SYSTEM_ALERT_WINDOW -> {
            // 系统悬浮穿权限
            Settings.canDrawOverlays(application)
        }
        else -> {
            // 其他权限
            ContextCompat.checkSelfPermission(application, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

/**
 * Description: 检查该权限是不是用户已经永久拒绝了
 * Author: summer
 */
fun checkPermissionIsAlwaysDeny(activity: Activity, permission: String): Boolean {
    val hasPermission = isPermissionGranted(permission)
    if (hasPermission) {
        // 如果已经被授权该权限，则返回false
        return false
    }
    // 被拒绝该权限 && 需要说明理由（点击了拒绝后不再提醒）
    return activity.shouldShowRequestPermissionRationale(permission)
}

@JvmOverloads
fun handlePermissions(
    context: Activity,
    permissions: List<APermission>,
    callback: IPermissionCallback? = null
) {
    check(isInited) {
        throw IllegalStateException("请先调用init(context, log)方法进行初始化")
    }
    if (permissions.isEmpty()) {
        logger?.v(tag = TAG, msg = "权限列表为空，未申请任何权限！", null)
        return
    }
    callback?.let {
        permissionCallback = WeakReference(it)
    }
    originPermissions = permissions
    // 组装数据，调起权限申请页面
    val intent = Intent(context, APermissionActivity::class.java)
    context.startActivityForResult(intent, APERMISSION_REQUEST_CODE)
}