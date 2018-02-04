/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.pipeline

import android.opengl.GLES20

private const val MAX_VECTOR_SIZE: Int = 4

class UniformHandle(private val handle: Int) {
    enum class VectorSize(val size: Int) {
        ONE(1) {
            override fun bind(handle: Int, data: FloatArray, count: Int, offset: Int) {
                checkData(data, count, offset)
                GLES20.glUniform1fv(handle, count, data, offset)
            }
        },
        TWO(2) {
            override fun bind(handle: Int, data: FloatArray, count: Int, offset: Int) {
                checkData(data, count, offset)
                GLES20.glUniform2fv(handle, count, data, offset)
            }
        },
        THREE(3) {
            override fun bind(handle: Int, data: FloatArray, count: Int, offset: Int) {
                checkData(data, count, offset)
                GLES20.glUniform3fv(handle, count, data, offset)
            }
        },
        FOUR(4) {
            override fun bind(handle: Int, data: FloatArray, count: Int, offset: Int) {
                checkData(data, count, offset)
                GLES20.glUniform4fv(handle, count, data, offset)
            }
        },
        ;

        abstract fun bind(handle: Int, data: FloatArray, count: Int, offset: Int)

        fun checkData(data: FloatArray, count: Int, offset: Int) {
            if (count <= 0) {
                throw RuntimeException()
            }
            if (offset < 0 || offset >= data.size) {
                throw IllegalArgumentException()
            }
            if (offset + count*size < data.size) {
                throw IllegalArgumentException()
            }
        }
    }

    fun setVector(data: FloatArray) {
        if (0 == data.size || data.size > MAX_VECTOR_SIZE) {
            throw IllegalArgumentException("Can't auto detect vector size.")
        }

        val detectedSize = VectorSize.values().firstOrNull { it.size == data.size } ?: throw IllegalStateException()
        setVector(data, detectedSize)
    }

    fun setVector(data: FloatArray, size: VectorSize, count: Int = 1, offset: Int = 0) {
        size.bind(handle, data, count, offset)
        if (glErred()) {
            throw ProgramException("Failed to set uniform data.")
        }
    }
}