/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/12
 */

package org.frikadelki.kash.klang

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : Any> dirtyProperty(valueBuilder: () -> T) = DirtyProperty(valueBuilder)

class DirtyProperty<out TValue : Any> internal constructor(private val valueBuilder: () -> TValue)
    : ReadOnlyProperty<Any, TValue> {
    private lateinit var lastValue: TValue
    private var dirty: Boolean = true

    fun markDirty() {
        dirty = true
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): TValue {
        if (dirty) {
            dirty = true
            lastValue = valueBuilder()
        }
        return lastValue
    }
}