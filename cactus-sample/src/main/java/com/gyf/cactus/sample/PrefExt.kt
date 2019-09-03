package com.gyf.cactus.sample

import kotlin.reflect.jvm.jvmName


/**
 * SharedPreferences扩展函数
 * @author gyf
 * @date 2018/7/17
 */
inline fun <reified R, T> R.preference(defaultValue: T) =
    Preference(App.context, "", defaultValue, R::class.jvmName)