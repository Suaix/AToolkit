package com.atoolkit.apermission


/**
 * Author:summer
 * Time: 2023/3/21 10:37
 * Description: IPermissionCallback是请求权限结果的回调
 */
interface IPermissionCallback {
    fun onPermissionResult(granted: List<String>, denied: List<String>)
}