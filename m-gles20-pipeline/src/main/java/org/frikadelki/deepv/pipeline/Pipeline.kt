/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.pipeline

import android.opengl.GLES20


class Pipeline {
    fun loadProgram(source: ProgramSource): Program {
        val programHandle = GLES20.glCreateProgram()
        if (0 == programHandle) {
            glClearErrors()
            throw ProgramException("Failed to create program.")
        }
        return Program(programHandle, source)
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