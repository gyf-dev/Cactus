package com.gyf.cactus.sample

import android.content.Context
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Preference<T>(val context: Context, private val attrName: String, private val defaultValue: T, private val fileName: String = "fileName")
    : ReadWriteProperty<Any?, T> {

    private val mPreferences by lazy {
        context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreference(findProperName(property))
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(findProperName(property), value)
    }

    private fun findProperName(property: KProperty<*>) = if (attrName.isEmpty()) property.name else attrName

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    private fun findPreference(key: String): T {
        return mPreferences.run {
            when (defaultValue) {
                is String -> getString(key, defaultValue)
                is Int -> getInt(key, defaultValue)
                is Long -> getLong(key, defaultValue)
                is Float -> getFloat(key, defaultValue)
                is Boolean -> getBoolean(key, defaultValue)
                else -> throw IllegalArgumentException("Unsupported type.")
            } as T
        }
    }

    private fun putPreference(key: String, value: T) {
        mPreferences.edit().apply {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        }.apply()
    }

}