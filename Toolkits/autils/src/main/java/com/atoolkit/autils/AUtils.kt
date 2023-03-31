package com.atoolkit.autils

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
import com.atoolkit.common.ILog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

internal lateinit var application: Context
internal var useCache = true
internal var aLog: ILog? = null
internal const val TAG = "AUtils"

internal val Context.utilsDataStore: DataStore<Preferences> by preferencesDataStore("pf-aUtils")

/**
 * Description: AUtils提供工具类所需的基础信息，如ApplicationContext、Log等；
 * Author: summer
 * Date: 2023/3/27 19:22
 * LastModifyTime:
 */
@JvmOverloads
fun initAUtils(context: Context, log: ILog? = null, isUseLocalCache: Boolean = true) {
    application = context.applicationContext
    aLog = log
    useCache = isUseLocalCache
}

/**
 * Description: 同步读取DataStore里的值
 * Author: summer
 */
internal fun <T> readValue(key: String, defaultValue: T): T {
    val preferencesKey = getPreferencesKey(key, defaultValue)
    return runBlocking {
        application.utilsDataStore.data.map {
            it[preferencesKey] ?: defaultValue
        }.first()
    }
}

/**
 * Description: 异步读取DataStore里的值，返回Flow类型
 * Author: summer
 */
internal fun <T> readValueAsync(key: String, defaultValue: T): Flow<T> {
    val preferencesKey = getPreferencesKey(key, defaultValue)
    return application.utilsDataStore.data.map {
        it[preferencesKey] ?: defaultValue
    }
}

/**
 * Description: 向DataStore里写入指定类型的值，必须在协程中调用
 * Author: summer
 */
internal suspend fun <T> writeValue(key: String, value: T) {
    val preferencesKey = getPreferencesKey(key, value)
    application.utilsDataStore.edit {
        it[preferencesKey] = value
    }
}

/**
 * Description: 获取从DataStore存储类型key
 * Author: summer
 */
private fun <T> getPreferencesKey(key: String, value: T): Preferences.Key<T> {
    val preferencesKey = when (value) {
        is Int -> intPreferencesKey(key)
        is Float -> floatPreferencesKey(key)
        is Double -> doublePreferencesKey(key)
        is Boolean -> booleanPreferencesKey(key)
        is String -> stringPreferencesKey(key)
        is Long -> longPreferencesKey(key)
        is Set<*> -> stringSetPreferencesKey(key)
        else -> throw IllegalArgumentException("The type of defaultValue($value) cannot store in DataStore")
    }
    return preferencesKey as Preferences.Key<T>
}