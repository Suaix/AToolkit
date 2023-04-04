package com.atoolkit.autils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowInsets
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.NetworkInterface
import kotlin.math.max
import kotlin.math.min

/**
 * Description: 设备信息工具，用来提供各种与设备相关的信息，如imei\androidId\mac地址等
 * Author: summer
 * Date: 2023/3/27 19:24
 * LastModifyTime:
 */

private var imei: String? = null
private var androidId: String? = null
private var mac: String? = null
private var bssid: String? = null
private var ssid: String? = null
private var phoneModel: String? = null
private var phoneBrand: String? = null
private var phoneManufacturer: String? = null
private var phoneDevice: String? = null
private var screenWidth: Int? = null
private var screenHeight: Int? = null
private var statusBarHeight: Int? = null
private var navigationBarHeight: Int? = null

/**
 * Description: 获取设备imei号。
 * 在Android 10及以上系统不再允许获取imei，该方法会返回空字符串；
 * 在Android 10以下系统如果未获取[Manifest.permission.READ_PHONE_STATE]权限，也返回空字符串；
 * 其他情况会尝试获取imei，如果不成功则返回空字符串；
 * Author: summer
 */
@SuppressLint("MissingPermission")
fun getImei(): String {
    return imei ?: run {
        if (useCache) {
            // imei还未初始化，且要使用本地缓存，先从本地存储里获取缓存的imei值
            val temp = readValue(KEY_DEVICE_IMEI, INTERNAL_EMPTY_VALUE)
            if (temp != INTERNAL_EMPTY_VALUE) {
                // 这里不要判断temp是否为空字符串，因为在Android 10以上获取不到imei，在使用缓存的配置下会将空字符串缓存到本地；
                imei = temp
                imei as String
            }
        }
        imei = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                aLog?.v(TAG, "System sdk version is bigger than 10, can't get imei, just return empty")
                ""
            }
            application.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED -> {
                aLog?.v(TAG, "The permission of READ_PHONE_STATE is not granted, just return empty")
                ""
            }
            else -> {
                aLog?.v(TAG, "try to get imei from TelephonyManager")
                try {
                    val tm = application.getSystemService(Context.TELECOM_SERVICE)
                    if (tm is TelephonyManager) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            tm.imei ?: ""
                        } else {
                            tm.deviceId
                        }
                    } else {
                        ""
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
            }
        }
        if (useCache) {
            runBlocking {
                withContext(Dispatchers.IO) {
                    writeValue(KEY_DEVICE_IMEI, imei as String)
                }
            }
        }
        imei as String
    }
}

/**
 * Description: 获取AndroidId
 * Author: summer
 */
@SuppressLint("HardwareIds")
fun getAndroidId(): String {
    return androidId ?: run {
        if (useCache) {
            val tempAndroidId = readValue(KEY_DEVICE_ANDROID_ID, INTERNAL_EMPTY_VALUE)
            if (tempAndroidId != INTERNAL_EMPTY_VALUE) {
                androidId = tempAndroidId
                // 将AndroidId return
                androidId as String
            }
        }
        val tmpAndroidId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
        androidId = tmpAndroidId ?: ""
        if (useCache) {
            runBlocking {
                withContext(Dispatchers.IO) {
                    writeValue(KEY_DEVICE_ANDROID_ID, androidId as String)
                }
            }
        }
        androidId as String
    }
}

/**
 * Description: 获取mac地址。因为mac、wifi等信息会变化，因此不会进行本地缓存，至少会调用一次，获取到之后保存到内存缓存里
 * Author: summer
 */
fun getMac(): String {
    if (mac == null) {
        initMac()
    }
    // 二次检查
    if (mac == null) {
        mac = ""
    }
    return mac as String
}

/**
 * Description: 获取链接wifi的bssid标识
 * Author: summer
 */
fun getBSSID(): String {
    if (bssid == null) {
        initMac()
    }
    // 二次检查
    if (bssid == null) {
        bssid = ""
    }
    return bssid as String
}

/**
 * Description: 获取链接wifi的ssid
 * Author: summer
 */
fun getSSID(): String {
    if (ssid == null) {
        initMac()
    }
    if (ssid == null) {
        ssid = ""
    }
    return ssid as String
}

private fun initMac() {
    try {
        val wm = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (application.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                application.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            bssid = wm.connectionInfo.bssid
            ssid = wm.connectionInfo.ssid
        } else {
            bssid = ""
            ssid = ""
        }
        // 7.0及以上系统使用如下方法获取，7.0之前的暂不支持
        val nif = NetworkInterface.getNetworkInterfaces()
        nif?.let {
            while (it.hasMoreElements()) {
                val element = it.nextElement()
                if ("wlan0" == element.name) {
                    val address = element.hardwareAddress
                    if (address == null || address.isEmpty()) {
                        continue
                    }
                    val buf = StringBuffer()
                    for (b in address) {
                        buf.append(String.format("%02X", b))
                    }
                    if (buf.isNotEmpty()) {
                        buf.deleteCharAt(buf.length - 1)
                    }
                    mac = buf.toString()
                    break
                }
            }
            if (mac == null) {
                mac = ""
            }
        } ?: run {
            mac = ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        mac = ""
        bssid = ""
        ssid = ""
    }
}

/**
 * Description: 获取手机型号，即[Build.MODEL]
 * Author: summer
 */
fun getPhoneModel(): String {
    return phoneModel ?: run {
        val tempModel = if (useCache) readValue(KEY_DEVICE_MODEL, INTERNAL_EMPTY_VALUE) else INTERNAL_EMPTY_VALUE
        if (tempModel != INTERNAL_EMPTY_VALUE) {
            phoneModel = tempModel
        } else {
            phoneModel = Build.MODEL
            if (useCache) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        writeValue(KEY_DEVICE_MODEL, phoneModel)
                    }
                }
            }
        }
        phoneModel as String
    }
}

/**
 * Description: 获取手机品牌，即[Build.BRAND]
 * Author: summer
 */
fun getPhoneBrand(): String {
    return phoneBrand ?: run {
        val tempBrand = if (useCache) readValue(KEY_DEVICE_BRAND, INTERNAL_EMPTY_VALUE) else INTERNAL_EMPTY_VALUE
        if (tempBrand != INTERNAL_EMPTY_VALUE) {
            phoneBrand = tempBrand
        } else {
            phoneBrand = Build.BRAND
            if (useCache) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        writeValue(KEY_DEVICE_BRAND, phoneBrand)
                    }
                }
            }
        }
        phoneBrand as String
    }
}

/**
 * Description: 获取手机制造商，即[Build.MANUFACTURER]
 * Author: summer
 */
fun getPhoneManufacturer(): String {
    return phoneManufacturer ?: run {
        val tempManufacturer =
            if (useCache) readValue(KEY_DEVICE_MANUFACTURER, INTERNAL_EMPTY_VALUE) else INTERNAL_EMPTY_VALUE
        if (tempManufacturer != INTERNAL_EMPTY_VALUE) {
            phoneManufacturer = tempManufacturer
        } else {
            phoneManufacturer = Build.MANUFACTURER
            if (useCache) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        writeValue(KEY_DEVICE_MANUFACTURER, phoneManufacturer)
                    }
                }
            }
        }
        phoneManufacturer as String
    }
}

/**
 * Description: 获取设备外观设置名称，即[Build.DEVICE]
 * Author: summer
 */
fun getPhoneDevice(): String {
    return phoneDevice ?: run {
        val tempDevice = if (useCache) readValue(KEY_DEVICE_DEVICE, INTERNAL_EMPTY_VALUE) else INTERNAL_EMPTY_VALUE
        if (tempDevice != INTERNAL_EMPTY_VALUE) {
            phoneDevice = tempDevice
        } else {
            phoneDevice = Build.DEVICE
            if (useCache) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        writeValue(KEY_DEVICE_DEVICE, phoneDevice)
                    }
                }
            }
        }
        phoneDevice as String
    }
}

/**
 * Description: 获取屏幕宽度，单位：px
 * Author: summer
 */
fun getScreenWidth(): Int {
    return screenWidth ?: run {
        initScreenInfo()
        screenWidth as Int
    }
}

/**
 * Description: 获取屏幕高度，单位：px
 * Author: summer
 */
fun getScreenHeight(): Int {
    return screenHeight ?: run {
        initScreenInfo()
        screenHeight as Int
    }
}

/**
 * Description: 初始化屏幕信息
 * Author: summer
 */
private fun initScreenInfo() {
    val wm = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = wm.currentWindowMetrics.bounds
        val width = bounds.width()
        val height = bounds.height()
        screenWidth = min(width, height)
        screenHeight = max(width, height)
    } else {
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        screenWidth = min(metrics.widthPixels, metrics.heightPixels)
        screenHeight = max(metrics.widthPixels, metrics.heightPixels)
    }
}

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun getStatusBarHeight(): Int {
    return statusBarHeight ?: run {
        statusBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowInsets = wm.currentWindowMetrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
            val insetHeight = insets.bottom - insets.top
            aLog?.v(TAG, "status bar height by insetHeight=$insetHeight")
            insetHeight
        } else {
            val resourceId = application.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                application.resources.getDimensionPixelSize(resourceId)
            } else {
                50
            }
        }
        statusBarHeight as Int
    }
}

/**
 * Description: 获取导航栏的高度
 * Author: summer
 */
@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun getNavigationBarHeight(): Int {
    return navigationBarHeight ?: run {
        navigationBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowInsets = wm.currentWindowMetrics.windowInsets
            val insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            val insetHeight = insets.bottom - insets.top
            aLog?.v(TAG, "navigation bar height by insetHeight = $insetHeight")
            insetHeight
        } else {
            val resourceId = application.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                application.resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }
        }
        navigationBarHeight as Int
    }
}

/**
 * Description: dp转px，返回Float，使用Int的可以调用方转化下类型
 * Author: summer
 */
fun dp2Px(dpValue: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, application.resources.displayMetrics)
}

/**
 * Description: sp转px，返回Float，使用Int的可以调用方转化下类型
 * Author: summer
 */
fun sp2Px(spValue: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, application.resources.displayMetrics)
}

/**
 * Description: px转dp，返回Float，使用Int的可以调用方转化下类型
 * Author: summer
 */
fun px2Dp(pxValue: Float): Float {
    val density = application.resources.displayMetrics.density
    return pxValue / density + 0.5F
}
