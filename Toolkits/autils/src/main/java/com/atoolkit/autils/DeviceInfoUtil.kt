package com.atoolkit.autils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager

/**
 * Description: 设备信息工具，用来提供各种与设备相关的信息，如imei\androidId\mac地址等
 * Author: summer
 * Date: 2023/3/27 19:24
 * LastModifyTime:
 */

private var imei: String? = null
private var imsi: String? = null
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
        imei as String
    }
}