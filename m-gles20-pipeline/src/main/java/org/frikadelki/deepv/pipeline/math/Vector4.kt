/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.pipeline.math

const val VECTOR4_SIZE = 4
internal const val VECTOR4_X = 0
internal const val VECTOR4_Y = 1
internal const val VECTOR4_Z = 2
internal const val VECTOR4_W = 3

class Vector4(private val data: FloatArray = FloatArray(VECTOR4_SIZE), private val offset: Int = 0) {

    init {
        if (offset < 0 || offset + VECTOR4_SIZE > data.size) {
            throw IllegalArgumentException("Bad offset and/or data array size.")
        }
    }

    constructor(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f, w: Float = 0.0f)
            : this(floatArrayOf(x, y, z, w), 0)

    fun set(x: Float = this.x, y: Float = this.y, z: Float = this.z, w: Float = this.w) {
        data[offset + VECTOR4_X] = x
        data[offset + VECTOR4_Y] = y
        data[offset + VECTOR4_Z] = z
        data[offset + VECTOR4_W] = w
    }

    fun rawAccess(accessor: (data: FloatArray, offset: Int) -> Unit) {
        accessor(data, offset)
    }

    var x: Float
        get() = data[offset + VECTOR4_X]
        set(value) { data[offset + VECTOR4_X] = value }

    var y: Float
        get() = data[offset + VECTOR4_Y]
        set(value) { data[offset + VECTOR4_Y] = value }

    var z: Float
        get() = data[offset + VECTOR4_Z]
        set(value) { data[offset + VECTOR4_Z] = value }

    var w: Float
        get() = data[offset + VECTOR4_W]
        set(value) { data[offset + VECTOR4_W] = value }

    var r: Float
        get() = data[offset + VECTOR4_X]
        set(value) { data[offset + VECTOR4_X] = value }

    var g: Float
        get() = data[offset + VECTOR4_Y]
        set(value) { data[offset + VECTOR4_Y] = value }

    var b: Float
        get() = data[offset + VECTOR4_Z]
        set(value) { data[offset + VECTOR4_Z] = value }

    var a: Float
        get() = data[offset + VECTOR4_W]
        set(value) { data[offset + VECTOR4_W] = value }
}