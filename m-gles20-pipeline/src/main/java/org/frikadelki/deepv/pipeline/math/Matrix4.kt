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

    private val tmpMatrix: FloatArray = FloatArray(MATRIX4_SIZE)

    init {
        if (offset < 0 || offset + MATRIX4_SIZE > data.size) {
            throw IllegalArgumentException("Bad offset and/or data array size.")
        }
    }

    fun setIdentity(): Matrix4 {
        MatrixUtils.setIdentityM(data, offset)
        return this
    }

    fun set(matrix: Matrix4): Matrix4 {
        System.arraycopy(matrix.data, matrix.offset, data, offset, MATRIX4_SIZE)
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

    fun rawAccess(accessor: (data: FloatArray, offset: Int) -> Unit) {
        accessor(data, offset)
    }
}