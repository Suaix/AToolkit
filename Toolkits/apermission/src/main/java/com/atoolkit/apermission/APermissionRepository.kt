package com.atoolkit.apermission

/**
 * Author:summer
 * Time: 2023/3/25 20:10
 * Description: APermissionRepository是用来处理权限相关存储数据逻辑的类
 */
internal class APermissionRepository(private val dataSource: APermissionLocalDataSource) {
    /**
     * Description: 获取权限申请标识
     * Author: summer
     */
    fun getPermissionRequestFlag(permission: String, defaultValue: Int): Int =
        dataSource.readValue("$KEY_PREFIX$permission$REQUEST_FLAG_SUFFIX", defaultValue)

    suspend fun savePermissionRequestFlag(permission: String, flag: Int) =
        dataSource.writeValue("$KEY_PREFIX$permission$REQUEST_FLAG_SUFFIX", flag)
}