/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.pipeline

import android.opengl.GLES20
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4

private const val MAX_VECTOR_SIZE: Int = 4

class UniformHandle internal constructor(private val handle: Int, private val checkDisposed: () -> Unit) {
    enum class VectorSize(val size: Int) {
        ONE(1) {
            override fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int) {
                GLES20.glUniform1fv(handle, count, data, offset)
            }
        },
        TWO(2) {
            override fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int) {
                GLES20.glUniform2fv(handle, count, data, offset)
            }
        },
        THREE(3) {
            override fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int) {
                GLES20.glUniform3fv(handle, count, data, offset)
            }
        },
        FOUR(4) {
            override fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int) {
                GLES20.glUniform4fv(handle, count, data, offset)
            }
        },
        ;

        protected abstract fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int)

        fun bind(handle: Int, data: FloatArray, count: Int, offset: Int) {
            checkData(data, count, offset)
            bindInternal(handle, data, count, offset)
        }

        fun checkData(data: FloatArray, count: Int, offset: Int) {
            if (count <= 0) {
                throw IllegalArgumentException()
            }
            if (offset < 0) {
                throw IllegalArgumentException()
            }
            if (offset + count * size > data.size) {
                throw IllegalArgumentException()
            }
        }
    }

    enum class MatrixSize(wh: Int) {
        TWO(2) {
            override fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int, transpose: Boolean) {
                GLES20.glUniformMatrix2fv(handle, count, transpose, data, offset)
            }
        },
        THREE(3) {
            override fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int, transpose: Boolean) {
                GLES20.glUniformMatrix3fv(handle, count, transpose, data, offset)
            }
        },
        FOUR(4) {
            override fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int, transpose: Boolean) {
                GLES20.glUniformMatrix4fv(handle, count, transpose, data, offset)
            }
        },
        ;

        private val size: Int = wh * wh

        protected abstract fun bindInternal(handle: Int, data: FloatArray, count: Int, offset: Int, transpose: Boolean)

        fun bind(handle: Int, data: FloatArray, count: Int, offset: Int, transpose: Boolean) {
            if (count <= 0) {
                throw IllegalArgumentException()
            }
            if (offset < 0) {
                throw IllegalArgumentException()
            }
            if (offset + count * size > data.size) {
                throw IllegalArgumentException()
            }
            bindInternal(handle, data, count, offset, transpose)
        }
    }

    fun setVector(vector: Vector4) {
        vector.rawAccess { data, offset ->
            setVector(data, VectorSize.FOUR, 1, offset)
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
        checkDisposed()
        size.bind(handle, data, count, offset)
        if (glErred()) {
            throw ProgramException("Failed to set uniform vector data.")
        }
    }

    fun setMatrix(matrix: Matrix4) {
        matrix.rawAccess { data, offset ->
            setMatrix(data, MatrixSize.FOUR, 1, offset)
        }
    }

    fun setMatrix(data: FloatArray, size: MatrixSize, count: Int = 1, offset: Int = 0, transpose: Boolean = false) {
        checkDisposed()
        size.bind(handle, data, count, offset, transpose)
        if (glErred()) {
            throw ProgramException("Failed to set uniform matrix data.")
        }
    }
}