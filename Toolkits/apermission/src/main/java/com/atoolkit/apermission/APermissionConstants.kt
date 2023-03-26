package com.atoolkit.apermission

// 未申请过权限标识
internal const val FLAG_PERMISSION_NEVER_REQUEST = -1

// 申请过权限，且用户同意了
internal const val FLAG_PERMISSION_REQUESTED_GRANTED = 0

// 申请过权限，且用户拒绝了
internal const val FLAG_PERMISSION_REQUESTED_DENIED = 1

// DataStore kv存储的key前缀
internal const val KEY_PREFIX = "aPermission-"

// 存储权限标识的后缀
internal const val REQUEST_FLAG_SUFFIX = "-requestFlag"

// 存储权限申请时间的后缀
internal const val REQUEST_TIME_SUFFIX = "-time"

// 申请权限被拒绝次数的后缀
internal const val DENIED_COUNTER_SUFFIX = "-counter"

// 权限申请时的request code
const val APERMISSION_REQUEST_CODE = 1000

// 权限申请时的result code
const val APERMISSION_RESULT_CODE = 1001

// 默认的权限申请间隔，如果一个权限申请被用户拒绝后，在一定时间间隔内不应该再请求该权限
const val APERMISSION_DEFAULT_NOT_REQUEST_DURATION = 48 * 60 * 60 * 1000L
const val APERMISSION_DATA_GRANTED = "apermission_granted_list"
const val APERMISSION_DATA_DENIED = "apermission_denied_list"