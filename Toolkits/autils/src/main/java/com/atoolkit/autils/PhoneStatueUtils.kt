package com.atoolkit.autils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.location.LocationManagerCompat

/**
 * Description: 判断手机底部导航栏是否展示
 * Author: summer
 * @return Boolean, true:展示，false：未展示
 */
@SuppressLint("PrivateApi", "DiscouragedApi")
fun isNavigationBarShow(): Boolean {
    var hasNavigationBar = true
    val resources = application.resources
    val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
    if (id > 0) {
        hasNavigationBar = resources.getBoolean(id)
    } else {
        try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java)
            val navBar = getMethod.invoke(systemProperties, "qemu.hw.mainkeys")
            hasNavigationBar = ("0" == navBar)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    if (!hasNavigationBar) {
        // 没有底部导航栏，直接返回false
        return false
    }
    // 有底部导航栏，根据品牌判断是否展示
    val deviceInfo = when (getPhoneBrand().lowercase()) {
        "huawei" -> "navigationbar_is_min"
        "xiaomi" -> "force_fsg_nav_bar"
        "vivo" -> "navigation_gesture_on"
        "oppo" -> "navigation_gesture_on"
        else -> "navigationbar_is_min"
    }
    if (Settings.Global.getInt(application.contentResolver, deviceInfo, -1) == 0) {
        return true
    }
    return false
}

/**
 * Description: app通知是否打开
 * Author: summer
 * @return Boolean, true：已打开，false：未打开
 */
fun isNotificationEnabled(): Boolean {
    return NotificationManagerCompat.from(application).areNotificationsEnabled()
}

/**
 * Description: 跳转到通知设置页面，API 26及以上系统会直接跳转到通知设置页；26以下系统会跳转到应用设置页；
 * Author: summer
 */
fun goNotificationSetting() {
    val intent = Intent()
    intent.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 26及以上系统，直接跳转到通知设置页面
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, application.packageName)
        } else {
            // 26以下系统，跳转到应用设置页
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:${application.packageName}")
        }
    }
    application.startActivity(intent)
}

/**
 * Description: 判断手机定位是否开启了，在 19<API<28 时会检查[LocationManager.GPS_PROVIDER]和[LocationManager.NETWORK_PROVIDER]
 * 是否可用，有一个可用即返回true。
 * Author: summer
 * @return Boolean， true：开启了，false：未开启
 */
fun isLocationEnabled(): Boolean {
    val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

/**
 * Description: 手机gps定位是否打开了
 * Author: summer
 * @return Boolean， true：打开了，false：未打开
 */
fun isGpsEnabled(): Boolean {
    val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

/**
 * Description: 跳转定位设置页
 * Author: summer
 */
fun goLocationSetting() {
    val intent = Intent()
    intent.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
    }
    application.startActivity(intent)
}

/**
 * Description: 当前wifi是否打开了
 * Author: summer
 * @return Boolean, true：打开了，false：未打开
 */
fun isWifiEnabled(): Boolean {
    val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return wifiManager.isWifiEnabled
}

/**
 * Description: 跳转wifi设置页面，需要在清单文件中声明 android.permission.ACCESS_WIFI_STATE 权限
 * Author: summer
 */
fun goWifiSetting() {
    val intent = Intent()
    intent.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        action = Settings.ACTION_WIFI_SETTINGS
    }
    application.startActivity(intent)
}

/**
 * Description: 手机蓝牙是否打开了
 * Author: summer
 * @return Boolean， true：已打开，false：未打开
 */
fun isBluetoothEnabled(): Boolean {
    val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return bluetoothManager.adapter?.isEnabled ?: false
}

/**
 * Description: 跳转蓝牙设置，需要在清单文件中声明 android.permission.BLUETOOTH 权限
 * Author: summer
 */
fun goBluetoothSetting() {
    val intent = Intent()
    intent.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        action = Settings.ACTION_BLUETOOTH_SETTINGS
    }
    application.startActivity(intent)
}

/**
 * Description: NFC是否可用，需要在清单文件中声明 android.permission.NFC 权限
 * Author: summer
 * @return Boolean, true：可用，false：不可用
 */
fun isNFCEnabled(): Boolean {
    val nfcManager = application.getSystemService(Context.NFC_SERVICE) as NfcManager
    return nfcManager.defaultAdapter?.isEnabled ?: false
}

/**
 * Description: 跳转NFC设置页
 * Author: summer
 */
fun goNFCSetting() {
    val intent = Intent()
    intent.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        action = Settings.ACTION_NFC_SETTINGS
    }
    application.startActivity(intent)
}