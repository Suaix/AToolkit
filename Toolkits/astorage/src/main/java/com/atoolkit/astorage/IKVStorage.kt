package com.atoolkit.astorage

import android.content.Context


/**
 * Author:summer
 * Time: 2023/4/18 20:19
 * Description: IKVStorage是进行KV存储的接口定义
 */
interface IKVStorage {
    /**
     * Description: 初始化kv-storage
     * Author: summer
     *
     * @param context ApplicationContext
     * @param config 存储配置项
     */
    fun init(context: Context, config: AStorageConfig)

    /**
     * Description: 存储k-v值
     * Author: summer
     * @param key 要保存值的key
     * @param value 要保存的值
     */
    fun <T> putValue(key: String, value: T)

    /**
     * Description:根据key获取存储的值，如果没有返回默认值
     * Author: summer
     */
    fun <T> getValue(key: String, default: T): T
}