package com.atoolkit.astorage

import android.content.Context
import com.atoolkit.common.ILog

internal lateinit var application: Context
internal lateinit var mKvStorage: IKVStorage
internal var isInited = false
internal var aLog: ILog? = null
internal const val TAG = "AStorage"
internal const val DEFAULT_KV_FILE_NAME = "AStorageKV"

/**
 * Description: 初始化存储器
 * Author: summer
 */
fun initAStorage(context: Context, config: AStorageConfig? = null) {
    application = context.applicationContext
    config?.let {
        initWithConfig(it)
    } ?: run {
        val defaultConfig = AStorageConfig(
            log = null,
            kvStorage = ADataStoreKVImpl(),
            extra = null
        )
        initWithConfig(defaultConfig)
    }
    isInited = true
}

/**
 * Description: 使用配置初始化kv存储实例
 * Author: summer
 */
private fun initWithConfig(config: AStorageConfig) {
    aLog = config.log
    config.kvStorage?.let {
        mKvStorage = it
    } ?: run {
        mKvStorage = ADataStoreKVImpl()
    }
    mKvStorage.init(application, config)
}

/**
 * Description: 获取KV存储的实例
 * Author: summer
 */
fun kvStorage(): IKVStorage {
    check(isInited) {
        "You must call initAStorage() method before use it"
    }
    return mKvStorage
}