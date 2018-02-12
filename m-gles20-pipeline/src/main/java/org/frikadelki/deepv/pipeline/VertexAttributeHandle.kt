/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.pipeline

import android.opengl.GLES20
import org.frikadelki.deepv.pipeline.math.Vector4Components
import java.nio.Buffer

class VertexAttributeHandle internal constructor(private val handle: Int, private val checkDisposed: () -> Unit) {

    enum class ComponentType(val type: Int, val size: Int) {
        BYTE(GLES20.GL_BYTE, 1),
        UNSIGNED_BYTE(GLES20.GL_UNSIGNED_BYTE, 1),
        SHORT(GLES20.GL_SHORT, 2),
        UNSIGNED_SHORT(GLES20.GL_UNSIGNED_SHORT, 2),
        FIXED(GLES20.GL_FIXED, 4),
        FLOAT(GLES20.GL_FLOAT, 4);
    }

    fun enable() {
        checkDisposed()
        GLES20.glEnableVertexAttribArray(handle)
        if (glErred()) {
            throw ProgramException("Failed to enable vertex attribute array.")
        }
    }

    fun disable() {
        checkDisposed()
        GLES20.glDisableVertexAttribArray(handle)
        if (glErred()) {
            throw ProgramException("Failed to disable vertex attribute array.")
        }
    }

    fun setData(data: Buffer, components: Vector4Components, type: ComponentType,
                vertexStride: Int = 0, normalizeInts: Boolean = false) {
        checkDisposed()
        if (vertexStride < 0) {
            throw IllegalArgumentException("vertexStride")
        }
        GLES20.glVertexAttribPointer(
                handle,
                components.count,
                type.type,
                normalizeInts,
                vertexStride,
                data)
        if (glErred()) {
            throw ProgramException("Failed to set vertex attribute data.")
        }
    }
}