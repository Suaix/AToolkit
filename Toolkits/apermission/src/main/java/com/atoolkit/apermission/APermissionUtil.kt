package com.atoolkit.apermission

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.intPreferencesKey
import com.atoolkit.common.ILog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference

internal const val TAG = "APermission"

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
 * Description: 检查该权限是不是用户已经永久拒绝了；Android 11及以上系统，申请过权限且用户拒绝了，则认为是永远拒绝了，
 * 官方解释是app安装生命周期内用具拒绝超过1次就对应是"拒绝后不再提醒"，此时再申请权限也不会弹出权限弹窗。官方解释如下：
 * At the same time, your app should respect the user's decision to deny a permission. Starting in Android 11 (API level 30),
 * if the user taps Deny for a specific permission more than once during your app's lifetime of installation on a device,
 * the user doesn't see the system permissions dialog if your app requests that permission again.
 * The user's action implies "don't ask again." On previous versions, users saw the system permissions dialog each time
 * your app requested a permission, unless they had previously selected a "don't ask again" checkbox or option.
 *
 * Author: summer
 *
 */
fun isPermissionAlwaysDenied(activity: Activity, permission: String): Boolean {
    val hasPermission = isPermissionGranted(permission)
    if (hasPermission) {
        // 如果已经被授权该权限，则返回false
        return false
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11及以上，申请过，且用户拒绝过该权限，即为不再提醒；
        runBlocking {
            application.permissionDataStore.data.map {
                (it[intPreferencesKey("$KEY_PREFIX$permission$REQUEST_FLAG_SUFFIX")]
                    ?: FLAG_PERMISSION_NEVER_REQUEST) == FLAG_PERMISSION_REQUESTED_DENIED
            }.first()
        }
    } else {
        // 被拒绝该权限 && 不需要需要说明理由（点击了拒绝后不再提醒）
        !activity.shouldShowRequestPermissionRationale(permission)
    }
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

/**
 * Description: 跳转到权限设置页，目前支持华为、小米和oppo，其他品牌使用通用设置
 * Author: summer
 */
fun goToPermissionSetting() {
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.putExtra("packageName", application.packageName)
    when (Build.MANUFACTURER.lowercase()) {
        "huawei" -> {
            // 华为
            val comp = ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity")
            intent.component = comp
        }
        "xiaomi" -> {
            val comp =
                ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
            intent.component = comp
        }
        "oppo" -> {
            val comp = ComponentName(
                "com.coloros.securitypermission",
                "com.coloros.securitypermission.permission.PermissionAppAllPermissionActivity"
            )
            intent.component = comp
        }
        else -> {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", application.packageName, null)
        }
    }
    application.startActivity(intent)
}