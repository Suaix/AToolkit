package com.atoolkit.apermission

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

internal val Context.permissionDataStore: DataStore<Preferences> by preferencesDataStore("pf-aPermission")

internal class APermissionLocalDataSource {

    fun <T> readValue(key: String, defaultValue: T): T {
        val preferencesKey = getPreferencesKey(key, defaultValue)
        return runBlocking {
            application.permissionDataStore.data.map {
                it[preferencesKey] ?: defaultValue
            }.first()
        }
    }

    fun <T> readValueAsync(key: String, defaultValue: T): Flow<T> {
        val preferencesKey = getPreferencesKey(key, defaultValue)
        return application.permissionDataStore.data.map {
            it[preferencesKey] ?: defaultValue
        }
    }

    suspend fun <T> writeValue(key: String, value: T) {
        val preferencesKey = getPreferencesKey(key, value)
        application.permissionDataStore.edit {
            it[preferencesKey] = value
        }
    }

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
}