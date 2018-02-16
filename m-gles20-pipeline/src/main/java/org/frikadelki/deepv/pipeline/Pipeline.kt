/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.pipeline

import android.opengl.GLES20
import org.frikadelki.deepv.pipeline.program.Program
import org.frikadelki.deepv.pipeline.program.ProgramException
import org.frikadelki.deepv.pipeline.program.ProgramSource


class Pipeline {
    fun loadProgram(source: ProgramSource): Program {
        val programHandle = GLES20.glCreateProgram()
        if (0 == programHandle) {
            glClearErrors()
            throw ProgramException("Failed to create program.")
        }
        return Program(programHandle, source)
    }

    fun setClearColor(r: Float, g: Float, b: Float, a: Float) {
        GLES20.glClearColor(r, g, b, a)
    }

    fun setCullingEnabled(enabled: Boolean) {
        if (enabled) {
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        } else {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }
    }

    fun clearColorBuffer() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
}

fun glErred() : Boolean {
    val erred = GLES20.GL_NO_ERROR != GLES20.glGetError()
    if (erred) {
        glClearErrors()
    }
    return erred
}

fun glClearErrors() {
    while (GLES20.GL_NO_ERROR != GLES20.glGetError()) { }
}