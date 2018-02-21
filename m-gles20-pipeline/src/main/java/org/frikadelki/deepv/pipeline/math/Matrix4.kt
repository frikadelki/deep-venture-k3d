/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.pipeline.math

import android.opengl.Matrix as MatrixUtils

const val MATRIX4_WH = 4
const val MATRIX4_SIZE = MATRIX4_WH * MATRIX4_WH

class Matrix4(private val data: FloatArray = FloatArray(MATRIX4_SIZE), private val offset: Int = 0) {
    private val tmpMatrix by lazy(LazyThreadSafetyMode.NONE) { FloatArray(MATRIX4_SIZE) }

    init {
        if (offset < 0 || offset + MATRIX4_SIZE > data.size) {
            throw IllegalArgumentException("Bad offset and/or data array size.")
        }
    }

    fun setE(): Matrix4 {
        MatrixUtils.setIdentityM(data, offset)
        return this
    }

    fun set(matrix: Matrix4): Matrix4 {
        System.arraycopy(matrix.data, matrix.offset, data, offset, MATRIX4_SIZE)
        return this
    }

    fun scale(scale: Vector4): Matrix4 {
        MatrixUtils.scaleM(data, offset, scale.x, scale.x, scale.x)
        return this
    }

    fun scale(sx: Float = World.C1, sy: Float = World.C1, sz: Float = World.C1): Matrix4 {
        MatrixUtils.scaleM(data, offset, sx, sy, sz)
        return this
    }

    fun translate(d: Vector4RO): Matrix4 {
        MatrixUtils.translateM(data, offset, d.x, d.y, d.z)
        return this
    }

    fun translate(dx: Float = World.C0, dy: Float = World.C0, dz: Float = World.C0): Matrix4 {
        MatrixUtils.translateM(data, offset, dx, dy, dz)
        return this
    }

    fun rotate(axis: Vector4RO, angleDegrees: Float): Matrix4 {
        MatrixUtils.rotateM(data, offset, angleDegrees, axis.x, axis.y, axis.z)
        return this
    }

    /**
     * this = this x matrix
     */
    fun multiply(matrix: Matrix4): Matrix4 {
        System.arraycopy(data, offset, tmpMatrix, 0, MATRIX4_SIZE)
        MatrixUtils.multiplyMM(data, offset, tmpMatrix, 0, matrix.data, matrix.offset)
        return this
    }

    /**
     * result = matrix x vector
     */
    fun multiply(vector: Vector4, out: Vector4 = Vector4()): Vector4 {
        MatrixUtils.multiplyMV(
                out.rawData, out.rawOffset,
                data, offset,
                vector.rawData, vector.rawOffset)
        return out
    }

    fun accessAsArray(accessor: (data: FloatArray, offset: Int) -> Unit) {
        accessor(data, offset)
    }

    internal val rawData: FloatArray
        get() = data

    internal val rawOffset: Int
        get() = offset
}