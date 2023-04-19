package com.atoolkit.astorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


/**
 * Author:summer
 * Time: 2023/4/19 19:29
 * Description: ADataStoreKVImpl是使用DataStore实现的kv存储实现类
 */
internal val Context.storageKvDataStore: DataStore<Preferences> by preferencesDataStore(DEFAULT_KV_FILE_NAME)

internal class ADataStoreKVImpl : IKVStorage {

    override fun init(context: Context, config: AStorageConfig) {
        aLog?.v(TAG, "ADataStoreKVImpl init")
    }

    override fun <T> putValue(key: String, value: T) {
        val preferencesKey = getPreferencesKey(key, value)
        runBlocking {
            application.storageKvDataStore.edit {
                it[preferencesKey] = value
            }
        }
    }

    override fun <T> getValue(key: String, default: T): T {
        val preferencesKey = getPreferencesKey(key, default)
        return runBlocking {
            application.storageKvDataStore.data.map {
                it[preferencesKey] ?: default
            }.first()
        }
    }

    /**
     * Description: 根据存储的value类型获取对应的Preferences.Key实例
     * Author: summer
     */
    private fun <T> getPreferencesKey(key: String, value: T): Preferences.Key<T> {
        val resultKey = when (value) {
            is String -> stringPreferencesKey(key)
            is Int -> intPreferencesKey(key)
            is Float -> floatPreferencesKey(key)
            is Long -> longPreferencesKey(key)
            is Double -> doublePreferencesKey(key)
            is Boolean -> booleanPreferencesKey(key)
            is Set<*> -> stringSetPreferencesKey(key)
            else -> throw IllegalArgumentException("The type of value($value) is unsupported by DataStore")
        }
        return resultKey as Preferences.Key<T>
    }
}