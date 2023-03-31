package com.atoolkit.autils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import kotlinx.coroutines.runBlocking
import java.net.NetworkInterface

/**
 * Description: 设备信息工具，用来提供各种与设备相关的信息，如imei\androidId\mac地址等
 * Author: summer
 * Date: 2023/3/27 19:24
 * LastModifyTime:
 */

private var imei: String? = null
private var androidId: String? = null
private var mac: String? = null
private var wifiMacAddress: String? = null
private var wifiSSID: String? = null
private var phoneModel: String? = null
private var phoneBrand: String? = null
private var phoneManufacturer: String? = null
private var phoneDevice: String? = null

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
                writeValue(KEY_DEVICE_IMEI, imei as String)
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
                writeValue(KEY_DEVICE_ANDROID_ID, androidId as String)
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
    // TODO: mac等信息是否会变化，以此来判断是否需要使用本地缓存
    if (mac == null) {
        initMac()
    }
    // 二次检查
    if (mac == null) {
        mac = ""
    }
    return mac as String
}

fun getWifiMacAddress(): String {
    if (wifiMacAddress == null) {
        initMac()
    }
    // 二次检查
    if (wifiMacAddress == null) {
        wifiMacAddress = ""
    }
    return wifiMacAddress as String
}

fun getWifiSSID(): String {
    if (wifiSSID == null) {
        initMac()
    }
    if (wifiSSID == null) {
        wifiSSID = ""
    }
    return wifiSSID as String
}

private fun initMac() {
    val wm = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (application.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
            application.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    ) {
        wifiMacAddress = wm.connectionInfo.bssid
        wifiSSID = wm.connectionInfo.ssid
    } else {
        wifiMacAddress = ""
        wifiSSID = ""
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
}
